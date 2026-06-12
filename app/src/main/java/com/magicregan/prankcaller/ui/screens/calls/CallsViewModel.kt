package com.magicregan.prankcaller.ui.screens.calls

import androidx.lifecycle.ViewModel
import com.magicregan.prankcaller.data.repository.PrankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CallsViewModel @Inject constructor(
    repository: PrankRepository
) : ViewModel() {
    val callHistory = repository.callHistory
}
