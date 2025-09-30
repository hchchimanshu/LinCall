package com.himanshuhc.lincall.presentation.inprogress

import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.himanshuhc.lincall.R
import com.himanshuhc.lincall.data.linphone.LinphoneManager
import com.himanshuhc.lincall.databinding.FragmentCallInProgressBinding
//import kotlinx.coroutines.DefaultExecutor.thread
import org.linphone.core.Call
import org.linphone.core.CallStats
import org.linphone.core.Conference
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


class CallInProgressFragment : Fragment() {


    private var _binding: FragmentCallInProgressBinding? = null
    private val binding get() = _binding!!

    private var call: Call? = null
    private var coreListener: CoreListenerStub? = null

    private val handler = Handler(Looper.getMainLooper())
    private var startTime: Long = 0

    private var batteryReceiver: BatteryLevelReceiver? = null
    private var lastAlertTime: Long = 0
    private val alertCooldown = 5 * 60 * 1000L // 5 minutes in ms

    private var activeCalls: MutableList<Call> = mutableListOf()
    private var isConference: Boolean = false

    private val updateDurationRunnable = object : Runnable {
        override fun run() {
            val elapsed = System.currentTimeMillis() - startTime
            val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60
            binding.tvCallDuration.text = String.format("%02d:%02d", minutes, seconds)
            handler.postDelayed(this, 1000)
        }
    }

    companion object {
        private const val ARG_CALL_NUMBER = "call_number"
        fun newInstance(callNumber: String): CallInProgressFragment {
            val fragment = CallInProgressFragment()
            val bundle = Bundle()
            bundle.putString(ARG_CALL_NUMBER, callNumber)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        call = LinphoneManager.core?.currentCall

        LinphoneManager.core?.currentCall?.let { call ->
            activeCalls.add(call)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCallInProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvCallerNumber.text = arguments?.getString(ARG_CALL_NUMBER) ?: "Unknown"

        // Start call duration timer
        startTime = System.currentTimeMillis()
        handler.post(updateDurationRunnable)

        // Audio setup
        val audioManager = requireContext().getSystemService(AudioManager::class.java)
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        // Hang-up
        binding.btnEndCall.setOnClickListener {
            call?.terminate()
            parentFragmentManager.popBackStack()
        }

        // Mute toggle
        binding.btnMute.setOnClickListener {
            call?.let { toggleMute(it) }
        }
        // Speaker toggle
        binding.btnSpeaker.setOnClickListener {
            audioManager.isSpeakerphoneOn = !audioManager.isSpeakerphoneOn
            binding.btnSpeaker.alpha = if (audioManager.isSpeakerphoneOn) 1f else 0.5f
        }
        // New Call
//        binding.btnCall.setOnClickListener {
//            val core = LinphoneManager.core ?: return@setOnClickListener
//            val newAddress = core.interpretUrl("sip:demo377@sip.linphone.org")
//            if (newAddress != null) {
//                val params = core.createCallParams(null)
//                val newCall = core.inviteAddressWithParams(newAddress, params!!)
//                if (newCall != null) {
//                    activeCalls.add(newCall)
//                }
//                Toast.makeText(requireContext(), "Calling ${newAddress.asString()}", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(requireContext(), "Invalid address", Toast.LENGTH_SHORT).show()
//            }
//        }
        // New Call
        binding.btnCall.setOnClickListener {
            val core = LinphoneManager.core ?: return@setOnClickListener

            try {
                val newAddress = core.interpretUrl("sip:demo377@sip.linphone.org")
                if (newAddress != null) {
                    val params = core.createCallParams(null)
                    // inviteAddressWithParams sometimes returns Call? — capture it
                    val outgoingCall = core.inviteAddressWithParams(newAddress, params!!)
                    if (outgoingCall != null) {
                        // Ensure list updated on main thread
                        requireActivity().runOnUiThread {
                            synchronized(activeCalls) {
                                if (!activeCalls.contains(outgoingCall)) {
                                    activeCalls.add(outgoingCall)
                                }
                            }
                                // update UI text
                                isConference = activeCalls.size > 1
                                binding.tvCallerNumber.text = if (isConference) {
                                    "Conference (${activeCalls.size})"
                                } else {
                                    activeCalls[0].remoteAddress.username
                                }

                        }
                    }
                    Toast.makeText(requireContext(), "Calling ${newAddress.asStringUriOnly()}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Invalid address", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to start call: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }



        // merge call
        binding.btnMerge.setOnClickListener {
            if (activeCalls.size >= 2) {
                val callsToMerge: List<Call>
                synchronized(activeCalls) {
                    callsToMerge = activeCalls.toList() // Snapshot to avoid concurrent modification
                }

                thread {
                    try {
                        val core = LinphoneManager.core ?: return@thread

                        // Create params for the conference
                        val params = core.createConferenceParams(null)

                        // Try to find an existing conference
                        var conference: Conference? = core.searchConference(params, null, null, arrayOf())

                        // If no conference exists, create one
                        if (conference == null) {
                            conference = core.createConferenceWithParams(params)
                        }

                        conference?.let { conf ->
                            callsToMerge.forEach { call ->
                                conf.addParticipant(call)
                            }
                        }

                        // Update UI on main thread
                        requireActivity().runOnUiThread {
                            isConference = true
                            binding.tvCallerNumber.text = "Conference (${callsToMerge.size})"
                            Toast.makeText(context, "Calls merged into conference", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                context,
                                "Failed to merge calls: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

            } else {
                Toast.makeText(context, "At least 2 calls required to merge", Toast.LENGTH_SHORT).show()
            }
        }

//        binding.btnMerge.setOnClickListener {
//            if (activeCalls.size >= 2) {
//                try {
//                    val core = LinphoneManager.core ?: return@setOnClickListener
//
//                    // Create params for the conference
//                    val params = core.createConferenceParams(null)
//
//                    // Try to find an existing conference
//                    var conference: Conference? = core.searchConference(params, null, null, null)
//
//                    // If no conference exists, create one
//                    if (conference == null) {
//                        conference = core.createConferenceWithParams(params)
//                    }
//
//                    conference?.let { conf ->
//                        // Add all active calls
//                        activeCalls.forEach { call ->
//                            conf.addParticipant(call)
//                        }
//
//                        isConference = true
//
//                        // Update UI
//                        binding.tvCallerNumber.text = "Conference (${activeCalls.size})"
//                        Toast.makeText(context, "Calls merged into conference", Toast.LENGTH_SHORT).show()
//                    }
//
//                } catch (e: Exception) {
//                    Toast.makeText(
//                        context,
//                        "Failed to merge calls: ${e.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            } else {
//                Toast.makeText(context, "At least 2 calls required to merge", Toast.LENGTH_SHORT).show()
//            }
//        }



        // Listen for call state changes
        coreListener = object : CoreListenerStub() {
            override fun onCallStateChanged(core: Core, call: Call, state: Call.State, message: String) {
//                if (call != this@CallInProgressFragment.call) return

                when (state) {
                    Call.State.End, Call.State.Error -> {
                        // remove the call in a thread-safe way and update UI on main thread
                        requireActivity().runOnUiThread {
                            synchronized(activeCalls) {
                                activeCalls.remove(call)
                            }
                            if (activeCalls.isEmpty()) {
                                handler.removeCallbacks(updateDurationRunnable)
                                parentFragmentManager.popBackStack()
                            } else if (activeCalls.size == 1) {
                                binding.tvCallerNumber.text = activeCalls[0].remoteAddress.username
                            } else {
                                // multiple calls remain
                                binding.tvCallerNumber.text = if (isConference) {
                                    "Conference (${activeCalls.size})"
                                } else {
                                    activeCalls[0].remoteAddress.username
                                }
                            }
                        }
                    }
                    Call.State.Connected, Call.State.StreamsRunning -> {
                        // Start audio when streams are ready
                        requireActivity().runOnUiThread {
                            synchronized(activeCalls) {
                                if (!activeCalls.contains(call)) activeCalls.add(call)
                            }
                            // set speaker on the main thread's audioManager if desired
                            audioManager.isSpeakerphoneOn = true

                            isConference = activeCalls.size > 1
                            binding.tvCallerNumber.text = if (isConference) {
                                "Conference (${activeCalls.size})"
                            } else {
                                activeCalls[0].remoteAddress.username
                            }
                        }
                    }
                    else -> {
                        // ignore other states or post if you need UI updates
                    }
                }
            }
            override fun onCallStatsUpdated(core: Core, call: Call, stats: CallStats) {

                val isActive = synchronized(activeCalls) {
                    activeCalls.contains(call)
                }
                if (!isActive) return

                // Update bandwidth UI on main thread
                requireActivity().runOnUiThread {
                    updateBandwidthUI(stats)
                }
            }
        }

        LinphoneManager.core?.addListener(coreListener)
    }

    private fun updateBandwidthUI(stats: CallStats) {
        val downloadBps = stats.downloadBandwidth
        val alertText: String
        val alertColor: Int

        when {
            downloadBps >= 15000 -> {
                alertText = "📶 Excellent Bandwidth"
                alertColor = Color.GREEN
            }
            downloadBps >= 8000 -> {
                alertText = "📶 Good Bandwidth"
                alertColor = Color.YELLOW
            }
            else -> {
                alertText = "⚠️ Low Bandwidth"
                alertColor = Color.RED

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastAlertTime > alertCooldown) {
                    lastAlertTime = currentTime
                    Toast.makeText(context, "⚠️ Bandwidth critically low", Toast.LENGTH_SHORT).show()
                }
            }
        }

        requireActivity().runOnUiThread {
            binding.tvAlert.text = alertText
            binding.tvAlert.setTextColor(alertColor)
            binding.tvAlert.visibility = View.VISIBLE
        }
    }


    private fun toggleMute(call: Call) {
        val core = LinphoneManager.core ?: return
        val isMuted = !core.isMicEnabled
        core.isMicEnabled = isMuted
        // Optional: update button alpha
        binding.btnMute.alpha = if (!isMuted) 1f else 0.5f
    }

    override fun onResume() {
        super.onResume()
        // Register receiver only while this fragment is active
        batteryReceiver = BatteryLevelReceiver { level ->
            val currentTime = System.currentTimeMillis()
            if (level <= 60 && (currentTime - lastAlertTime) > alertCooldown) {
                lastAlertTime = currentTime
                showLowBatteryDialog(level)
                Toast.makeText(context, "⚠️ Battery critically low ($level%)", Toast.LENGTH_LONG).show()

            }
        }

        requireContext().registerReceiver(
            batteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
    }

    private fun showLowBatteryDialog(level: Int) {
        // Replace with your preferred UI (Dialog, Snackbar, Banner)
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Low Battery")
            .setMessage("Battery critically low ($level%). Call may drop soon.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onPause() {
        super.onPause()
        // Unregister when fragment not visible
        batteryReceiver?.let {
            requireContext().unregisterReceiver(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateDurationRunnable)
        coreListener?.let { LinphoneManager.core?.removeListener(it) }
        _binding = null
    }
}