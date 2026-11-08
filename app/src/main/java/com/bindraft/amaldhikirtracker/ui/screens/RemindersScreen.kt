package com.bindraft.amaldhikirtracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.bindraft.amaldhikirtracker.R
import com.bindraft.amaldhikirtracker.data.local.PreferencesManager
import com.bindraft.amaldhikirtracker.data.local.ReminderSettings
import com.bindraft.amaldhikirtracker.reminder.ReminderKind
import com.bindraft.amaldhikirtracker.reminder.ReminderScheduler
import com.bindraft.amaldhikirtracker.ui.theme.AppTheme
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun RemindersScreen(
    preferencesManager: PreferencesManager,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dhikirSettings by preferencesManager.dhikirReminderSettings.collectAsState(initial = ReminderSettings())
    val quranSettings by preferencesManager.quranReminderSettings.collectAsState(initial = ReminderSettings())

    fun applyDhikir(settings: ReminderSettings) {
        scope.launch {
            preferencesManager.setDhikirReminderSettings(settings)
            ReminderScheduler.schedule(context, ReminderKind.DHIKIR, settings)
        }
    }

    fun applyQuran(settings: ReminderSettings) {
        scope.launch {
            preferencesManager.setQuranReminderSettings(settings)
            ReminderScheduler.schedule(context, ReminderKind.QURAN, settings)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 18.dp)) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = AppTheme.colors.text)
                }
                Text(
                    text = stringResource(R.string.settings_reminders_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.text
                )
            }
        }

        item { RemindersSectionHeader(stringResource(R.string.reminders_section_dhikir)) }
        item {
            ReminderCard(
                settings = dhikirSettings,
                onChange = ::applyDhikir,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        item { RemindersSectionHeader(stringResource(R.string.reminders_section_quran)) }
        item {
            ReminderCard(
                settings = quranSettings,
                onChange = ::applyQuran
            )
        }
    }
}

@Composable
private fun RemindersSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = AppTheme.colors.textMuted,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun ReminderCard(
    settings: ReminderSettings,
    onChange: (ReminderSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartPicker by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppTheme.colors.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.reminders_enable_label),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppTheme.colors.text,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = settings.enabled,
                    onCheckedChange = { onChange(settings.copy(enabled = it)) }
                )
            }

            if (settings.enabled) {
                HorizontalDivider(color = AppTheme.colors.border, thickness = 1.dp)
                TimeRow(
                    label = stringResource(R.string.reminders_start_time),
                    minutes = settings.startMinutes,
                    onClick = { showStartPicker = true }
                )
                Text(
                    text = stringResource(R.string.reminders_hourly_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.textMuted,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }

    if (showStartPicker) {
        TimePickerDialog(
            initialMinutes = settings.startMinutes,
            onDismiss = { showStartPicker = false },
            onPick = { minutes -> onChange(settings.copy(startMinutes = minutes)); showStartPicker = false }
        )
    }
}

@Composable
private fun TimeRow(label: String, minutes: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.text,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = formatMinutes(minutes),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.green
        )
    }
}

private fun formatMinutes(minutes: Int): String =
    LocalTime.of(minutes / 60, minutes % 60).format(DateTimeFormatter.ofPattern("h:mm a"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onPick: (Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialMinutes / 60,
        initialMinute = initialMinutes % 60,
        is24Hour = false
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = AppTheme.colors.surface) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = state)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
                    TextButton(onClick = { onPick(state.hour * 60 + state.minute) }) { Text(stringResource(R.string.common_ok)) }
                }
            }
        }
    }
}
