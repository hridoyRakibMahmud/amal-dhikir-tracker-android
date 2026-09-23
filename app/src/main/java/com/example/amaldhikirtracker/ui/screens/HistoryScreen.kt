package com.example.amaldhikirtracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amaldhikirtracker.data.local.entities.Dhikir
import com.example.amaldhikirtracker.data.local.entities.DhikirLog
import com.example.amaldhikirtracker.data.local.entities.SalatLog
import com.example.amaldhikirtracker.ui.theme.AppTheme
import com.example.amaldhikirtracker.ui.viewmodel.HistoryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit,
    isTopLevel: Boolean = false
) {
    val state by viewModel.historyState.collectAsState()
    val dateRange by viewModel.dateRange.collectAsState()

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
                            contentDescription = "Back",
                            tint = AppTheme.colors.text
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.text
                )
            }
        }

        item {
            DateRangeToggle(
                selectedDays = dateRange,
                onSelect = { viewModel.setDateRange(it) },
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
                    label = "Salah completion",
                    value = "${(state.completionRate * 100).toInt()}%"
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Total Dhikir",
                    value = state.totalDhikirCount.toString(),
                    valueColor = AppTheme.colors.gold
                )
            }
        }

        item {
            SectionLabel("SALAH COMPLETED / DAY")
            ChartCard {
                SalatBarChart(state = state, daysCount = dateRange)
            }
        }

        item {
            SectionLabel("DHIKIR LOGGED / DAY")
            ChartCard {
                DhikirBarChart(state = state, daysCount = dateRange)
            }
        }

        item {
            SectionLabel("DETAILED ACTIVITY LOG")
        }

        val sortedDates = (state.salatLogs.keys + state.dhikirLogs.keys).distinct().sortedDescending()
        if (sortedDates.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No activity recorded yet.",
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
private fun DateRangeToggle(selectedDays: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = AppTheme.colors.surface2,
        shape = RoundedCornerShape(100.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            listOf(7 to "7 days", 30 to "30 days").forEach { (days, label) ->
                val selected = selectedDays == days
                Surface(
                    onClick = { onSelect(days) },
                    color = if (selected) AppTheme.colors.surface else Color.Transparent,
                    shape = RoundedCornerShape(100.dp),
                    shadowElevation = if (selected) 1.dp else 0.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) AppTheme.colors.text else AppTheme.colors.textMuted
                    )
                }
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
private fun SalatBarChart(state: HistoryViewModel.HistoryState, daysCount: Int) {
    val barColor = AppTheme.colors.green
    val labelColor = AppTheme.colors.textMuted
    val days = (0 until daysCount).map { state.endDate.minusDays(it.toLong()) }.reversed()
    val data = days.map { date ->
        val logs = state.salatLogs[date] ?: emptyList()
        if (logs.isEmpty()) 0f else logs.count { it.isCompleted }.toFloat() / logs.size.toFloat()
    }
    BarChart(data = data, days = days, daysCount = daysCount, barColor = barColor, labelColor = labelColor)
}

@Composable
private fun DhikirBarChart(state: HistoryViewModel.HistoryState, daysCount: Int) {
    val barColor = AppTheme.colors.gold
    val labelColor = AppTheme.colors.textMuted
    val days = (0 until daysCount).map { state.endDate.minusDays(it.toLong()) }.reversed()
    val counts = days.map { date -> state.dhikirLogs[date]?.sumOf { it.count }?.toFloat() ?: 0f }
    val maxVal = (counts.maxOrNull() ?: 0f).coerceAtLeast(1f)
    BarChart(data = counts.map { it / maxVal }, days = days, daysCount = daysCount, barColor = barColor, labelColor = labelColor)
}

@Composable
private fun BarChart(data: List<Float>, days: List<LocalDate>, daysCount: Int, barColor: Color, labelColor: Color) {
    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val barWidth = size.width / (daysCount * 1.5f)
            val spacing = (size.width - (barWidth * daysCount)) / (daysCount + 1)
            val radius = 4.dp.toPx()

            data.forEachIndexed { index, ratio ->
                val barHeight = (size.height * ratio).coerceAtLeast(4f)
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(spacing + index * (barWidth + spacing), size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(radius, radius)
                )
            }
        }
        if (daysCount <= 7) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { date ->
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("E")).take(1),
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor
                    )
                }
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
                    text = "Salah: $completedCount/${salats.size} completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.textMuted
                )
            }
            dhikirs.forEach { log ->
                val name = dhikirMap[log.dhikirId]?.name ?: "Dhikir"
                Text(
                    text = "$name: ${log.count}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.gold
                )
            }
        }
    }
}
