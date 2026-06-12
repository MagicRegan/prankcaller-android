package com.magicregan.prankcaller.ui.screens.call

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.magicregan.prankcaller.data.model.Prank
import com.magicregan.prankcaller.data.repository.PrankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PrankRepository,
    private val exoPlayer: ExoPlayer
) : ViewModel() {

    private val prankId: Int = savedStateHandle.get<Int>("prankId") ?: -1

    private val _prank = MutableStateFlow<Prank?>(null)
    val prank: StateFlow<Prank?> = _prank.asStateFlow()

    private val _isAudioPlaying = MutableStateFlow(false)
    val isAudioPlaying: StateFlow<Boolean> = _isAudioPlaying.asStateFlow()

    private val _callDuration = MutableStateFlow(0)
    val callDuration: StateFlow<Int> = _callDuration.asStateFlow()

    private val _audioProgress = MutableStateFlow(0f)
    val audioProgress: StateFlow<Float> = _audioProgress.asStateFlow()

    init {
        _prank.value = repository.getPrankById(prankId)
        startCallTimer()
    }

    private fun startCallTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _callDuration.value += 1
            }
        }
    }

    fun playPrankAudio(context: Context) {
        val currentPrank = _prank.value ?: return

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = true

        exoPlayer.stop()
        exoPlayer.setMediaItem(MediaItem.fromUri(currentPrank.previewUrl))
        exoPlayer.prepare()
        exoPlayer.play()
        _isAudioPlaying.value = true

        viewModelScope.launch {
            while (exoPlayer.isPlaying) {
                val duration = exoPlayer.duration.coerceAtLeast(1)
                _audioProgress.value = exoPlayer.currentPosition.toFloat() / duration.toFloat()
                delay(100)
            }
            _isAudioPlaying.value = false
            _audioProgress.value = 0f
        }
    }

    fun pauseAudio() {
        exoPlayer.pause()
        _isAudioPlaying.value = false
    }

    fun stopAudio() {
        exoPlayer.stop()
        _isAudioPlaying.value = false
        _audioProgress.value = 0f
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.stop()
    }

    fun formatDuration(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }
}
