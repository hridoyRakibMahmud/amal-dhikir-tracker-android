package com.example.amaldhikirtracker.ui.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.amaldhikirtracker.data.local.AuthProfile
import com.example.amaldhikirtracker.data.local.PreferencesManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * OAuth 2.0 Web Client ID from Google Cloud Console (APIs & Services > Credentials),
 * required by Credential Manager's Google ID request. Sign-in will fail until this is
 * replaced with a real ID registered against this app's package name + SHA-1.
 */
const val GOOGLE_WEB_CLIENT_ID = "REPLACE_WITH_YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {

    val authProfile: StateFlow<AuthProfile?> = preferencesManager.authProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signIn(context: Context) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(GOOGLE_WEB_CLIENT_ID)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = CredentialManager.create(context).getCredential(context, request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    preferencesManager.setAuthProfile(
                        AuthProfile(
                            displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.id,
                            email = googleIdTokenCredential.id,
                            photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                        )
                    )
                    _uiState.value = LoginUiState.Idle
                } else {
                    _uiState.value = LoginUiState.Error("Unexpected credential type from Google")
                }
            } catch (e: GetCredentialException) {
                _uiState.value = LoginUiState.Error(e.message ?: "Sign-in was cancelled or failed")
            } catch (e: GoogleIdTokenParsingException) {
                _uiState.value = LoginUiState.Error("Couldn't parse the Google credential")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch { preferencesManager.clearAuthProfile() }
    }

    class Factory(private val preferencesManager: PreferencesManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(preferencesManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
