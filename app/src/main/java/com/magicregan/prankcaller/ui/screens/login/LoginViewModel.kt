package com.magicregan.prankcaller.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magicregan.prankcaller.data.api.PrankCallerApi
import com.magicregan.prankcaller.data.repository.PrankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val api: PrankCallerApi,
    private val repository: PrankRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = api.login(email, password)
            _isLoading.value = false
            result.fold(
                onSuccess = {
                    syncCredits()
                    onResult(true, null)
                },
                onFailure = { onResult(false, it.message) }
            )
        }
    }

    fun register(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = api.register(email, password)
            _isLoading.value = false
            result.fold(
                onSuccess = {
                    repository.initNewUserCredits()
                    syncCredits()
                    onResult(true, null)
                },
                onFailure = { onResult(false, it.message) }
            )
        }
    }

    private suspend fun syncCredits() {
        val userDetail = api.getUserDetail()
        userDetail.onSuccess { detail ->
            val apiCredits = detail.minutes + detail.freeCalls
            if (apiCredits > 0) {
                repository.setCredits(apiCredits)
            }
        }
    }
}
