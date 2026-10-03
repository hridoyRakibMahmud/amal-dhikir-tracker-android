package com.bindraft.amaldhikirtracker.ui.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bindraft.amaldhikirtracker.R
import com.bindraft.amaldhikirtracker.data.local.AuthProfile
import com.bindraft.amaldhikirtracker.data.local.PreferencesManager
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = CredentialManager.create(context).getCredential(context, request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).awaitResult()

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
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "Couldn't sign in to the backend")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            FirebaseAuth.getInstance().signOut()
            preferencesManager.clearAuthProfile()
        }
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

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
    addOnCanceledListener { continuation.cancel() }
}
