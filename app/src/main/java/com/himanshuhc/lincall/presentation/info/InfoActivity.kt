package com.himanshuhc.lincall.presentation.info

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.himanshuhc.lincall.R
import com.himanshuhc.lincall.data.linphone.LinphoneManager
import com.himanshuhc.lincall.data.linphone.RegistrationState
import com.himanshuhc.lincall.databinding.ActivityInfoBinding
import com.himanshuhc.lincall.presentation.MainActivity
import org.linphone.core.TransportType

class InfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Attach listener BEFORE calling registerAccount
        LinphoneManager.registrationStateListener = { state ->
            runOnUiThread {
                when (state) {
                    RegistrationState.Ok -> {
                        // ✅ Registration success → move to MainActivity
                        Toast.makeText(this, "✅ Registered successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    RegistrationState.Progress -> {
                        Toast.makeText(this, "⏳ Registering...", Toast.LENGTH_SHORT).show()
                    }
                    is RegistrationState.Failed -> {
                        Toast.makeText(this, "❌ Failed: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                    RegistrationState.Cleared -> {
                        Toast.makeText(this, "🔄 Unregistered", Toast.LENGTH_SHORT).show()
                    }
                    RegistrationState.None -> {
                        // do nothing or log
                    }
                }
            }
        }

        binding.proceedTV.setOnClickListener {

            LinphoneManager.registerAccount(
                username = binding.usernameET.text.toString().trim(),
                password = binding.passwordET.text.toString().trim(),
                domain = binding.domainET.text.toString().trim(),
                transportType = TransportType.Tcp
            )

        }

        binding.defaultRegisterTV.setOnClickListener {

            // 🔹 Trigger registration AFTER listener is set
            LinphoneManager.registerAccount(
                username = "demo376",
                password = "Demo376@@",
                domain = "sip.linphone.org",
                transportType = TransportType.Tcp
            )
        }
    }
}