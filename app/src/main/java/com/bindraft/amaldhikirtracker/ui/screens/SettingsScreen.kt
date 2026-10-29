package com.bindraft.amaldhikirtracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bindraft.amaldhikirtracker.R
import com.bindraft.amaldhikirtracker.data.local.AppLanguage
import com.bindraft.amaldhikirtracker.data.local.CalendarType
import com.bindraft.amaldhikirtracker.data.local.PreferencesManager
import com.bindraft.amaldhikirtracker.data.local.ThemeMode
import com.bindraft.amaldhikirtracker.data.local.applyAppLanguage
import com.bindraft.amaldhikirtracker.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    preferencesManager: PreferencesManager,
    onNavigateToManageDhikirs: () -> Unit,
    onNavigateToFasting: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val calendarType by preferencesManager.calendarType.collectAsState(initial = CalendarType.HIJRI)
    val themeMode by preferencesManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val hijriOffset by preferencesManager.hijriDateOffset.collectAsState(initial = 0)
    val authProfile by preferencesManager.authProfile.collectAsState(initial = null)
    val appLanguage by preferencesManager.appLanguage.collectAsState(initial = AppLanguage.ENGLISH)
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.text,
                modifier = Modifier.padding(bottom = 18.dp)
            )
        }

        item { SettingsSectionHeader(stringResource(R.string.settings_section_profile)) }
        item {
            SettingsCard(modifier = Modifier.padding(bottom = 18.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToLogin)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.surface2),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Person, contentDescription = null, tint = AppTheme.colors.textMuted)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            authProfile?.displayName ?: stringResource(R.string.settings_guest),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.text
                        )
                        Text(
                            authProfile?.email ?: stringResource(R.string.settings_signin_prompt),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.textMuted
                        )
                    }
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = AppTheme.colors.textMuted)
                }
            }
        }

        item { SettingsSectionHeader(stringResource(R.string.settings_section_preferences)) }
        item {
            SettingsCard(modifier = Modifier.padding(bottom = 18.dp)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.settings_calendar_type),
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppTheme.colors.text,
                            modifier = Modifier.weight(1f)
                        )
                        SegmentedPill(
                            options = listOf(
                                CalendarType.HIJRI to stringResource(R.string.common_hijri),
                                CalendarType.GREGORIAN to stringResource(R.string.common_gregorian)
                            ),
                            selected = calendarType,
                            onSelect = { scope.launch { preferencesManager.setCalendarType(it) } }
                        )
                    }
                    HorizontalDivider(color = AppTheme.colors.border, thickness = 1.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.settings_appearance),
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppTheme.colors.text,
                            modifier = Modifier.weight(1f)
                        )
                        val isDark = themeMode == ThemeMode.DARK
                        AppearanceToggle(
                            isDark = isDark,
                            onToggle = {
                                scope.launch {
                                    preferencesManager.setThemeMode(if (isDark) ThemeMode.LIGHT else ThemeMode.DARK)
                                }
                            }
                        )
                    }
                    HorizontalDivider(color = AppTheme.colors.border, thickness = 1.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.settings_hijri_offset_title),
                                style = MaterialTheme.typography.bodyLarge,
                                color = AppTheme.colors.text
                            )
                            Text(
                                stringResource(R.string.settings_hijri_offset_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.colors.textMuted
                            )
                        }
                        OffsetStepper(
                            value = hijriOffset,
                            onChange = { newValue ->
                                scope.launch { preferencesManager.setHijriDateOffset(newValue.coerceIn(-3, 3)) }
                            }
                        )
                    }
                    HorizontalDivider(color = AppTheme.colors.border, thickness = 1.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.settings_language_title),
                                style = MaterialTheme.typography.bodyLarge,
                                color = AppTheme.colors.text
                            )
                            Text(
                                stringResource(R.string.settings_language_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.colors.textMuted
                            )
                        }
                        SegmentedPill(
                            options = listOf(AppLanguage.ENGLISH to "English", AppLanguage.BANGLA to "বাংলা"),
                            selected = appLanguage,
                            onSelect = { language ->
                                scope.launch { preferencesManager.setAppLanguage(language) }
                                applyAppLanguage(language)
                            }
                        )
                    }
                }
            }
        }

        item { SettingsSectionHeader(stringResource(R.string.settings_section_content)) }
        item {
            SettingsCard(modifier = Modifier.padding(bottom = 18.dp)) {
                SettingsMenuItem(
                    title = stringResource(R.string.settings_manage_dhikirs_title),
                    subtitle = stringResource(R.string.settings_manage_dhikirs_subtitle),
                    icon = Icons.AutoMirrored.Rounded.List,
                    onClick = onNavigateToManageDhikirs
                )
                HorizontalDivider(color = AppTheme.colors.border, thickness = 1.dp)
                SettingsMenuItem(
                    title = stringResource(R.string.settings_fasting_title),
                    subtitle = stringResource(R.string.settings_fasting_subtitle),
                    icon = Icons.Rounded.CalendarMonth,
                    onClick = onNavigateToFasting
                )
                HorizontalDivider(color = AppTheme.colors.border, thickness = 1.dp)
                SettingsMenuItem(
                    title = stringResource(R.string.settings_quran_title),
                    subtitle = stringResource(R.string.settings_quran_subtitle),
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    onClick = onNavigateToQuran
                )
            }
        }

        item { SettingsSectionHeader(stringResource(R.string.settings_section_about)) }
        item {
            SettingsCard(modifier = Modifier.padding(bottom = 18.dp)) {
                SettingsMenuItem(title = stringResource(R.string.settings_app_version), subtitle = "1.0.0", icon = null, onClick = {})
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = AppTheme.colors.textMuted,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun SettingsCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppTheme.colors.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Column(content = content)
    }
}

@Composable
private fun <T> SegmentedPill(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Surface(color = AppTheme.colors.surface2, shape = RoundedCornerShape(100.dp)) {
        Row(modifier = Modifier.padding(3.dp)) {
            options.forEach { (value, label) ->
                val isSelected = value == selected
                Surface(
                    onClick = { onSelect(value) },
                    color = if (isSelected) AppTheme.colors.surface else Color.Transparent,
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) AppTheme.colors.text else AppTheme.colors.textMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun AppearanceToggle(isDark: Boolean, onToggle: () -> Unit) {
    Surface(
        onClick = onToggle,
        color = AppTheme.colors.surface2,
        shape = RoundedCornerShape(100.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                contentDescription = null,
                tint = AppTheme.colors.text,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isDark) "Dark" else "Light",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.text
            )
        }
    }
}

@Composable
private fun OffsetStepper(value: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onChange(value - 1) }, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Rounded.Remove, contentDescription = stringResource(R.string.common_decrease), tint = AppTheme.colors.text, modifier = Modifier.size(16.dp))
        }
        Text(
            text = if (value > 0) "+$value" else value.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.text,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 24.dp)
        )
        IconButton(onClick = { onChange(value + 1) }, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.common_increase), tint = AppTheme.colors.text, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun SettingsMenuItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = AppTheme.colors.textMuted, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = AppTheme.colors.text)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.textMuted)
            }
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = AppTheme.colors.textMuted.copy(alpha = 0.5f))
    }
}
