package com.example.myapp.connections

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import com.example.myapp.MainActivity

/* This class is used for WiFi legacy to scan for networks to check if a WiFi Direct group exists around us */

class WiFiBroadcastReceiver(
    private val wifiManager: WifiManager,
    private val mainActivity: MainActivity
)
    : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        val success = intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
        if (success!!) {
//            scanSuccess()
        } else {
//            scanFailure()
        }
    }

}

