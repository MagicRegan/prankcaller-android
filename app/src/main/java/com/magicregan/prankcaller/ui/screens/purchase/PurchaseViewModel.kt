package com.magicregan.prankcaller.ui.screens.purchase

import android.content.Context
import android.content.Intent
import android.net.Uri
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

        // Open prankcaller.io purchase page in browser
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PURCHASE_URL))
        context.startActivity(intent)

        _isPurchasing.value = false

        Toast.makeText(
            context,
            "Complete purchase in browser, then credits will sync automatically",
            Toast.LENGTH_LONG
        ).show()
    }

    fun syncCreditsFromApi() {
        if (!api.isLoggedIn) return
        viewModelScope.launch {
            val result = api.getUserDetail()
            result.onSuccess { detail ->
                val apiCredits = detail.minutes + detail.freeCalls
                if (apiCredits > 0) {
                    repository.setCredits(apiCredits)
                }
            }
        }
    }

    fun addFreeCredits() {
        repository.addCredits(5)
    }

    companion object {
        private const val PURCHASE_URL = "https://prankcaller.io/get-tokens"
    }
}
