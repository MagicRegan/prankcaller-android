package com.magicregan.prankcaller.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.magicregan.prankcaller.data.api.PrankCallerApi
import com.magicregan.prankcaller.data.repository.PrankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    repository: PrankRepository,
    private val api: PrankCallerApi
) : ViewModel() {
    val credits = repository.credits

    val isLoggedIn: Boolean
        get() = api.isLoggedIn

    val userEmail: String?
        get() = api.userEmail

    fun logout() {
        api.logout()
    }
}
