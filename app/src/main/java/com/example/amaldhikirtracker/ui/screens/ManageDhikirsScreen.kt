package com.example.amaldhikirtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.amaldhikirtracker.data.local.entities.Dhikir
import com.example.amaldhikirtracker.ui.viewmodel.TrackerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageDhikirsScreen(
    viewModel: TrackerViewModel,
    onNavigateBack: () -> Unit
) {
    val allDhikirs by viewModel.allDhikirs.collectAsState()
    var showAddEditDialog by remember { mutableStateOf<Dhikir?>(null) }
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Dhikirs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                showAddEditDialog = Dhikir(name = "")
                isEditing = false
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Dhikir")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allDhikirs) { dhikir ->
                DhikirManagementItem(
                    dhikir = dhikir,
                    onEdit = { 
                        showAddEditDialog = dhikir
                        isEditing = true
                    },
                    onDelete = { viewModel.deleteDhikir(dhikir) }
                )
            }
        }

        if (showAddEditDialog != null) {
            AddEditDhikirDialog(
                dhikir = showAddEditDialog!!,
                isEditing = isEditing,
                onDismiss = { showAddEditDialog = null },
                onSave = { name, arabic, category ->
                    if (isEditing) {
                        viewModel.updateDhikir(showAddEditDialog!!.copy(name = name, arabicName = arabic, category = category))
                    } else {
                        viewModel.addDhikir(name, arabic, category)
                    }
                    showAddEditDialog = null
                }
            )
        }
    }
}

@Composable
fun DhikirManagementItem(
    dhikir: Dhikir,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = dhikir.name, style = MaterialTheme.typography.bodyLarge)
                if (!dhikir.arabicName.isNullOrEmpty()) {
                    Text(text = dhikir.arabicName, style = MaterialTheme.typography.bodyMedium)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            if (dhikir.isCustom) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AddEditDhikirDialog(
    dhikir: Dhikir,
    isEditing: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf(dhikir.name) }
    var arabicName by remember { mutableStateOf(dhikir.arabicName ?: "") }
    var category by remember { mutableStateOf(dhikir.category ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Dhikir" else "Add Dhikir") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = arabicName,
                    onValueChange = { arabicName = it },
                    label = { Text("Arabic Name (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onSave(name, arabicName.ifBlank { null }, category.ifBlank { null }) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
