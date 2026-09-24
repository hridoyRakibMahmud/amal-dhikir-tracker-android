package com.bindraft.amaldhikirtracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bindraft.amaldhikirtracker.data.local.entities.Dhikir
import com.bindraft.amaldhikirtracker.ui.theme.AppTheme
import com.bindraft.amaldhikirtracker.ui.theme.NotoNaskhArabic
import com.bindraft.amaldhikirtracker.ui.viewmodel.TrackerViewModel

@Composable
fun ManageDhikirsScreen(
    viewModel: TrackerViewModel,
    onNavigateBack: () -> Unit
) {
    val allDhikirs by viewModel.allDhikirs.collectAsState()
    val todayLogs by viewModel.todayDhikirLogs.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTargetId by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = AppTheme.colors.text)
            }
            Text(
                text = "Manage Dhikirs",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.text
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allDhikirs, key = { it.id }) { dhikir ->
                val todayCount = todayLogs.find { it.dhikirId == dhikir.id }?.count ?: 0
                DhikirManagementItem(
                    dhikir = dhikir,
                    todayCount = todayCount,
                    editingTarget = editingTargetId == dhikir.id,
                    onStartEditTarget = { editingTargetId = dhikir.id },
                    onCancelEditTarget = { editingTargetId = null },
                    onSaveTarget = { target ->
                        viewModel.setDhikirTarget(dhikir.id, target)
                        editingTargetId = null
                    },
                    onClearTarget = { viewModel.setDhikirTarget(dhikir.id, null) },
                    onDelete = { viewModel.deleteDhikir(dhikir) }
                )
            }
            item {
                DashedAddButton(onClick = { showAddDialog = true })
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showAddDialog) {
        AddDhikirDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, arabic, category ->
                viewModel.addDhikir(name, arabic, category)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun DhikirManagementItem(
    dhikir: Dhikir,
    todayCount: Int,
    editingTarget: Boolean,
    onStartEditTarget: () -> Unit,
    onCancelEditTarget: () -> Unit,
    onSaveTarget: (Int?) -> Unit,
    onClearTarget: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppTheme.colors.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dhikir.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.text
                    )
                    if (!dhikir.category.isNullOrEmpty()) {
                        Text(
                            text = dhikir.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.textMuted
                        )
                    }
                }
                if (!dhikir.arabicName.isNullOrEmpty()) {
                    Text(
                        text = dhikir.arabicName,
                        fontFamily = NotoNaskhArabic,
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTheme.colors.textMuted,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                if (dhikir.isCustom) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = AppTheme.colors.danger,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (editingTarget) {
                var text by remember { mutableStateOf(dhikir.dailyTarget?.toString() ?: "") }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { if (it.all { c -> c.isDigit() }) text = it },
                        placeholder = { Text("e.g. 100") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onCancelEditTarget) { Text("Cancel") }
                    TextButton(onClick = { onSaveTarget(text.toIntOrNull()) }) {
                        Text("Save", color = AppTheme.colors.gold, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    val target = dhikir.dailyTarget
                    Text(
                        text = if (target != null && target > 0) "$todayCount / $target today" else "No target set",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.textMuted,
                        modifier = Modifier.weight(1f)
                    )
                    if (target != null && target > 0) {
                        Text(
                            text = "Edit target",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.gold,
                            modifier = Modifier.clickable(onClick = onStartEditTarget)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.gold,
                            modifier = Modifier.clickable(onClick = onClearTarget)
                        )
                    } else {
                        Text(
                            text = "Set target",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.gold,
                            modifier = Modifier.clickable(onClick = onStartEditTarget)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashedAddButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, AppTheme.colors.border)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, tint = AppTheme.colors.textMuted)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Add custom Dhikir",
                style = MaterialTheme.typography.labelLarge,
                color = AppTheme.colors.textMuted
            )
        }
    }
}

@Composable
private fun AddDhikirDialog(
    onDismiss: () -> Unit,
    onSave: (String, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var arabicName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Dhikir") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = arabicName,
                    onValueChange = { arabicName = it },
                    label = { Text("Arabic (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, arabicName.ifBlank { null }, category.ifBlank { null }) },
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.green, contentColor = AppTheme.colors.onGreen)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
