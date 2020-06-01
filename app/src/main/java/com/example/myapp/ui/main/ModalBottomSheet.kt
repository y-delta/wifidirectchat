package com.example.myapp.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import com.example.myapp.R
import com.example.myapp.R.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

private lateinit var continueButton: Button
private lateinit var wifiButton: Button
private lateinit var gpsButton: Button

private var wifiStatus : Boolean = false
private var gpsStatus : Boolean = false

private lateinit var wifiManager: WifiManager
private lateinit var locManager: LocationManager

class ModalBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(layout.bottom_sheet, container, false)
        continueButton = root.findViewById(R.id.cont)
        wifiButton = root.findViewById(R.id.wifi_on)
        gpsButton = root.findViewById(R.id.loc_on)

        wifiManager =
            requireActivity().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        locManager =
            context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        wifiStatus = wifiManager.isWifiEnabled
        gpsStatus = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if(wifiStatus && gpsStatus){dismiss()}

        if(wifiStatus)
            serviceOn(wifiButton)

        if(gpsStatus)
            serviceOn(gpsButton)

        continueButton.setOnClickListener {
            if(!wifiManager.isWifiEnabled||!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                Toast.makeText(
                    root.context,
                    "Enable permissions first!",
                    Toast.LENGTH_LONG
                ).show()
            else {
                dismiss()
            }
        }

        wifiButton.setOnClickListener {
            if(!wifiStatus) {
               // startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
             //   Timer().schedule(1000){
                    wifiManager.isWifiEnabled = true
                    serviceOn(wifiButton)
            }
        }
        gpsButton.setOnClickListener {
            //may be buggy
            if(locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                serviceOn(gpsButton)
            }
            else {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
        return root
    }
    @SuppressLint("SetTextI18n")
    private fun serviceOn(button: Button) {
        button.setTextColor(Color.parseColor("#0B916C"))
        button.text = "ON"
        button.setBackgroundResource(drawable.ic_wifi)
    }
}