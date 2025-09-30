package com.himanshuhc.lincall.data.linphone

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.himanshuhc.lincall.presentation.history.CallHistory
import com.himanshuhc.lincall.presentation.history.CallType
import org.linphone.core.Call
import org.linphone.core.CallParams
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.ProxyConfig
import org.linphone.core.TransportType

object LinphoneManager {

    var core: Core? = null
    private var coreListener: CoreListenerStub? = null

    var registrationStateListener: ((RegistrationState) -> Unit)? = null
    var incomingCallListener: ((Call?) -> Unit)? = null
    private val callHistory = mutableListOf<CallHistory>()

    /**
     * Initialize Linphone Core.
     * Call this once in Application.onCreate() or MainActivity.onCreate().
     */
    fun init(context: Context) {
        if (core != null) return // already initialized

        val factory = Factory.instance()
        factory.setDebugMode(true, "LinCall")

        core = factory.createCore(null, null, context.applicationContext)

        core!!.start();

        coreListener = object : CoreListenerStub() {
            override fun onRegistrationStateChanged(
                lc: Core,
                cfg: ProxyConfig,
                state: org.linphone.core.RegistrationState,
                message: String
            ) {
                val mapped = when (state) {
                    org.linphone.core.RegistrationState.None -> RegistrationState.None
                    org.linphone.core.RegistrationState.Progress -> RegistrationState.Progress
                    org.linphone.core.RegistrationState.Ok -> RegistrationState.Ok
                    org.linphone.core.RegistrationState.Cleared -> RegistrationState.Cleared
                    org.linphone.core.RegistrationState.Failed -> RegistrationState.Failed(message)
                    else -> RegistrationState.None
                }
                registrationStateListener?.invoke(mapped)
            }

            override fun onCallStateChanged(
                lc: Core,
                call: Call,
                state: Call.State,
                message: String
            ) {
                println("🔹 Call state: $state ($message)")
                when (state) {
                    Call.State.IncomingReceived -> {
                        // Incoming call detected
                        Log.d("Linphone", "📞 Incoming call from: ${call.remoteAddress.asStringUriOnly()}")

                        // Notify UI (MainActivity, or wherever you handle calls)
                        incomingCallListener?.invoke(call)
                    }
                    Call.State.OutgoingInit -> Log.d("Linphone", "Call is being initiated.")
                    Call.State.OutgoingProgress -> Log.d("Linphone", "Call is ringing (progress).")
                    Call.State.Connected -> {
                        Log.d("Linphone", "Call is connected.")
                        if (state == Call.State.Connected) {
                            val params = call.currentParams
                            val codec = params?.usedAudioPayloadType
                            codec?.let {
                                println("🎤 Codec in use: ${it.mimeType}/${it.clockRate}")
                            }
                        }
                    }
                    Call.State.StreamsRunning -> {
                        val audioManager =
                            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                        audioManager.isSpeakerphoneOn = true
                        Log.d("Linphone", "Media streams are running (audio is live!)")
                    }

                    Call.State.End -> {
                        Log.d("Linphone", "Call has ended.")
                        saveCallLog(call, CallType.Outgoing) // we’ll fix type detection below
                    }
                    Call.State.Error ->  {
                        Log.e(
                            "Linphone",
                            "Call failed with error: $message"
                        )
                        saveCallLog(call, CallType.Missed)
                    }

                    else -> {}
                }
            }
        }

        core?.addListener(coreListener)

        core?.isMicEnabled = true
        core?.isEchoCancellationEnabled=true
        core?.isAudioAdaptiveJittcompEnabled=true

        core?.start()

        // Start Core in background
        Thread {
            while (true) {
                core?.iterate()
                try {
                    Thread.sleep(20)
                } catch (_: InterruptedException) { }
            }
        }.start()
    }

    private fun saveCallLog(call: Call, defaultType: CallType) {
        val number = call.remoteAddress.username ?: "Unknown"
        val type = when {
            call.dir == Call.Dir.Outgoing -> CallType.Outgoing
            call.dir == Call.Dir.Incoming && call.reason == org.linphone.core.Reason.Declined -> CallType.Missed
            call.dir == Call.Dir.Incoming -> CallType.Incoming
            else -> defaultType
        }

        saveCallHistory(CallHistory(number, type, System.currentTimeMillis()))
    }


    fun saveCallHistory(history: CallHistory) {
        callHistory.add(0, history) // latest first
    }

    fun getCallHistory(): List<CallHistory> = callHistory

    /**
     * Register an existing SIP account
     */
    fun registerAccount(username: String, password: String, domain: String, transportType: TransportType) {
        val factory = Factory.instance()

        // Create AuthInfo (credentials)
        val authInfo = factory.createAuthInfo(
            username,   // username
            null,   // password
            password,       // userId
            null,       // ha1
            null,       // realm
            domain      // domain
        )
        core?.addAuthInfo(authInfo)

        // Create empty ProxyConfig
        val proxyConfig = core?.createProxyConfig()

        proxyConfig?.apply {
            // Identity address (who we are)
            identityAddress = core?.createAddress("sip:$username@$domain")?.apply {
                this.transport = transportType
            }

            // Server address (SIP server to register with)
            serverAddr = "sip:$domain"

            // Enable registration
            isRegisterEnabled = true
        }

        // Add ProxyConfig and set it as default
        proxyConfig?.let {
            core?.addProxyConfig(it)
            core?.defaultProxyConfig = it
        }

        // Force registration refresh
        core?.refreshRegisters()
    }


    /**
     * Make an outgoing call
     */
//    fun makeCall(number: String, domain: String) {
//        val remoteAddress = core?.interpretUrl("sip:$number@$domain")
//        remoteAddress?.let {
//            core?.inviteAddress(it)
//        } ?: println("❌ Invalid number: $number")
//    }
    fun makeCall(number: String, domain: String, context: Context) {
        val remoteAddress = core?.interpretUrl("sip:$number@$domain")
        remoteAddress?.let { address ->
            // Start the call
            val call = core?.inviteAddress(address)
            call?.let {
                // Create call params and explicitly enable audio
                val params = core?.createCallParams(it)
                params?.isAudioEnabled = true   // ✅ Ensure audio is active
                params?.isVideoEnabled = false   // Optional: disable video if not used

                // Apply the params to the call
                it.update(params)
            }
        } ?: println("❌ Invalid number: $number")

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = true
    }

    /**
     * Accept an incoming call
     */
    fun acceptCall(call: Call?) {
        call?.accept()
    }

    /**
     * End an ongoing call
     */
    fun endCall() {
        core?.currentCall?.terminate()
    }

}






