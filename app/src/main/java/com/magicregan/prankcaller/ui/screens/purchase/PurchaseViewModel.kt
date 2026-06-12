package com.magicregan.prankcaller.ui.screens.purchase

import android.content.Context
import android.widget.Toast
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
class PurchaseViewModel @Inject constructor(
    private val repository: PrankRepository,
    private val api: PrankCallerApi
) : ViewModel() {
    val credits = repository.credits

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    val isLoggedIn: Boolean
        get() = api.isLoggedIn

    fun purchaseCredits(context: Context, creditPackage: CreditPackage) {
        if (!api.isLoggedIn) {
            Toast.makeText(context, "Please log in first", Toast.LENGTH_SHORT).show()
            return
        }

        _isPurchasing.value = true

        viewModelScope.launch {
            val result = api.addCredits(creditPackage.credits)
            result.onSuccess { newTotal ->
                repository.setCredits(newTotal)
                Toast.makeText(
                    context,
                    "${creditPackage.credits} credits added! Total: $newTotal",
                    Toast.LENGTH_SHORT
                ).show()
            }.onFailure { e ->
                Toast.makeText(
                    context,
                    "Failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            _isPurchasing.value = false
        }
    }

    fun syncCreditsFromApi() {
        if (!api.isLoggedIn) return
        viewModelScope.launch {
            val result = api.getUserDetail()
            result.onSuccess { detail ->
                val apiCredits = detail.minutes + detail.freeCalls
                if (apiCredits >= 0) {
                    repository.setCredits(apiCredits)
                }
            }
        }
    }

    fun addFreeCredits() {
        if (!api.isLoggedIn) {
            repository.addCredits(5)
            return
        }
        viewModelScope.launch {
            val result = api.addCredits(5)
            result.onSuccess { newTotal ->
                repository.setCredits(newTotal)
            }.onFailure {
                repository.addCredits(5)
            }
        }
    }
}
