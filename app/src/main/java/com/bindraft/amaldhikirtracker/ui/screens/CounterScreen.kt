package com.bindraft.amaldhikirtracker.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bindraft.amaldhikirtracker.ui.theme.AppTheme
import com.bindraft.amaldhikirtracker.ui.theme.NotoNaskhArabic
import com.bindraft.amaldhikirtracker.ui.viewmodel.CounterViewModel

@Composable
fun CounterScreen(
    viewModel: CounterViewModel,
    onNavigateBack: () -> Unit
) {
    val sessionCount by viewModel.sessionCount.collectAsState()
    val totalToday by viewModel.totalToday.collectAsState()
    val dhikir by viewModel.dhikir.collectAsState()
    val haptic = LocalHapticFeedback.current

    var showManualAdd by remember { mutableStateOf(false) }
    var editingTarget by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = AppTheme.colors.text)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = dhikir?.name ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.text
                )
                if (!dhikir?.arabicName.isNullOrEmpty()) {
                    Text(
                        text = dhikir?.arabicName!!,
                        fontFamily = NotoNaskhArabic,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.textMuted
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Today's total",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.textMuted
            )
            Text(
                text = totalToday.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.text
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(260.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(AppTheme.colors.green)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.increment()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sessionCount.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.onGreen
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            TargetSection(
                target = dhikir?.dailyTarget,
                todayCount = totalToday,
                editing = editingTarget,
                onStartEdit = { editingTarget = true },
                onSave = { newTarget ->
                    viewModel.setTarget(newTarget)
                    editingTarget = false
                },
                onCancel = { editingTarget = false }
            )

            Spacer(modifier = Modifier.height(20.dp))

            ManualAddSection(
                expanded = showManualAdd,
                onExpand = { showManualAdd = true },
                onCancel = { showManualAdd = false },
                onAdd = { amount ->
                    viewModel.addManual(amount)
                    showManualAdd = false
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.resetSession() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, AppTheme.colors.border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTheme.colors.text)
            ) {
                Text("Reset Session")
            }
            Button(
                onClick = {
                    viewModel.saveSession()
                    onNavigateBack()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.green, contentColor = AppTheme.colors.onGreen)
            ) {
                Text("Complete")
            }
        }
    }
}

@Composable
private fun TargetSection(
    target: Int?,
    todayCount: Int,
    editing: Boolean,
    onStartEdit: () -> Unit,
    onSave: (Int?) -> Unit,
    onCancel: () -> Unit
) {
    when {
        editing -> {
            var text by remember { mutableStateOf(target?.toString() ?: "") }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.all { c -> c.isDigit() }) text = it },
                    placeholder = { Text("e.g. 100") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onCancel) { Text("Cancel") }
                Button(
                    onClick = { onSave(text.toIntOrNull()) },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.green, contentColor = AppTheme.colors.onGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
            }
        }
        target != null && target > 0 -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { (todayCount.toFloat() / target).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onStartEdit() },
                    color = AppTheme.colors.green,
                    trackColor = AppTheme.colors.surface2
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$todayCount / $target today",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.colors.textMuted,
                    modifier = Modifier.clickable { onStartEdit() }
                )
            }
        }
        else -> {
            Surface(
                onClick = onStartEdit,
                color = androidx.compose.ui.graphics.Color.Transparent,
                border = BorderStroke(1.dp, AppTheme.colors.gold),
                shape = RoundedCornerShape(100.dp)
            ) {
                Text(
                    text = "Set a daily target",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = AppTheme.colors.gold
                )
            }
        }
    }
}

@Composable
private fun ManualAddSection(
    expanded: Boolean,
    onExpand: () -> Unit,
    onCancel: () -> Unit,
    onAdd: (Int) -> Unit
) {
    if (!expanded) {
        Row(
            modifier = Modifier.clickable(onClick = onExpand),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.EditNote,
                contentDescription = null,
                tint = AppTheme.colors.textMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Already counted some? Add manually",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.textMuted
            )
        }
    } else {
        var text by remember { mutableStateOf("") }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, AppTheme.colors.border), RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Add a count you already did outside the app",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.textMuted,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onCancel, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = "Cancel", tint = AppTheme.colors.textMuted)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.all { c -> c.isDigit() }) text = it },
                    placeholder = { Text("0") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val amount = text.toIntOrNull() ?: 0
                        if (amount > 0) onAdd(amount) else onCancel()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.green, contentColor = AppTheme.colors.onGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add")
                }
            }
        }
    }
}
