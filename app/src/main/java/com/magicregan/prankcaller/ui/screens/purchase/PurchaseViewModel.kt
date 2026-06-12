package com.magicregan.prankcaller.ui.screens.purchase

import androidx.lifecycle.ViewModel
import com.magicregan.prankcaller.data.repository.PrankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val repository: PrankRepository
) : ViewModel() {
    val credits = repository.credits

    fun addCredits(amount: Int) {
        repository.addCredits(amount)
    }
}
