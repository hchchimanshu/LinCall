package com.himanshuhc.lincall.presentation.inprogress

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.widget.Toast

class BatteryLevelReceiver(
    private val onLowBattery: (level: Int) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = (level * 100) / scale

        // Trigger alert only when battery is critically low (5% or less)
        if (batteryPct in 1..5) {
            onLowBattery.invoke(batteryPct)

            // Optional: fallback toast
//            Toast.makeText(context, "⚠️ Battery critically low ($batteryPct%)", Toast.LENGTH_LONG).show()
        }
    }
}