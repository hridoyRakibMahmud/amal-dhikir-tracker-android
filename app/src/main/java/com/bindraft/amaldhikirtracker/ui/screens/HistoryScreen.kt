package com.bindraft.amaldhikirtracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bindraft.amaldhikirtracker.R
import com.bindraft.amaldhikirtracker.data.local.entities.Dhikir
import com.bindraft.amaldhikirtracker.data.local.entities.DhikirLog
import com.bindraft.amaldhikirtracker.data.local.entities.SalatLog
import com.bindraft.amaldhikirtracker.ui.theme.AppTheme
import com.bindraft.amaldhikirtracker.ui.viewmodel.HistoryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit,
    isTopLevel: Boolean = false
) {
    val state by viewModel.historyState.collectAsState()
    val weekNumber by viewModel.weekNumber.collectAsState()
    val weekYear by viewModel.weekYear.collectAsState()
    val allDhikirs by viewModel.allDhikirs.collectAsState()
    val selectedDhikirId by viewModel.selectedDhikirId.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 18.dp)) {
                if (!isTopLevel) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = AppTheme.colors.text
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = stringResource(R.string.nav_history),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.text
                )
            }
        }

        item {
            WeekNavigator(
                weekLabel = stringResource(R.string.quran_week_label, weekNumber, weekYear),
                onPrevious = viewModel::previousWeek,
                onNext = viewModel::nextWeek,
                modifier = Modifier.padding(bottom = 18.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.history_salah_completion),
                    value = "${(state.completionRate * 100).toInt()}%"
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.history_total_dhikir),
                    value = state.totalDhikirCount.toString(),
                    valueColor = AppTheme.colors.gold
                )
            }
        }

        item {
            SectionLabel(stringResource(R.string.history_section_salah_chart))
            ChartCard {
                SalatBarChart(state = state)
            }
        }

        item {
            SectionLabel(stringResource(R.string.history_section_dhikir_chart))
            DhikirDropdown(
                dhikirs = allDhikirs,
                selectedDhikirId = selectedDhikirId,
                onSelect = viewModel::selectDhikir,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            ChartCard {
                DhikirBarChart(state = state, selectedDhikirId = selectedDhikirId)
            }
        }

        item {
            SectionLabel(stringResource(R.string.history_section_activity_log))
        }

        val sortedDates = (state.salatLogs.keys + state.dhikirLogs.keys).distinct().sortedDescending()
        if (sortedDates.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.textMuted
                    )
                }
            }
        } else {
            items(sortedDates) { date ->
                HistoryLogItem(
                    date = date,
                    salats = state.salatLogs[date] ?: emptyList(),
                    dhikirs = state.dhikirLogs[date] ?: emptyList(),
                    dhikirMap = state.dhikirMap
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun WeekNavigator(weekLabel: String, onPrevious: () -> Unit, onNext: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DhikirDropdown(
    dhikirs: List<Dhikir>,
    selectedDhikirId: Long?,
    onSelect: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = dhikirs.find { it.id == selectedDhikirId }?.name ?: stringResource(R.string.history_dhikir_all)
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.history_dhikir_filter_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.history_dhikir_all)) },
                onClick = { onSelect(null); expanded = false }
            )
            dhikirs.forEach { dhikir ->
                DropdownMenuItem(
                    text = { Text(dhikir.name) },
                    onClick = { onSelect(dhikir.id); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, label: String, value: String, valueColor: Color = AppTheme.colors.text) {
    Surface(
        modifier = modifier,
        color = AppTheme.colors.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.textMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = AppTheme.colors.textMuted,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun ChartCard(content: @Composable () -> Unit) {
    Surface(
        color = AppTheme.colors.surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, AppTheme.colors.border),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 22.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun SalatBarChart(state: HistoryViewModel.HistoryState) {
    val days = (0 until 7).map { state.startDate.plusDays(it.toLong()) }
    val data = days.map { date ->
        val logs = state.salatLogs[date] ?: emptyList()
        date to (if (logs.isEmpty()) 0 else logs.count { it.isCompleted })
    }
    val labels = days.associateWith { date ->
        val logs = state.salatLogs[date] ?: emptyList()
        "${logs.count { it.isCompleted }}/${logs.size}"
    }
    WeeklyBarChart(data = data, barColor = AppTheme.colors.green, maxDate = state.endDate, labelOverride = labels)
}

@Composable
private fun DhikirBarChart(state: HistoryViewModel.HistoryState, selectedDhikirId: Long?) {
    val days = (0 until 7).map { state.startDate.plusDays(it.toLong()) }
    val data = days.map { date ->
        val logs = state.dhikirLogs[date] ?: emptyList()
        val count = if (selectedDhikirId != null) {
            logs.filter { it.dhikirId == selectedDhikirId }.sumOf { it.count }
        } else {
            logs.sumOf { it.count }
        }
        date to count
    }
    WeeklyBarChart(data = data, barColor = AppTheme.colors.gold, maxDate = state.endDate)
}

/** One bar per day with its value labeled above it, matching the Quran reading chart's design. */
@Composable
private fun WeeklyBarChart(
    data: List<Pair<LocalDate, Int>>,
    barColor: Color,
    maxDate: LocalDate,
    labelOverride: Map<LocalDate, String>? = null
) {
    val labelColor = AppTheme.colors.textMuted
    val trackHeight = 120.dp
    val maxVal = (data.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1).toFloat()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().height(trackHeight + 22.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            data.forEach { (date, value) ->
                val future = date.isAfter(maxDate)
                val ratio = (value / maxVal).coerceIn(0f, 1f)
                val barHeight = if (value > 0) (trackHeight * ratio).coerceAtLeast(6.dp) else 2.dp

                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = labelOverride?.get(date) ?: value.toString(),
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

@Composable
private fun HistoryLogItem(
    date: LocalDate,
    salats: List<SalatLog>,
    dhikirs: List<DhikirLog>,
    dhikirMap: Map<Long, Dhikir>
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        color = AppTheme.colors.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.text
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (salats.isNotEmpty()) {
                val completedCount = salats.count { it.isCompleted }
                Text(
                    text = stringResource(R.string.history_salah_summary, completedCount, salats.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.textMuted
                )
            }
            dhikirs.forEach { log ->
                val name = dhikirMap[log.dhikirId]?.name ?: stringResource(R.string.common_dhikir_fallback)
                Text(
                    text = "$name: ${log.count}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.gold
                )
            }
        }
    }
}
