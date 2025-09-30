package com.himanshuhc.lincall.presentation.history

data class CallHistory(
    val phoneNumber: String,
    val callType: CallType,
    val timestamp: Long
)

sealed class CallType {
    object Incoming : CallType()
    object Outgoing : CallType()
    object Missed : CallType()
}