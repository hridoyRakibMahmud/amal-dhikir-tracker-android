package com.example.amaldhikirtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.amaldhikirtracker.data.local.entities.Dhikir
import com.example.amaldhikirtracker.data.local.entities.SalatLog
import com.example.amaldhikirtracker.ui.theme.AmalDhikirTrackerTheme
import com.example.amaldhikirtracker.ui.viewmodel.TrackerViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel,
    onNavigateToCounter: (Long) -> Unit
) {
    val todaySalats by viewModel.todaySalatLogs.collectAsState()
    val allDhikirs by viewModel.allDhikirs.collectAsState()
    val todayDhikirLogs by viewModel.todayDhikirLogs.collectAsState()
    val formattedDate by viewModel.formattedDate.collectAsState()

    var showAddSalatDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("Daily Tracker", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { showAddSalatDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Voluntary Salat", modifier = Modifier.size(36.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TrackerSectionHeader("Salat Prayers")
            }

            items(todaySalats) { salat ->
                SalatItem(salat = salat, onToggle = { viewModel.toggleSalat(salat) })
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                TrackerSectionHeader("Daily Dhikirs")
            }

            items(allDhikirs) { dhikir ->
                val log = todayDhikirLogs.find { it.dhikirId == dhikir.id }
                DhikirTrackerItem(
                    dhikir = dhikir,
                    count = log?.count ?: 0,
                    onItemClick = { onNavigateToCounter(dhikir.id) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
            }
        }

        if (showAddSalatDialog) {
            AddSalatDialog(
                onDismiss = { showAddSalatDialog = false },
                onAdd = { name ->
                    viewModel.addVoluntarySalat(name)
                    showAddSalatDialog = false
                }
            )
        }
    }
}

@Composable
private fun AddSalatDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Voluntary Salat") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Salat Name (e.g., Tahajjud, Duha)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onAdd(name) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TrackerSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SalatItem(salat: SalatLog, onToggle: () -> Unit) {
    Surface(
        onClick = onToggle,
        shape = MaterialTheme.shapes.extraLarge,
        color = if (salat.isCompleted) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = if (salat.isCompleted) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = salat.salatName, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = if (salat.isCompleted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = salat.type, 
                    style = MaterialTheme.typography.labelMedium,
                    color = if (salat.isCompleted) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = if (salat.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = if (salat.isCompleted) "Completed" else "Not Completed",
                tint = if (salat.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun DhikirTrackerItem(dhikir: Dhikir, count: Int, onItemClick: () -> Unit) {
    Surface(
        onClick = onItemClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dhikir.name, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold
                )
                if (!dhikir.arabicName.isNullOrEmpty()) {
                    Text(
                        text = dhikir.arabicName, 
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.secondaryContainer,
                tonalElevation = 2.dp
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun TrackerScreenPreview() {
    AmalDhikirTrackerTheme {
        Scaffold { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                TrackerSectionHeader("Salat Prayers")
                SalatItem(SalatLog(salatName = "Fajr", type = "FARD", isCompleted = true, date = LocalDate.now()), {})
                Spacer(modifier = Modifier.height(12.dp))
                SalatItem(SalatLog(salatName = "Dhuhr", type = "FARD", isCompleted = false, date = LocalDate.now()), {})
                Spacer(modifier = Modifier.height(24.dp))
                TrackerSectionHeader("Daily Dhikirs")
                DhikirTrackerItem(Dhikir(name = "SubhanAllah", arabicName = "سُبْحَانَ ٱللَّٰهِ"), 33, {})
            }
        }
    }
}
