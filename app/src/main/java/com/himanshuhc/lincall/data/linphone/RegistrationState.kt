package com.himanshuhc.lincall.data.linphone

sealed class RegistrationState {

    object None : RegistrationState()
    object Progress : RegistrationState()
    object Ok : RegistrationState()
    object Cleared : RegistrationState()
    data class Failed(val message: String?) : RegistrationState()

}