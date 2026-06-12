package com.magicregan.prankcaller.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magicregan.prankcaller.data.api.PrankCallerApi
import com.magicregan.prankcaller.data.repository.PrankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val api: PrankCallerApi,
    private val repository: PrankRepository
) : ViewModel() {

    fun onTokenReceived(idToken: String, refreshToken: String, email: String, uid: String) {
        api.saveAuthTokens(idToken, refreshToken, email, uid)
        repository.initNewUserCredits()
        viewModelScope.launch {
            syncCredits()
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
