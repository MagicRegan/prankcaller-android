package com.magicregan.prankcaller.data.model

data class CallRecord(
    val id: Long = System.currentTimeMillis(),
    val prankName: String,
    val prankImage: String,
    val phoneNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val status: CallStatus = CallStatus.COMPLETED
)

enum class CallStatus {
    COMPLETED,
    MISSED,
    FAILED,
    IN_PROGRESS
}
