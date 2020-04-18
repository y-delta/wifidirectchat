package com.example.myapp.ui.ledger

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import kotlinx.android.synthetic.main.activity_take_input.*
import java.io.IOException
import java.util.*

private const val PERMISSION_REQUEST = 10

class TakeInput : AppCompatActivity(){

    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    private var permissionEnabled = false
    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var accuracy: Float? = 0F
    private var locationName: String? = null
    private lateinit var bestLocation: Location
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_input)
        permissionEnabled = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission(permissions)) {
                permissionEnabled = true
            } else {
                requestPermissions(permissions, PERMISSION_REQUEST)
            }
        } else {
            permissionEnabled = true
        }

        if(latitude==null){
            text_location_accuracy.text = "6969"
        }
        else{
            text_location_accuracy.text = accuracy.toString()
        }


        val handler = Handler()
        handler.postDelayed(Runnable {
            getLocation()
            handler.postDelayed(this, 1000)
        }, 0)

    }

    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices) {
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if(permissionEnabled) {
            if (hasGps || hasNetwork) {

                if (hasGps) {
                    Log.d("CodeAndroidLocation", "hasGps")
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        5000,
                        0F,
                        object :
                            LocationListener {
                            override fun onLocationChanged(location: Location?) {
                                if (location != null) {
                                    locationGps = location
                                    bestLocation = location
                                    latitude = locationGps!!.latitude
                                    longitude = locationGps!!.longitude
                                    accuracy = locationGps!!.accuracy
                                    text_location_accuracy.text = accuracy.toString()
                                    Log.d(
                                        "CodeAndroidLocation",
                                        " GPS Latitude : " + locationGps!!.latitude
                                    )
                                    Log.d(
                                        "CodeAndroidLocation",
                                        " GPS Longitude : " + locationGps!!.longitude
                                    )

                                }
                            }

                            override fun onStatusChanged(
                                provider: String?,
                                status: Int,
                                extras: Bundle?
                            ) {

                            }

                            override fun onProviderEnabled(provider: String?) {

                            }

                            override fun onProviderDisabled(provider: String?) {

                            }

                        })

                    val localGpsLocation =
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (localGpsLocation != null)
                        locationGps = localGpsLocation
                }
                if (hasNetwork) {
                    Log.d("CodeAndroidLocation", "hasGps")
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        5000,
                        0F,
                        object : LocationListener {
                            override fun onLocationChanged(location: Location?) {
                                if (location != null && location!!.accuracy > accuracy!!) {
                                    locationNetwork = location
                                    bestLocation = location
                                    latitude = locationNetwork!!.latitude
                                    longitude = locationNetwork!!.longitude
                                    accuracy = locationNetwork!!.accuracy
                                    text_location_accuracy.text = accuracy.toString()
                                    Log.d(
                                        "CodeAndroidLocation",
                                        " Network Latitude : " + locationNetwork!!.latitude
                                    )
                                    Log.d(
                                        "CodeAndroidLocation",
                                        " Network Longitude : " + locationNetwork!!.longitude
                                    )
                                }
                            }

                            override fun onStatusChanged(provider: String?,status: Int,extras: Bundle?) { }

                            override fun onProviderEnabled(provider: String?) { }

                            override fun onProviderDisabled(provider: String?) { }

                        })

                    val localNetworkLocation =
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (localNetworkLocation != null)
                        locationNetwork = localNetworkLocation
                }

                if(locationGps!= null && locationNetwork!= null){
                    if(locationGps!!.accuracy >= locationNetwork!!.accuracy){
                        bestLocation = locationGps as Location
                    }else{

                        bestLocation = locationNetwork as Location
                    }

                    latitude = bestLocation!!.latitude
                    longitude = bestLocation!!.longitude
                    accuracy = bestLocation!!.accuracy
                    text_location_accuracy.text = accuracy.toString()
                }

            } else {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(permissionEnabled)
        {
            if (requestCode == PERMISSION_REQUEST) {
                var allSuccess = true
                for (i in permissions.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        allSuccess = false
                        val requestAgain = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(permissions[i])
                        if (requestAgain) {
                            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Go to settings and enable the permission", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                if (allSuccess)
                    permissionEnabled = true

            }
        }
    }

    fun onCheckboxClicked(view: View) {
        var checked = view as CheckBox

    }
    fun message(str: String) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show()
    }

    fun onButtonClick(view: View) {

        if(latitude == null || longitude == null || !permissionEnabled){
            text_landmark.requestFocus()
            return
        }
        val geocoder = Geocoder(applicationContext, Locale.getDefault())
        try {
            val listAddresses: List<Address>? =
                geocoder.getFromLocation(latitude!!, longitude!!, 1)
            if (null != listAddresses && listAddresses.size > 0) {
                locationName = listAddresses[0].getAddressLine(0)
                Log.d("onButtonClick", "location = "+ locationName)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if(locationName.isNullOrEmpty())
            locationName = latitude.toString() + ", " + longitude.toString()

        var landmark = text_landmark.text.toString()
        val intent = Intent()
        intent.putExtra("Location", locationName)
        intent.putExtra("Landmark", landmark)
        intent.putStringArrayListExtra("LatLongAcc", arrayListOf(latitude.toString(), longitude.toString(), accuracy.toString()))
        Log.d("onButtonClick", "location = "+locationName)
        Log.d("onButtonClick", "landmark = "+landmark)
        setResult(7070, intent)
        finish()

    }

}


private fun Handler.postDelayed(takeInput: TakeInput, l: Long) {

}