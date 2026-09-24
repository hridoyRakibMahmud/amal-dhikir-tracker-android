package com.bindraft.amaldhikirtracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bindraft.amaldhikirtracker.data.local.entities.Dhikir
import com.bindraft.amaldhikirtracker.data.local.entities.SalatLog
import com.bindraft.amaldhikirtracker.ui.theme.AppTheme
import com.bindraft.amaldhikirtracker.ui.theme.NotoNaskhArabic
import com.bindraft.amaldhikirtracker.ui.viewmodel.TrackerViewModel

@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel,
    onNavigateToCounter: (Long) -> Unit,
    onNavigateToManageDhikirs: () -> Unit
) {
    val fardSalats by viewModel.fardSalatLogs.collectAsState()
    val naflSalats by viewModel.naflSalatLogs.collectAsState()
    val allDhikirs by viewModel.allDhikirs.collectAsState()
    val todayDhikirLogs by viewModel.todayDhikirLogs.collectAsState()
    val dateHeader by viewModel.dateHeader.collectAsState()
    val streak by viewModel.streak.collectAsState()

    var showAddNaflForm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            HijriDateHeader(primary = dateHeader.first, secondary = dateHeader.second, streak = streak)
        }

        item {
            SectionHeader(title = "SALAH · FARD")
        }
        item {
            SectionCard {
                fardSalats.forEachIndexed { index, salat ->
                    if (index > 0) RowDivider()
                    FardRow(
                        salat = salat,
                        onToggleFard = { viewModel.toggleSalat(salat) },
                        onToggleSunnah = { viewModel.toggleSunnah(salat) }
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = "NAFL · VOLUNTARY",
                action = "+ Add",
                onAction = { showAddNaflForm = !showAddNaflForm }
            )
        }
        item {
            SectionCard {
                naflSalats.forEachIndexed { index, salat ->
                    if (index > 0) RowDivider()
                    NaflRow(salat = salat, onToggle = { viewModel.toggleSalat(salat) })
                }
                if (showAddNaflForm) {
                    if (naflSalats.isNotEmpty()) RowDivider()
                    AddNaflInlineForm(
                        onSave = { name ->
                            viewModel.addVoluntarySalat(name)
                            showAddNaflForm = false
                        },
                        onCancel = { showAddNaflForm = false }
                    )
                }
            }
        }

        item {
            SectionHeader(title = "DHIKIR", action = "Manage", onAction = onNavigateToManageDhikirs)
        }
        items(allDhikirs.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { dhikir ->
                    val todayCount = todayDhikirLogs.find { it.dhikirId == dhikir.id }?.count ?: 0
                    DhikirCard(
                        dhikir = dhikir,
                        todayCount = todayCount,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToCounter(dhikir.id) }
                    )
                }
                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun HijriDateHeader(primary: String, secondary: String, streak: Int) {
    Column(modifier = Modifier.padding(bottom = 22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Nightlight,
                contentDescription = null,
                tint = AppTheme.colors.gold,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = primary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.text,
                modifier = Modifier.weight(1f)
            )
            if (streak > 0) {
                Surface(
                    color = AppTheme.colors.goldTint,
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = AppTheme.colors.gold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${streak}d",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.goldStrong
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = secondary,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.textMuted,
            modifier = Modifier.padding(start = 30.dp)
        )
    }
}

@Composable
private fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.colors.textMuted
        )
        if (action != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelLarge,
                color = AppTheme.colors.gold,
                modifier = Modifier.clickable { onAction?.invoke() }
            )
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = AppTheme.colors.surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, AppTheme.colors.border),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 22.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(color = AppTheme.colors.border, thickness = 1.dp)
}

@Composable
private fun FardRow(salat: SalatLog, onToggleFard: () -> Unit, onToggleSunnah: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleFard)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (salat.isCompleted) AppTheme.colors.green else AppTheme.colors.surface)
                .border(width = if (salat.isCompleted) 0.dp else 1.5.dp, color = AppTheme.colors.border, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (salat.isCompleted) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Completed",
                    tint = AppTheme.colors.onGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = salat.salatName,
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.colors.text
            )
            Spacer(modifier = Modifier.height(6.dp))
            SunnahChip(done = salat.sunnahDone, onClick = onToggleSunnah)
        }
        if (!salat.arabicName.isNullOrEmpty()) {
            Text(
                text = salat.arabicName,
                fontFamily = NotoNaskhArabic,
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.colors.textMuted
            )
        }
    }
}

@Composable
private fun SunnahChip(done: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (done) AppTheme.colors.goldTint else Color.Transparent,
        border = if (done) null else BorderStroke(1.dp, AppTheme.colors.border),
        shape = RoundedCornerShape(100.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (done) "✓" else "+",
                style = MaterialTheme.typography.labelSmall,
                color = if (done) AppTheme.colors.goldStrong else AppTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Sunnah",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (done) AppTheme.colors.goldStrong else AppTheme.colors.textMuted
            )
        }
    }
}

private fun naflIconFor(name: String?): ImageVector = when (name) {
    "bedtime" -> Icons.Rounded.Bedtime
    "wb_sunny" -> Icons.Rounded.WbSunny
    else -> Icons.Rounded.SelfImprovement
}

@Composable
private fun NaflRow(salat: SalatLog, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (salat.isCompleted) AppTheme.colors.green else AppTheme.colors.surface)
                .border(width = if (salat.isCompleted) 0.dp else 1.5.dp, color = AppTheme.colors.border, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (salat.isCompleted) {
                Icon(Icons.Rounded.Check, contentDescription = "Completed", tint = AppTheme.colors.onGreen, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Icon(
            imageVector = naflIconFor(salat.icon),
            contentDescription = null,
            tint = AppTheme.colors.gold,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = salat.salatName,
            style = MaterialTheme.typography.titleMedium,
            color = AppTheme.colors.text,
            modifier = Modifier.weight(1f)
        )
        if (!salat.arabicName.isNullOrEmpty()) {
            Text(
                text = salat.arabicName,
                fontFamily = NotoNaskhArabic,
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.colors.textMuted
            )
        }
    }
}

@Composable
private fun AddNaflInlineForm(onSave: (String) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("e.g. Witr") },
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { if (name.isNotBlank()) onSave(name) else onCancel() },
            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.green, contentColor = AppTheme.colors.onGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save")
        }
    }
}

@Composable
private fun DhikirCard(dhikir: Dhikir, todayCount: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = AppTheme.colors.surface,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dhikir.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.text
            )
            if (!dhikir.arabicName.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dhikir.arabicName,
                    fontFamily = NotoNaskhArabic,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.textMuted
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            val target = dhikir.dailyTarget
            if (target != null && target > 0) {
                LinearProgressIndicator(
                    progress = { (todayCount.toFloat() / target).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                    color = AppTheme.colors.green,
                    trackColor = AppTheme.colors.surface2
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$todayCount / $target",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.colors.textMuted
                )
            } else {
                Text(
                    text = todayCount.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.green
                )
            }
        }
    }
}
