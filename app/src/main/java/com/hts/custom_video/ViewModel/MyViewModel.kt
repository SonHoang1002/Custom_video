package com.hts.custom_video.ViewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MyViewModel : ViewModel() {

    private val _videoUri = mutableStateOf<String>("")
    private val _isMute = mutableStateOf<Boolean>(false)
    private val _isTrimVideo = mutableStateOf<Boolean>(true)

    private val _trimLeftDurationMs = mutableStateOf<Long>(0)
    private val _trimRightDurationMs = mutableStateOf<Long?>(null)
    private val _speed = mutableStateOf<Float>(1f)

    val getVideoUri: MutableState<String> get() = _videoUri
    val getIsMute: MutableState<Boolean> get() = _isMute
    val getIsTrimVideo: MutableState<Boolean> get() = _isTrimVideo
    val getTrimLeftDurationMs: MutableState<Long> get() = _trimLeftDurationMs
    val getTrimRightDurationMs: MutableState<Long?> get() = _trimRightDurationMs
    val getSpeed: MutableState<Float> get() = _speed

    val getVideoUriValue: String get() = _videoUri.value
    val getIsMuteValue: Boolean get() = _isMute.value
    val getIsTrimVideoValue: Boolean get() = _isTrimVideo.value
    val getTrimLeftDurationMsValue: Long get() = _trimLeftDurationMs.value
    val getTrimRightDurationMsValue: Long get() = _trimRightDurationMs.value?:getTrimLeftDurationMsValue
    val getSpeedValue: Float get() = _speed.value

    fun getBetweenDuration(): Long {
        return getTrimRightDurationMsValue.minus(getTrimLeftDurationMsValue)
    }
    fun setVideoUri(value: String) {
        _videoUri.value = value
    }

    fun setMute(value: Boolean) {
        _isMute.value = value
    }

    fun setTrimVideo(value: Boolean) {
        _isTrimVideo.value = value
    }

    fun setTrimLeftDurationMs(value: Long) {
        _trimLeftDurationMs.value = value
    }

    fun setTrimRightDurationMs(value: Long?) {
        _trimRightDurationMs.value = value
    }

    fun setSpeed(value: Float) {
        _speed.value = value
    }
}
