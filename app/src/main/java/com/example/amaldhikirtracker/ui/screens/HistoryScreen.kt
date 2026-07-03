package com.example.amaldhikirtracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.amaldhikirtracker.data.local.entities.Dhikir
import com.example.amaldhikirtracker.data.local.entities.DhikirLog
import com.example.amaldhikirtracker.data.local.entities.SalatLog
import com.example.amaldhikirtracker.ui.viewmodel.HistoryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit,
    isTopLevel: Boolean = false
) {
    val state by viewModel.historyState.collectAsState()
    val dateRange by viewModel.dateRange.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spiritual Journey") },
                navigationIcon = {
                    if (!isTopLevel) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Analytics Range",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RangeChip(selected = dateRange == 7, label = "7D") { viewModel.setDateRange(7) }
                        RangeChip(selected = dateRange == 30, label = "30D") { viewModel.setDateRange(30) }
                    }
                }
            }

            item {
                SummaryStatsRow(state = state)
            }

            item {
                HistoryHeader(title = "Salat Completion %")
                Spacer(modifier = Modifier.height(8.dp))
                SalatCanvasChart(state = state, daysCount = dateRange)
            }

            item {
                HistoryHeader(title = "Dhikir Recitations")
                Spacer(modifier = Modifier.height(8.dp))
                DhikirCanvasChart(state = state, daysCount = dateRange)
            }

            item {
                HistoryHeader(title = "Detailed Activity Log")
            }

            val sortedDates = (state.salatLogs.keys + state.dhikirLogs.keys).distinct().sortedDescending()
            if (sortedDates.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No activity recorded yet.", style = MaterialTheme.typography.bodyMedium)
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
        }
    }
}

@Composable
fun RangeChip(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
fun SummaryStatsRow(state: HistoryViewModel.HistoryState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Salat Rate",
            value = "${(state.completionRate * 100).toInt()}%",
            icon = Icons.Rounded.Star,
            color = MaterialTheme.colorScheme.primaryContainer
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Total Dhikir",
            value = state.totalDhikirCount.toString(),
            icon = Icons.AutoMirrored.Rounded.ShowChart,
            color = MaterialTheme.colorScheme.secondaryContainer
        )
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun SalatCanvasChart(state: HistoryViewModel.HistoryState, daysCount: Int) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val days = (0 until daysCount).map { state.endDate.minusDays(it.toLong()) }.reversed()
    val data = days.map { date ->
        val logs = state.salatLogs[date] ?: emptyList()
        if (logs.isEmpty()) 0f else logs.count { it.isCompleted }.toFloat() / logs.size.toFloat()
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = size.width / (daysCount * 1.5f)
                val spacing = (size.width - (barWidth * daysCount)) / (daysCount + 1)
                val radius = 4.dp.toPx()

                data.forEachIndexed { index, ratio ->
                    val barHeight = size.height * ratio
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(spacing + index * (barWidth + spacing), size.height - barHeight),
                        size = Size(barWidth, barHeight.coerceAtLeast(2f)),
                        cornerRadius = CornerRadius(radius, radius)
                    )
                }
            }
        }
        if (daysCount <= 7) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { date ->
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("E")),
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun DhikirCanvasChart(state: HistoryViewModel.HistoryState, daysCount: Int) {
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val days = (0 until daysCount).map { state.endDate.minusDays(it.toLong()) }.reversed()
    val data = days.map { date ->
        state.dhikirLogs[date]?.sumOf { it.count }?.toFloat() ?: 0f
    }
    val maxVal = (data.maxOrNull() ?: 100f).coerceAtLeast(1f)

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = size.width / (daysCount * 1.5f)
                val spacing = (size.width - (barWidth * daysCount)) / (daysCount + 1)
                val radius = 4.dp.toPx()

                data.forEachIndexed { index, count ->
                    val barHeight = size.height * (count / maxVal)
                    drawRoundRect(
                        color = secondaryColor,
                        topLeft = Offset(spacing + index * (barWidth + spacing), size.height - barHeight),
                        size = Size(barWidth, barHeight.coerceAtLeast(2f)),
                        cornerRadius = CornerRadius(radius, radius)
                    )
                }
            }
        }
        if (daysCount <= 7) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { date ->
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("E")),
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun HistoryLogItem(
    date: LocalDate,
    salats: List<SalatLog>,
    dhikirs: List<DhikirLog>,
    dhikirMap: Map<Long, Dhikir>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (salats.isNotEmpty()) {
                val completedCount = salats.count { it.isCompleted }
                Text(
                    text = "Salat: $completedCount/${salats.size} completed",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            dhikirs.forEach { log ->
                val name = dhikirMap[log.dhikirId]?.name ?: "Dhikir"
                Text(
                    text = "$name: ${log.count}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
