package com.example.myapp.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.myapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

private lateinit var continueButton: Button
private lateinit var wifiButton: Button
private lateinit var gpsButton: Button

class ModalBottomSheet : BottomSheetDialogFragment() { //incomplete implementation

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.bottom_sheet, container, false)
        continueButton = root.findViewById(R.id.cont)
        wifiButton = root.findViewById(R.id.wifi_on)
        gpsButton = root.findViewById(R.id.loc_on)

        continueButton.setOnClickListener { disableModal() }
        wifiButton.setOnClickListener { wifiPermission() }
        gpsButton.setOnClickListener { locPermission() }

        return root

    }

    @SuppressLint("WrongConstant")
    private fun wifiPermission(): Boolean {
        startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        return true
    }

    @SuppressLint("WrongConstant")
    private fun locPermission(): Boolean {
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        return true
    }

    private fun disableModal(): Boolean {
        dismiss()
        return true
    }

}