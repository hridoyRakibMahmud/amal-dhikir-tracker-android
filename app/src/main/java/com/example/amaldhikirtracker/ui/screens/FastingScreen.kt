package com.example.amaldhikirtracker.ui.screens

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amaldhikirtracker.data.local.entities.FastingLog
import com.example.amaldhikirtracker.data.local.entities.FastingType
import com.example.amaldhikirtracker.ui.theme.AppTheme
import com.example.amaldhikirtracker.ui.theme.NotoNaskhArabic
import com.example.amaldhikirtracker.ui.viewmodel.FastingViewModel
import com.example.amaldhikirtracker.ui.viewmodel.StandardFastInfo
import com.example.amaldhikirtracker.util.FastingRules
import com.example.amaldhikirtracker.util.HijriCalendar
import java.time.LocalDate

@Composable
fun FastingScreen(
    viewModel: FastingViewModel,
    onNavigateBack: () -> Unit
) {
    val monthLabel by viewModel.visibleMonthLabel.collectAsState()
    val gregorianLabel by viewModel.visibleGregorianLabel.collectAsState()
    val hijriOffset by viewModel.hijriOffset.collectAsState()
    val spiritualDate by viewModel.spiritualDate.collectAsState()
    val monthDates by viewModel.monthDates.collectAsState()
    val monthLogs by viewModel.monthLogs.collectAsState()
    val allTypes by viewModel.allTypes.collectAsState()
    val standardFasts by viewModel.standardFasts.collectAsState()

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showAddType by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = AppTheme.colors.text)
                }
                Text(
                    text = "Nafl Fasting",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.text
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(Icons.Rounded.ChevronLeft, contentDescription = "Previous month", tint = AppTheme.colors.text)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = monthLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.text
                    )
                    Text(
                        text = gregorianLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.textMuted
                    )
                }
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.Rounded.ChevronRight, contentDescription = "Next month", tint = AppTheme.colors.text)
                }
            }
        }

        item {
            Surface(
                color = AppTheme.colors.surface,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, AppTheme.colors.border),
                modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                            Text(
                                text = label,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textMuted
                            )
                        }
                    }
                    // Sunday-first grid, padded with leading blanks so weekday columns line up.
                    val leadingBlanks = monthDates.firstOrNull()?.let { it.dayOfWeek.value % 7 } ?: 0
                    val paddedCells: List<LocalDate?> = List(leadingBlanks) { null } + monthDates
                    paddedCells.chunked(7).forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            week.forEach { date ->
                                if (date == null) {
                                    Spacer(modifier = Modifier.weight(1f))
                                } else {
                                    DayCell(
                                        date = date,
                                        fasted = monthLogs.containsKey(date),
                                        forbidden = FastingRules.isFastingForbidden(date, hijriOffset),
                                        future = date.isAfter(spiritualDate),
                                        hijriOffset = hijriOffset,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedDate = date }
                                    )
                                }
                            }
                            repeat(7 - week.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }

        item {
            LegendRow(modifier = Modifier.padding(bottom = 22.dp))
        }

        item {
            Text(
                text = "STANDARD SUNNAH FASTS",
                style = MaterialTheme.typography.labelMedium,
                color = AppTheme.colors.textMuted,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }
        items(standardFasts) { info ->
            StandardFastRow(info = info, onClick = { info.nextOccurrence?.let { viewModel.jumpToDate(it.date) } })
        }

        item { Spacer(modifier = Modifier.height(22.dp)) }

        item {
            DashedAddFastingTypeButton(onClick = { showAddType = true })
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    selectedDate?.let { date ->
        FastingDayDialog(
            date = date,
            existingLog = monthLogs[date],
            types = allTypes,
            onDismiss = { selectedDate = null },
            onLog = { typeId ->
                viewModel.toggleDay(date, typeId)
                selectedDate = null
            },
            onRemove = {
                viewModel.toggleDay(date, null)
                selectedDate = null
            }
        )
    }

    if (showAddType) {
        AddFastingTypeDialog(
            onDismiss = { showAddType = false },
            onSave = { name, arabic ->
                viewModel.addFastingType(name, arabic)
                showAddType = false
            }
        )
    }
}

@Composable
private fun DayCell(date: LocalDate, fasted: Boolean, forbidden: Boolean, future: Boolean, hijriOffset: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val hijriDay = HijriCalendar.from(date, hijriOffset).day
    val mandatory = !fasted && !forbidden && !future && FastingRules.isMandatoryFastDay(date, hijriOffset)
    val recommended = !fasted && !forbidden && !future && !mandatory && FastingRules.isRecommendedFastDay(date, hijriOffset)
    val textColor = when {
        fasted -> AppTheme.colors.onGreen
        forbidden -> AppTheme.colors.danger
        future -> AppTheme.colors.textMuted.copy(alpha = 0.5f)
        mandatory -> AppTheme.colors.greenStrong
        recommended -> AppTheme.colors.goldStrong
        else -> AppTheme.colors.text
    }
    Box(
        modifier = modifier
            .padding(3.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                when {
                    fasted -> AppTheme.colors.green
                    forbidden -> AppTheme.colors.dangerTint
                    mandatory -> AppTheme.colors.greenTint
                    recommended -> AppTheme.colors.goldTint
                    else -> Color.Transparent
                }
            )
            .then(
                when {
                    mandatory -> Modifier.border(1.dp, AppTheme.colors.green, CircleShape)
                    recommended -> Modifier.border(1.dp, AppTheme.colors.gold, CircleShape)
                    else -> Modifier
                }
            )
            .clickable(enabled = !forbidden && !future, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = hijriDay.toString(),
                fontSize = 13.sp,
                lineHeight = 14.sp,
                fontWeight = if (fasted || recommended || mandatory) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 8.sp,
                lineHeight = 9.sp,
                color = textColor.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun LegendRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LegendItem(color = AppTheme.colors.green, label = "Fasted")
        LegendItem(color = AppTheme.colors.greenTint, borderColor = AppTheme.colors.green, label = "Ramadan")
        LegendItem(color = AppTheme.colors.goldTint, borderColor = AppTheme.colors.gold, label = "Sunnah")
        LegendItem(color = AppTheme.colors.dangerTint, label = "Forbidden")
    }
}

@Composable
private fun LegendItem(color: Color, label: String, borderColor: Color? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
                .then(if (borderColor != null) Modifier.border(1.dp, borderColor, CircleShape) else Modifier)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.textMuted)
    }
}

@Composable
private fun StandardFastRow(info: StandardFastInfo, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = AppTheme.colors.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AppTheme.colors.border),
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(info.type.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.text)
                if (info.nextOccurrence != null) {
                    Text(info.nextOccurrence.text, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.gold)
                }
            }
            if (!info.type.arabicName.isNullOrEmpty()) {
                Text(
                    text = info.type.arabicName,
                    fontFamily = NotoNaskhArabic,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun DashedAddFastingTypeButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, AppTheme.colors.border)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, tint = AppTheme.colors.textMuted)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add fasting type", style = MaterialTheme.typography.labelLarge, color = AppTheme.colors.textMuted)
        }
    }
}

@Composable
private fun FastingDayDialog(
    date: LocalDate,
    existingLog: FastingLog?,
    types: List<FastingType>,
    onDismiss: () -> Unit,
    onLog: (Long?) -> Unit,
    onRemove: () -> Unit
) {
    var selectedTypeId by remember { mutableStateOf<Long?>(existingLog?.fastingTypeId) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingLog != null) "Fast logged" else "Log a fast") },
        text = {
            if (existingLog != null) {
                val typeName = types.find { it.id == existingLog.fastingTypeId }?.name ?: "No specific reason"
                Text("Reason: $typeName")
            } else {
                Column {
                    Text("Reason (optional)", style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.textMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    TypeOptionRow(label = "No specific reason", selected = selectedTypeId == null) { selectedTypeId = null }
                    types.forEach { type ->
                        TypeOptionRow(label = type.name, selected = selectedTypeId == type.id) { selectedTypeId = type.id }
                    }
                }
            }
        },
        confirmButton = {
            if (existingLog != null) {
                TextButton(onClick = onRemove) {
                    Text("Remove", color = AppTheme.colors.danger)
                }
            } else {
                Button(
                    onClick = { onLog(selectedTypeId) },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.green, contentColor = AppTheme.colors.onGreen)
                ) {
                    Text("Log Fast")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun TypeOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (selected) AppTheme.colors.green else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (selected) Icon(Icons.Rounded.Check, contentDescription = null, tint = AppTheme.colors.onGreen, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.text)
    }
}

@Composable
private fun AddFastingTypeDialog(onDismiss: () -> Unit, onSave: (String, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var arabic by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Fasting Type") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = arabic, onValueChange = { arabic = it }, label = { Text("Arabic (Optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, arabic.ifBlank { null }) },
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
