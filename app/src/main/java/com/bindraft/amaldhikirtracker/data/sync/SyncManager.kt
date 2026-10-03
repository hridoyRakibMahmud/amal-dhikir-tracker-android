package com.bindraft.amaldhikirtracker.data.sync

import com.bindraft.amaldhikirtracker.data.local.dao.AmalDao
import com.bindraft.amaldhikirtracker.data.local.entities.Dhikir
import com.bindraft.amaldhikirtracker.data.local.entities.DhikirLog
import com.bindraft.amaldhikirtracker.data.local.entities.FastingLog
import com.bindraft.amaldhikirtracker.data.local.entities.FastingType
import com.bindraft.amaldhikirtracker.data.local.entities.SalatLog
import com.bindraft.amaldhikirtracker.util.awaitResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Mirrors Room data to Firestore under users/{uid}/... keyed by each row's syncId.
 * Last-write-wins by updatedAt. Room stays the UI source of truth; this only moves data.
 * Remote changes are applied straight to the DAO (never through AmalRepository), so they
 * don't echo back as new pushes.
 */
class SyncManager(private val dao: AmalDao) {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()
    private val listeners = mutableListOf<ListenerRegistration>()

    @Volatile
    private var activeUid: String? = null

    // Parents before children, so foreign-key references resolve during pull.
    private val collections = listOf("fastingTypes", "dhikirs", "salatLogs", "dhikirLogs", "fastingLogs")

    fun attach() {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val uid = auth.currentUser?.uid
            if (uid == null) stop() else if (uid != activeUid) start(uid)
        }
    }

    private fun start(uid: String) {
        stop()
        activeUid = uid
        scope.launch {
            val root = userRoot(uid)
            pullAll(root)
            pushAll()
            listen(root)
        }
    }

    private fun stop() {
        synchronized(lock) {
            listeners.forEach { it.remove() }
            listeners.clear()
        }
        activeUid = null
    }

    // ---- Push (called by AmalRepository after each local write) ----

    fun pushDhikir(dhikir: Dhikir) = write("dhikirs", dhikir.syncId, mapOf(
        "name" to dhikir.name,
        "arabicName" to dhikir.arabicName,
        "category" to dhikir.category,
        "isCustom" to dhikir.isCustom,
        "dailyTarget" to dhikir.dailyTarget,
        "updatedAt" to dhikir.updatedAt
    ))

    fun pushDhikirLog(log: DhikirLog) = scope.launch {
        val dhikirSyncId = dao.getDhikirById(log.dhikirId)?.syncId ?: return@launch
        write("dhikirLogs", log.syncId, mapOf(
            "dhikirSyncId" to dhikirSyncId,
            "count" to log.count,
            "date" to log.date.toString(),
            "updatedAt" to log.updatedAt
        ))
    }

    fun pushSalatLog(log: SalatLog) = write("salatLogs", log.syncId, mapOf(
        "salatName" to log.salatName,
        "type" to log.type,
        "isCompleted" to log.isCompleted,
        "sunnahDone" to log.sunnahDone,
        "icon" to log.icon,
        "arabicName" to log.arabicName,
        "date" to log.date.toString(),
        "updatedAt" to log.updatedAt
    ))

    fun pushFastingType(type: FastingType) = write("fastingTypes", type.syncId, mapOf(
        "name" to type.name,
        "arabicName" to type.arabicName,
        "isCustom" to type.isCustom,
        "updatedAt" to type.updatedAt
    ))

    fun pushFastingLog(log: FastingLog) = scope.launch {
        val typeSyncId = log.fastingTypeId?.let { dao.getFastingTypeById(it)?.syncId }
        write("fastingLogs", log.syncId, mapOf(
            "date" to log.date.toString(),
            "fastingTypeSyncId" to typeSyncId,
            "updatedAt" to log.updatedAt
        ))
    }

    fun deleteRemote(collection: String, syncId: String) {
        val uid = activeUid ?: return
        userRoot(uid).collection(collection).document(syncId).delete()
    }

    private fun write(collection: String, syncId: String, data: Map<String, Any?>) {
        val uid = activeUid ?: return
        userRoot(uid).collection(collection).document(syncId).set(data)
    }

    private suspend fun pushAll() {
        dao.getAllFastingTypes().first().forEach { pushFastingType(it) }
        dao.getAllDhikirs().first().forEach { pushDhikir(it) }
        dao.getAllSalatLogsOnce().forEach { pushSalatLog(it) }
        dao.getAllDhikirLogsOnce().forEach { pushDhikirLog(it) }
        dao.getAllFastingLogsOnce().forEach { pushFastingLog(it) }
    }

    // ---- Pull / listen (applied straight to the DAO, last-write-wins) ----

    private suspend fun pullAll(root: DocumentReference) {
        for (name in collections) {
            root.collection(name).get().awaitResult().documents.forEach { applyDocument(name, it) }
        }
    }

    private fun listen(root: DocumentReference) {
        synchronized(lock) {
            collections.forEach { name ->
                listeners += root.collection(name).addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch {
                        snapshot.documentChanges.forEach { change ->
                            if (change.type == DocumentChange.Type.REMOVED) {
                                removeLocal(name, change.document.id)
                            } else {
                                applyDocument(name, change.document)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun applyDocument(collection: String, doc: DocumentSnapshot) {
        when (collection) {
            "fastingTypes" -> applyFastingType(doc)
            "dhikirs" -> applyDhikir(doc)
            "salatLogs" -> applySalatLog(doc)
            "dhikirLogs" -> applyDhikirLog(doc)
            "fastingLogs" -> applyFastingLog(doc)
        }
    }

    private suspend fun applyFastingType(doc: DocumentSnapshot) {
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val local = dao.getFastingTypeBySyncId(doc.id)
        if (local != null && local.updatedAt >= remoteUpdatedAt) return
        dao.insertFastingType(FastingType(
            id = local?.id ?: 0,
            syncId = doc.id,
            name = doc.getString("name") ?: return,
            arabicName = doc.getString("arabicName"),
            isCustom = doc.getBoolean("isCustom") ?: false,
            updatedAt = remoteUpdatedAt
        ))
    }

    private suspend fun applyDhikir(doc: DocumentSnapshot) {
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val local = dao.getDhikirBySyncId(doc.id)
        if (local != null && local.updatedAt >= remoteUpdatedAt) return
        dao.insertDhikir(Dhikir(
            id = local?.id ?: 0,
            syncId = doc.id,
            name = doc.getString("name") ?: return,
            arabicName = doc.getString("arabicName"),
            category = doc.getString("category"),
            isCustom = doc.getBoolean("isCustom") ?: false,
            dailyTarget = doc.getLong("dailyTarget")?.toInt(),
            updatedAt = remoteUpdatedAt
        ))
    }

    private suspend fun applySalatLog(doc: DocumentSnapshot) {
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val local = dao.getSalatLogBySyncId(doc.id)
        if (local != null && local.updatedAt >= remoteUpdatedAt) return
        dao.insertSalatLog(SalatLog(
            id = local?.id ?: 0,
            syncId = doc.id,
            salatName = doc.getString("salatName") ?: return,
            type = doc.getString("type") ?: return,
            isCompleted = doc.getBoolean("isCompleted") ?: false,
            sunnahDone = doc.getBoolean("sunnahDone") ?: false,
            icon = doc.getString("icon"),
            arabicName = doc.getString("arabicName"),
            date = LocalDate.parse(doc.getString("date") ?: return),
            updatedAt = remoteUpdatedAt
        ))
    }

    private suspend fun applyDhikirLog(doc: DocumentSnapshot) {
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val local = dao.getDhikirLogBySyncId(doc.id)
        if (local != null && local.updatedAt >= remoteUpdatedAt) return
        val dhikirId = dao.getDhikirBySyncId(doc.getString("dhikirSyncId") ?: return)?.id ?: return
        dao.insertDhikirLog(DhikirLog(
            id = local?.id ?: 0,
            syncId = doc.id,
            dhikirId = dhikirId,
            count = doc.getLong("count")?.toInt() ?: 0,
            date = LocalDate.parse(doc.getString("date") ?: return),
            updatedAt = remoteUpdatedAt
        ))
    }

    private suspend fun applyFastingLog(doc: DocumentSnapshot) {
        val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
        val local = dao.getFastingLogBySyncId(doc.id)
        if (local != null && local.updatedAt >= remoteUpdatedAt) return
        val typeId = doc.getString("fastingTypeSyncId")?.let { dao.getFastingTypeBySyncId(it)?.id }
        dao.insertFastingLog(FastingLog(
            id = local?.id ?: 0,
            syncId = doc.id,
            date = LocalDate.parse(doc.getString("date") ?: return),
            fastingTypeId = typeId,
            updatedAt = remoteUpdatedAt
        ))
    }

    private suspend fun removeLocal(collection: String, syncId: String) {
        when (collection) {
            "dhikirs" -> dao.getDhikirBySyncId(syncId)?.let { dao.deleteDhikir(it) }
            "dhikirLogs" -> dao.getDhikirLogBySyncId(syncId)?.let { dao.deleteDhikirLog(it) }
            "salatLogs" -> dao.getSalatLogBySyncId(syncId)?.let { dao.deleteSalatLog(it) }
            "fastingTypes" -> dao.getFastingTypeBySyncId(syncId)?.let { dao.deleteFastingType(it) }
            "fastingLogs" -> dao.getFastingLogBySyncId(syncId)?.let { dao.deleteFastingLog(it) }
        }
    }

    private fun userRoot(uid: String): DocumentReference =
        firestore.collection("users").document(uid)
}
