package com.example.amaldhikirtracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.amaldhikirtracker.data.local.PreferencesManager
import com.example.amaldhikirtracker.ui.theme.AppTheme
import com.example.amaldhikirtracker.ui.viewmodel.LoginUiState
import com.example.amaldhikirtracker.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    preferencesManager: PreferencesManager,
    onNavigateBack: () -> Unit
) {
    val viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory(preferencesManager))
    val context = LocalContext.current
    val authProfile by viewModel.authProfile.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
    ) {
        IconButton(onClick = onNavigateBack, modifier = Modifier.padding(8.dp)) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = AppTheme.colors.text)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.greenTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.NightsStay,
                    contentDescription = null,
                    tint = AppTheme.colors.green,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (authProfile != null) {
                Text(
                    text = "Signed in as",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.textMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = authProfile!!.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.text,
                    textAlign = TextAlign.Center
                )
                if (!authProfile!!.email.isNullOrEmpty()) {
                    Text(
                        text = authProfile!!.email!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.textMuted
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
                OutlinedButton(
                    onClick = { viewModel.signOut() },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AppTheme.colors.border),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTheme.colors.danger)
                ) {
                    Text("Sign out")
                }
            } else {
                Text(
                    text = "Welcome back",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.text,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your prayers and dhikir are already saved on this device. Sign in to back them up and sync across devices.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.textMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(28.dp))

                OutlinedButton(
                    onClick = { viewModel.signIn(context) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AppTheme.colors.border),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppTheme.colors.text),
                    enabled = uiState !is LoginUiState.Loading
                ) {
                    if (uiState is LoginUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = AppTheme.colors.text)
                    } else {
                        // Placeholder mark — swap for Google's official multicolor "G" asset
                        // (Google Identity branding guidelines) before shipping.
                        Icon(Icons.Rounded.AccountCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Sign in with Google")
                    }
                }

                if (uiState is LoginUiState.Error) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = (uiState as LoginUiState.Error).message,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.danger,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
