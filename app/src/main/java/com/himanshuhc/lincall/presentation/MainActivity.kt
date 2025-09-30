package com.himanshuhc.lincall.presentation

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.commit
import com.himanshuhc.lincall.R
import com.himanshuhc.lincall.data.linphone.LinphoneManager
import com.himanshuhc.lincall.data.linphone.RegistrationState
import com.himanshuhc.lincall.databinding.ActivityMainBinding
import com.himanshuhc.lincall.presentation.history.CallHistoryFragment
import com.himanshuhc.lincall.presentation.dialer.DialerFragment
import com.himanshuhc.lincall.presentation.incoming.IncomingCallFragment
import org.linphone.core.TransportType

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load Dialer as the first screen
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragmentContainer, DialerFragment())
            }
        }

        // Bottom navigation listener
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_dialer -> navigateToDialer()
                R.id.menu_history -> navigateToHistory()
            }
            true
        }

        // Register the test account
        LinphoneManager.registerAccount(
            username = "demo376",
            password = "Demo376@@",
            domain = "sip.linphone.org",
            transportType = TransportType.Tcp
        )


        // Observe registration state
        LinphoneManager.registrationStateListener = { state ->
            runOnUiThread {
                when (state) {
                    RegistrationState.Ok ->
                        Toast.makeText(this, "✅ Registered successfully", Toast.LENGTH_SHORT).show()
                    is RegistrationState.Failed ->
                        Toast.makeText(this, "❌ Failed: ${state.message}", Toast.LENGTH_SHORT).show()
                    RegistrationState.Progress ->
                        Toast.makeText(this, "⏳ Registering...", Toast.LENGTH_SHORT).show()
                    RegistrationState.Cleared ->
                        Toast.makeText(this, "🔄 Unregistered", Toast.LENGTH_SHORT).show()
                    else -> {}
                }
            }
        }

        // Incoming call listener
        LinphoneManager.incomingCallListener = { call ->
            runOnUiThread {
                val caller = call?.remoteAddress?.username ?: "Unknown"
                showIncomingCallFragment(caller)
            }
        }

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 123)
    }

    // Switch to Call History
    fun navigateToHistory() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, CallHistoryFragment())
            addToBackStack(null)
        }
    }

    // Switch to Dialer
    fun navigateToDialer() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, DialerFragment())
            addToBackStack(null)
        }
    }

    // Show incoming call fragment programmatically
    fun showIncomingCallFragment(callerNumber: String) {
        val incomingFragment = IncomingCallFragment.newInstance(callerNumber)
        supportFragmentManager.commit {
            add(R.id.fragmentContainer, incomingFragment)
            addToBackStack(null)
        }
    }

//    private lateinit var binding: ActivityMainBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Load Dialer as the first screen
//        if (savedInstanceState == null) {
//            supportFragmentManager.commit {
//                replace(R.id.fragmentContainer, DialerFragment())
//            }
//        }
//
//        // Example: if you add a bottom navigation in activity_main.xml
//        // you can do this:
//        // binding.bottomNavigation.setOnItemSelectedListener { item ->
//        //     when (item.itemId) {
//        //         R.id.menu_dialer -> navigateToDialer()
//        //         R.id.menu_history -> navigateToHistory()
//        //     }
//        //     true
//        // }
//    }
//
//    // Helper to switch fragments
//    fun navigateToHistory() {
//        supportFragmentManager.commit {
//            replace(R.id.fragmentContainer, CallHistoryFragment())
//            addToBackStack(null)
//        }
//    }
//
//    fun navigateToDialer() {
//        supportFragmentManager.commit {
//            replace(R.id.fragmentContainer, DialerFragment())
//            addToBackStack(null)
//        }
//    }

}