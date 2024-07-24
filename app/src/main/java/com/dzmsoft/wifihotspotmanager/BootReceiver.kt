package com.dzmsoft.wifihotspotmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences("HotspotPrefs", Context.MODE_PRIVATE)
            val autoStart = sharedPreferences.getBoolean("autoStart", false)
            val hotspotEnabled = sharedPreferences.getBoolean("hotspotEnabled", false)
            if (autoStart && hotspotEnabled) {
                val serviceIntent = Intent(context, HotspotService::class.java)
                context.startService(serviceIntent)
            }
        }
    }
}