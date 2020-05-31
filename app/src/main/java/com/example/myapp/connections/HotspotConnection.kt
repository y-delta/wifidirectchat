package com.example.myapp.connections

import android.content.Context
import android.net.nsd.NsdManager
import android.net.wifi.WifiConfiguration
import android.util.Log
import android.widget.Toast
import com.example.myapp.MainActivity
import com.example.myapp.MainActivity.Companion.nsdAlreadyDiscovering

class HotspotConnection (var activity: MainActivity) : Thread() {

    override fun run() {

        while(MainActivity.NETWORK_USERNAME.isNullOrEmpty()){
            Log.d("CreateGroupOrConnect", "Username is not yet set")
            sleep(1500)
        }
        while(!MainActivity.wifiScannedAtleastOnce){
            Log.d("HotspotConnection", "Wifi not yet scanned")
            Thread.sleep(2000)
        }

        for(deviceName in MainActivity.ssidList){
            Log.d("connectToHotspot", "checking device $deviceName")
            Log.d("connectToHotspot", "serverCreated = ${MainActivity.serverCreated}")
            if(!MainActivity.serverCreated){
                //this code is very inefficient right now
                Log.d("connectToHotspot", "device is legit")
                for(wifiDevice in MainActivity.resultList){
                    Log.d("connectToHotspot", "checking if ${wifiDevice.SSID} is $deviceName")
                    if(wifiDevice.SSID.length > 10 && wifiDevice.SSID.substring(10)==deviceName){
                        Log.d("connectToHotspot", "${wifiDevice.SSID} is $deviceName")
                        Log.d("connectToHotspot", "found the ssid's full name")
                        var wifiConfig = WifiConfiguration()
                        wifiConfig.SSID = "\"" + wifiDevice.SSID + "\""     //make sure device is connected to GO's hotspot once before
//                            wifiConfig.preSharedKey = "\""+ "aGkPCGl2" +"\""    //TODO check without this line!!!

//                            wifiConfig.SSID =  wifiDevice.SSID
                        Log.d("connectToHotspot", "attempting connect to ${wifiConfig.SSID}")
                        val list = activity.wifiManager!!.configuredNetworks
                        for (i in list) {
                            Log.d("connectToHotspot", "inner checking ${i.SSID}")
                            if (i.SSID != null && i.SSID == "\"" + wifiDevice.SSID.toString() + "\"") {
                                Log.d("connectToHotspot", "inner attempting connect to ${i.SSID}")
//                                activity.wifiManager!!.disconnect()
//                                activity.wifiManager!!.enableNetwork(i.networkId, true)
//                                activity.wifiManager!!.reconnect()
//                                    Log.d("INETADDRESS", getLocalIpAddress()?.hostAddress)
//                                    clientClass = getLocalIpAddress()?.let { ClientClass(it) }
//                                    clientClass!!.start()
                                break
                            }
                        }
                        try {
                            activity.nsdManager = activity.getSystemService(Context.NSD_SERVICE) as NsdManager
                            if(!nsdAlreadyDiscovering) {
                                activity.nsdManager.discoverServices(
                                    MainActivity.SERVICE_TYPE,
                                    NsdManager.PROTOCOL_DNS_SD,
                                    activity.mDiscoveryListener
                                )
                                nsdAlreadyDiscovering = true
                            }
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
//                            wifiManager?.disconnect()
//                            wifiManager?.enableNetwork(wifiConfig.networkId, true)
//                            wifiManager?.reconnect()
                        break
                    }
                }
            }else{
                Toast.makeText(activity, "can't join if you are GO", Toast.LENGTH_SHORT)
            }

        }
    }

}