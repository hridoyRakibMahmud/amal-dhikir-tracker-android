package com.bindraft.amaldhikirtracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bindraft.amaldhikirtracker.R
import com.bindraft.amaldhikirtracker.data.local.entities.QuranMode
import com.bindraft.amaldhikirtracker.ui.theme.AppTheme
import com.bindraft.amaldhikirtracker.ui.viewmodel.QuranViewModel
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun QuranScreen(
    viewModel: QuranViewModel,
    onNavigateBack: () -> Unit
) {
    val modes by viewModel.modes.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val todayAyahs by viewModel.todayAyahsForSelected.collectAsState()
    val weekDailyAyahs by viewModel.weekDailyAyahs.collectAsState()
    val weekNumber by viewModel.weekNumber.collectAsState()
    val weekYear by viewModel.weekYear.collectAsState()
    val spiritualDate by viewModel.spiritualDate.collectAsState()
    var editingTarget by remember { mutableStateOf(false) }
    var ayahsText by remember { mutableStateOf("") }
    var logDate by remember { mutableStateOf<LocalDate?>(null) }
    var showLogDatePicker by remember { mutableStateOf(false) }
    var editingDate by remember { mutableStateOf<LocalDate?>(null) }

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
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = AppTheme.colors.text)
            }
            Text(
                text = stringResource(R.string.settings_quran_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.text
            )
        }

        if (selectedMode != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp)
            ) {
                QuranCard(
                    modes = modes,
                    selectedMode = selectedMode!!,
                    onSelectMode = { viewModel.selectMode(it.id); editingTarget = false },
                    todayAyahs = todayAyahs,
                    editingTarget = editingTarget,
                    onStartEditTarget = { editingTarget = true },
                    onCancelEditTarget = { editingTarget = false },
                    onSaveTarget = { target ->
                        viewModel.setTarget(selectedMode!!.id, target)
                        editingTarget = false
                    },
                    onClearTarget = { viewModel.setTarget(selectedMode!!.id, null) },
                    ayahsText = ayahsText,
                    onAyahsTextChange = { ayahsText = it },
                    logDate = logDate ?: spiritualDate,
                    isLogDateToday = (logDate ?: spiritualDate) == spiritualDate,
                    onChangeLogDate = { showLogDatePicker = true },
                    onAddAyahs = {
                        ayahsText.toIntOrNull()?.let { amount ->
                            if (amount > 0) {
                                viewModel.addAyahs(selectedMode!!.id, amount, logDate ?: spiritualDate)
                                ayahsText = ""
                                logDate = null
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                WeekNavigator(
                    weekLabel = stringResource(R.string.quran_week_label, weekNumber, weekYear),
                    onPrevious = viewModel::previousWeek,
                    onNext = viewModel::nextWeek
                )
                ChartCard {
                    AyahsBarChart(
                        data = weekDailyAyahs,
                        maxDate = spiritualDate,
                        onBarClick = { date, _ -> editingDate = date }
                    )
                }
            }
        }
    }

    if (showLogDatePicker) {
        DateOnlyPickerDialog(
            initialDate = logDate ?: spiritualDate,
            maxDate = spiritualDate,
            onDismiss = { showLogDatePicker = false },
            onPick = { picked -> logDate = picked; showLogDatePicker = false }
        )
    }

    val dateBeingEdited = editingDate
    if (dateBeingEdited != null && selectedMode != null) {
        EditAyahsDialog(
            modeName = selectedMode!!.name,
            date = dateBeingEdited,
            currentAyahs = weekDailyAyahs.find { it.first == dateBeingEdited }?.second ?: 0,
            onDismiss = { editingDate = null },
            onSave = { amount ->
                viewModel.editAyahs(selectedMode!!.id, dateBeingEdited, amount)
                editingDate = null
            }
        )
    }
}

@Composable
private fun QuranCard(
    modes: List<QuranMode>,
    selectedMode: QuranMode,
    onSelectMode: (QuranMode) -> Unit,
    todayAyahs: Int,
    editingTarget: Boolean,
    onStartEditTarget: () -> Unit,
    onCancelEditTarget: () -> Unit,
    onSaveTarget: (Int?) -> Unit,
    onClearTarget: () -> Unit,
    ayahsText: String,
    onAyahsTextChange: (String) -> Unit,
    logDate: LocalDate,
    isLogDateToday: Boolean,
    onChangeLogDate: () -> Unit,
    onAddAyahs: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppTheme.colors.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ModeDropdown(modes = modes, selectedMode = selectedMode, onSelectMode = onSelectMode)

            Spacer(modifier = Modifier.height(14.dp))

            val target = selectedMode.dailyTarget
            if (target != null && target > 0) {
                LinearProgressIndicator(
                    progress = { (todayAyahs.toFloat() / target).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                    color = AppTheme.colors.green,
                    trackColor = AppTheme.colors.surface2
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.quran_target_progress, todayAyahs, target),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.colors.textMuted
                )
            } else {
                Text(
                    text = stringResource(R.string.quran_today_no_target, todayAyahs),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.green
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.quran_log_section_title),
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Top) {
                OutlinedTextField(
                    value = ayahsText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) onAyahsTextChange(it) },
                    label = { Text(stringResource(R.string.quran_ayahs_label)) },
                    placeholder = { Text(stringResource(R.string.quran_ayahs_placeholder)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                DateField(
                    label = stringResource(R.string.common_date_label),
                    valueText = if (isLogDateToday) stringResource(R.string.common_today) else logDate.format(DateTimeFormatter.ofPattern("d MMM")),
                    onClick = onChangeLogDate,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onAddAyahs,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.green, contentColor = AppTheme.colors.onGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(R.string.common_add))
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = AppTheme.colors.border, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            if (editingTarget) {
                var text by remember(selectedMode.id) { mutableStateOf(selectedMode.dailyTarget?.toString() ?: "") }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { if (it.all { c -> c.isDigit() }) text = it },
                        placeholder = { Text(stringResource(R.string.common_target_placeholder)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onCancelEditTarget) { Text(stringResource(R.string.common_cancel)) }
                    TextButton(onClick = { onSaveTarget(text.toIntOrNull()) }) {
                        Text(stringResource(R.string.common_save), color = AppTheme.colors.gold, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                val target = selectedMode.dailyTarget
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (target != null && target > 0) {
                        Text(
                            text = stringResource(R.string.quran_edit_limit),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.gold,
                            modifier = Modifier.clickable(onClick = onStartEditTarget)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.common_clear),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.gold,
                            modifier = Modifier.clickable(onClick = onClearTarget)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.quran_set_limit),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeDropdown(
    modes: List<QuranMode>,
    selectedMode: QuranMode,
    onSelectMode: (QuranMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedMode.name,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.quran_reading_type_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            modes.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.name) },
                    onClick = {
                        onSelectMode(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** Styled like a text field, but the whole area (not just a small icon) is a tap target — opens the date picker. */
@Composable
private fun DateField(label: String, valueText: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = AppTheme.colors.background,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.textMuted)
                Text(text = valueText, style = MaterialTheme.typography.bodyLarge, color = AppTheme.colors.text)
            }
            Icon(Icons.Rounded.CalendarMonth, contentDescription = stringResource(R.string.common_change_date), tint = AppTheme.colors.gold)
        }
    }
}

@Composable
private fun WeekNavigator(weekLabel: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Rounded.ChevronLeft, contentDescription = stringResource(R.string.quran_prev_week), tint = AppTheme.colors.text)
        }
        Text(
            text = weekLabel,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.text
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = stringResource(R.string.quran_next_week), tint = AppTheme.colors.text)
        }
    }
}

@Composable
private fun ChartCard(content: @Composable () -> Unit) {
    Surface(
        color = AppTheme.colors.surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, AppTheme.colors.border),
        modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) { content() }
    }
}

/**
 * One bar per day with its ayah count labeled above it. Past/today bars are tappable — opens an
 * edit dialog for that day (see [EditAyahsDialog]); future-week bars (visible once the week
 * navigator pages ahead) are dimmed and inert, since there's nothing to log yet.
 */
@Composable
private fun AyahsBarChart(data: List<Pair<LocalDate, Int>>, maxDate: LocalDate, onBarClick: (LocalDate, Int) -> Unit) {
    val barColor = AppTheme.colors.gold
    val labelColor = AppTheme.colors.textMuted
    val trackHeight = 120.dp
    val maxVal = (data.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1).toFloat()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().height(trackHeight + 22.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            data.forEach { (date, ayahs) ->
                val future = date.isAfter(maxDate)
                val ratio = (ayahs / maxVal).coerceIn(0f, 1f)
                val barHeight = if (ayahs > 0) (trackHeight * ratio).coerceAtLeast(6.dp) else 2.dp

                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = ayahs.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (future) labelColor.copy(alpha = 0.4f) else labelColor,
                        modifier = Modifier.padding(bottom = barHeight + 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeight)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (future) barColor.copy(alpha = 0.25f) else barColor)
                            .then(if (future) Modifier else Modifier.clickable { onBarClick(date, ayahs) })
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            data.forEach { (date, _) ->
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("E")).take(1),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateOnlyPickerDialog(
    initialDate: LocalDate,
    maxDate: LocalDate,
    onDismiss: () -> Unit,
    onPick: (LocalDate) -> Unit
) {
    val maxMillis = maxDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= maxMillis
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    onPick(LocalDate.ofEpochDay(millis / 86_400_000L))
                } ?: onDismiss()
            }) { Text(stringResource(R.string.common_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    ) {
        DatePicker(state = datePickerState, showModeToggle = false)
    }
}

/** Opened by tapping a chart bar. Pre-filled with that day's current total; Save overwrites it (0 clears it). */
@Composable
private fun EditAyahsDialog(
    modeName: String,
    date: LocalDate,
    currentAyahs: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var text by remember(date) { mutableStateOf(if (currentAyahs > 0) currentAyahs.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(modeName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.textMuted
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.all { c -> c.isDigit() }) text = it },
                    label = { Text(stringResource(R.string.quran_edit_total_label)) },
                    placeholder = { Text(stringResource(R.string.quran_ayahs_placeholder)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(text.toIntOrNull() ?: 0) },
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.green, contentColor = AppTheme.colors.onGreen)
            ) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}
