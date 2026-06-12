package com.magicregan.prankcaller.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magicregan.prankcaller.data.api.PrankCallerApi
import com.magicregan.prankcaller.data.repository.PrankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val api: PrankCallerApi,
    private val repository: PrankRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = api.register(email, password)
            result.onSuccess { auth ->
                repository.setCredits(auth.credits)
                _uiState.value = LoginUiState.Success
            }.onFailure { e ->
                _uiState.value = LoginUiState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = api.login(email, password)
            result.onSuccess { auth ->
                repository.setCredits(auth.credits)
                _uiState.value = LoginUiState.Success
            }.onFailure { e ->
                _uiState.value = LoginUiState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = LoginUiState.Idle
        }
    }
}
