package com.magicregan.prankcaller.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.magicregan.prankcaller.data.repository.PrankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    repository: PrankRepository
) : ViewModel() {
    val credits = repository.credits
}
