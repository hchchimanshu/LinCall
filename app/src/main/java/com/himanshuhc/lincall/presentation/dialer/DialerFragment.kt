package com.himanshuhc.lincall.presentation.dialer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.himanshuhc.lincall.presentation.MainActivity
import com.himanshuhc.lincall.R
import com.himanshuhc.lincall.data.linphone.LinphoneManager
import com.himanshuhc.lincall.databinding.FragmentDialerBinding
import com.himanshuhc.lincall.presentation.inprogress.CallInProgressFragment

class DialerFragment : Fragment() {

        private var _binding: FragmentDialerBinding? = null
        private val binding get() = _binding!!

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentDialerBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // Setup number buttons
            val buttons = listOf(
                binding.btn0, binding.btn1, binding.btn2, binding.btn3,
                binding.btn4, binding.btn5, binding.btn6,
                binding.btn7, binding.btn8, binding.btn9,
                binding.btnStar, binding.btnHash
            )

            buttons.forEach { button ->
                button.setOnClickListener {
                    appendDigit(button.text.toString())
                }
            }

            // Backspace
            binding.btnBackspace.setOnClickListener {
                val current = binding.tvPhoneNumber.text.toString()
                if (current.isNotEmpty()) {
                    binding.tvPhoneNumber.text = current.dropLast(1)
                }
            }

            // Call Button
            binding.btnCall.setOnClickListener {
                val number = binding.tvPhoneNumber.text.toString()
                if (number.isNotEmpty()) {
                    val core = LinphoneManager.core
                    if (core != null) {
                        when (number) {
                            "7696729269" -> {
                                // Make the outgoing call
                                LinphoneManager.makeCall("himanshuhc", "sip.linphone.org", requireContext())
                                Toast.makeText(requireContext(), "Calling $number...", Toast.LENGTH_SHORT).show()

                                // Navigate to CallInProgressFragment
                                parentFragmentManager.beginTransaction()
                                    .replace(
                                        R.id.fragmentContainer,
                                        CallInProgressFragment.newInstance(number)
                                    )
                                    .addToBackStack(null)
                                    .commit()
                            }
                            "7696729260" -> {
                                // Make the outgoing call
                                LinphoneManager.makeCall("demo377", "sip.linphone.org", requireContext())
                                Toast.makeText(requireContext(), "Calling $number...", Toast.LENGTH_SHORT).show()

                                // Navigate to CallInProgressFragment
                                parentFragmentManager.beginTransaction()
                                    .replace(
                                        R.id.fragmentContainer,
                                        CallInProgressFragment.newInstance(number)
                                    )
                                    .addToBackStack(null)
                                    .commit()
                            }

                            else -> {
                                Toast.makeText(requireContext(), "Linphone not initialized", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Linphone not initialized", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Enter a number", Toast.LENGTH_SHORT).show()
                }
            }

            // Navigate to history on long press
            binding.btnCall.setOnLongClickListener {
                (activity as? MainActivity)?.navigateToHistory()
                true
            }
        }

        private fun appendDigit(digit: String) {
            binding.tvPhoneNumber.text = binding.tvPhoneNumber.text.toString() + digit
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
}
