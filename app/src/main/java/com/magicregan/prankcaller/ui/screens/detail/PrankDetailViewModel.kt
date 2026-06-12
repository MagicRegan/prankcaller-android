package com.magicregan.prankcaller.ui.screens.detail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magicregan.prankcaller.data.api.PrankCallerApi
import com.magicregan.prankcaller.data.model.CallRecord
import com.magicregan.prankcaller.data.model.CallStatus
import com.magicregan.prankcaller.data.model.Prank
import com.magicregan.prankcaller.data.repository.PrankRepository
import com.magicregan.prankcaller.service.PrankCallService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrankDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PrankRepository,
    private val api: PrankCallerApi
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

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    val credits = repository.credits

    val isLoggedIn: Boolean
        get() = api.isLoggedIn

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
        return _phoneNumber.value.length >= 7 && _recordingConsent.value
    }

    fun resetCallState() {
        _callState.value = CallState.Idle
    }

    fun startPrankCall(context: Context) {
        val currentPrank = _prank.value ?: return
        val number = _phoneNumber.value
        if (number.length < 7) {
            _callState.value = CallState.Error("Invalid phone number")
            return
        }

        if (!api.isLoggedIn) {
            _callState.value = CallState.NeedLogin
            return
        }

        val fullNumber = "${_countryCode.value}$number"
        _callState.value = CallState.Calling

        viewModelScope.launch {
            val result = api.startCall(
                prankId = currentPrank.id,
                callTo = fullNumber,
                callFrom = fullNumber,
                recordCall = false
            )

            result.fold(
                onSuccess = { response ->
                    _callState.value = CallState.Success(response.sidToken)

                    // Start background service for audio monitoring
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
                },
                onFailure = { error ->
                    _callState.value = CallState.Error(
                        error.message ?: "Call failed. Please try again."
                    )
                }
            )
        }
    }

    sealed class CallState {
        data object Idle : CallState()
        data object Calling : CallState()
        data object NeedLogin : CallState()
        data class Success(val sidToken: String) : CallState()
        data class Error(val message: String) : CallState()
    }
}
