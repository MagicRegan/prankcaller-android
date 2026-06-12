package com.magicregan.prankcaller.ui.screens.detail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.magicregan.prankcaller.data.model.CallRecord
import com.magicregan.prankcaller.data.model.CallStatus
import com.magicregan.prankcaller.data.model.Prank
import com.magicregan.prankcaller.data.repository.PrankRepository
import com.magicregan.prankcaller.service.PrankCallService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PrankDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PrankRepository
) : ViewModel() {

    private val prankId: Int = savedStateHandle.get<Int>("prankId") ?: -1

    private val _prank = MutableStateFlow<Prank?>(null)
    val prank: StateFlow<Prank?> = _prank.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _countryCode = MutableStateFlow("+1")
    val countryCode: StateFlow<String> = _countryCode.asStateFlow()

    private val _recordingConsent = MutableStateFlow(false)
    val recordingConsent: StateFlow<Boolean> = _recordingConsent.asStateFlow()

    val credits = repository.credits

    init {
        _prank.value = repository.getPrankById(prankId)
    }

    fun onPhoneNumberChange(number: String) {
        _phoneNumber.value = number
    }

    fun onCountryCodeChange(code: String) {
        _countryCode.value = code
    }

    fun onRecordingConsentChange(consent: Boolean) {
        _recordingConsent.value = consent
    }

    fun canStartCall(): Boolean {
        return _phoneNumber.value.length >= 7 &&
            repository.credits.value > 0 &&
            _recordingConsent.value
    }

    sealed class CallResult {
        data object Success : CallResult()
        data object NoCredits : CallResult()
        data object InvalidInput : CallResult()
    }

    fun startPrankCall(context: Context): CallResult {
        val currentPrank = _prank.value ?: return CallResult.InvalidInput
        val number = _phoneNumber.value
        if (number.length < 7) return CallResult.InvalidInput

        if (!repository.useCredit()) return CallResult.NoCredits

        val fullNumber = "${_countryCode.value}$number"

        PrankCallService.start(
            context = context,
            audioUrl = currentPrank.previewUrl,
            prankName = currentPrank.name,
            phoneNumber = fullNumber
        )

        repository.addCallRecord(
            CallRecord(
                prankName = currentPrank.name,
                prankImage = currentPrank.largeImage,
                phoneNumber = fullNumber,
                status = CallStatus.IN_PROGRESS
            )
        )

        return CallResult.Success
    }
}
