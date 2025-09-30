package com.himanshuhc.lincall.presentation.incoming

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.himanshuhc.lincall.R
import com.himanshuhc.lincall.data.linphone.LinphoneManager
import com.himanshuhc.lincall.data.linphone.LinphoneManager.core
import com.himanshuhc.lincall.databinding.FragmentCallInProgressBinding
import com.himanshuhc.lincall.databinding.FragmentIncomingCallBinding
import com.himanshuhc.lincall.presentation.MainActivity
import com.himanshuhc.lincall.presentation.inprogress.CallInProgressFragment
import org.linphone.core.Call
import org.linphone.core.CallStats
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub

class IncomingCallFragment : Fragment() {

    private var _binding: FragmentIncomingCallBinding? = null
    private val binding get() = _binding!!

    private var callerNumber: String? = null

    companion object {
        private const val ARG_CALLER_NUMBER = "caller_number"

        fun newInstance(number: String): IncomingCallFragment {
            val fragment = IncomingCallFragment()
            val bundle = Bundle()
            bundle.putString(ARG_CALLER_NUMBER, number)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callerNumber = arguments?.getString(ARG_CALLER_NUMBER)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncomingCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvCallerNumber.text = callerNumber ?: "Unknown"

        // ✅ Accept call → go to CallInProgressFragment
        binding.btnAcceptCall.setOnClickListener {
            val call = LinphoneManager.core?.currentCall
            call?.accept()

            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    CallInProgressFragment.newInstance(call?.remoteAddress?.username ?: "Unknown")
                )
                .addToBackStack(null)
                .commit()
        }

        // ❌ Reject call → terminate + back to dialer
        binding.btnRejectCall.setOnClickListener {
            LinphoneManager.core?.currentCall?.terminate()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
