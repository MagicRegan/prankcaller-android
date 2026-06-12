package com.magicregan.prankcaller.ui.screens.detail

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.magicregan.prankcaller.data.model.CallRecord
import com.magicregan.prankcaller.data.model.CallStatus
import com.magicregan.prankcaller.data.model.Prank
import com.magicregan.prankcaller.data.repository.PrankRepository
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

    fun onRecordingConsentChange(consent: Boolean) {
        _recordingConsent.value = consent
    }

    fun canStartCall(): Boolean {
        return _phoneNumber.value.length >= 7 && repository.credits.value > 0
    }

    fun startPrankCall(context: Context): Boolean {
        val currentPrank = _prank.value ?: return false
        val number = _phoneNumber.value
        if (number.length < 7) return false

        if (!repository.useCredit()) return false

        repository.addCallRecord(
            CallRecord(
                prankName = currentPrank.name,
                prankImage = currentPrank.largeImage,
                phoneNumber = "${_countryCode.value}$number",
                status = CallStatus.IN_PROGRESS
            )
        )

        val fullNumber = "${_countryCode.value}$number"
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$fullNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.isSpeakerphoneOn = true
            context.startActivity(callIntent)
            return true
        } catch (e: SecurityException) {
            return false
        }
    }
}
