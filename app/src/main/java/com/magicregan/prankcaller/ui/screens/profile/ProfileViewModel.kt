package com.magicregan.prankcaller.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.magicregan.prankcaller.data.api.PrankCallerApi
import com.magicregan.prankcaller.data.repository.PrankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    repository: PrankRepository,
    private val api: PrankCallerApi
) : ViewModel() {
    val credits = repository.credits

    private val _isLoggedIn = MutableStateFlow(api.isLoggedIn)
    val isLoggedInState: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    val isLoggedIn: Boolean
        get() = api.isLoggedIn

    val userEmail: String?
        get() = api.userEmail

    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut.asStateFlow()

    fun logout() {
        api.logout()
        _isLoggedIn.value = false
        _loggedOut.value = true
    }
}
