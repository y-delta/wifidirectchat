package com.example.myapp

import LedgerFragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.DiscoveryListener
import android.net.nsd.NsdManager.RegistrationListener
import android.net.nsd.NsdServiceInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener
import android.net.wifi.p2p.WifiP2pManager.PeerListListener
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.connections.*
import com.example.myapp.ui.directmessage.DirectMessageFragment
import com.example.myapp.ui.globalmessage.GlobalMessageFragment
import com.example.myapp.ui.main.ModalBottomSheet
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class MainActivity : AppCompatActivity() {

    lateinit var nsdManager: NsdManager
    private var peerCount = 0
    var directMessageFragment: DirectMessageFragment = DirectMessageFragment()
    var globalMessageFragment: GlobalMessageFragment = GlobalMessageFragment()
    var ledgerFragment: LedgerFragment = LedgerFragment()

    var wifiManager: WifiManager? = null
    var mManager: WifiP2pManager? = null
    var mChannel: WifiP2pManager.Channel? = null
    var mReceiver: BroadcastReceiver? = null
    var mIntentFilter: IntentFilter? = null

    var wifiIntentFilter: IntentFilter? = null
    var wifiScanReceiver: BroadcastReceiver? = null

    var serverClass: ServerClass? = null
    var clientClass: ClientClass? = null
    var sendReceive: SendReceive? = null


    private val modalBottomSheet = ModalBottomSheet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.nav_host_fragment, globalMessageFragment, "putGlobalMessageFragment")
                .commit()

            wifiManager =
                applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager    //DIRECT-KE-Moto G (4)_4eaa DIRECT-sG-potter_n

            mManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
            mChannel = mManager!!.initialize(this, mainLooper, null)

            mReceiver = WifiDirectBroadcastReceiver(mManager, mChannel!!, this)
            mIntentFilter = IntentFilter()

            mIntentFilter!!.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            mIntentFilter!!.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            mIntentFilter!!.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            mIntentFilter!!.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

//            wifiIntentFilter!!.addAction((WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
//            wifiScanReceiver = object : BroadcastReceiver() {         //this piece of code was not working for unknown reasons
//
//                override fun onReceive(context: Context, intent: Intent) {
//                    val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
//                    if (success) {
//                        scanSuccess()
//                    } else {
//                        scanFailure()
//                    }
//                }
//            }
//            wifiIntentFilter = IntentFilter()
//            wifiIntentFilter?.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
//            registerReceiver(wifiScanReceiver, wifiIntentFilter)
//
//            val success = wifiManager!!.startScan()
//            if (!success) {
//                // scan failure handling
//                scanFailure()
//            }
        }

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

//        initialWork()

        modalBottomSheet.show(supportFragmentManager, modalBottomSheet.tag) //implement this on WiFiDirectBroadcastReceiver when Prashant sends code
        modalBottomSheet.isCancelable = false //prevents cancelling
    }


//    private fun scanSuccess() {
//        val results = wifiManager?.scanResults
//        for (result in results!!){
//            Log.d("Successful scan results", result.SSID)
//        }
//    }

    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        val results = wifiManager?.scanResults
        for (result in results!!){
            Log.d("Successful scan results", result.SSID)
        }
        if(results!!.size > 0) wifiScannedAtleastOnce = true
    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_directmessage -> {
                    supportFragmentManager.beginTransaction().replace(
                        R.id.nav_host_fragment,
                        directMessageFragment,
                        "putDirectMessageFragment"
                    )
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_globalmessage -> {
                    supportFragmentManager.beginTransaction().replace(
                        R.id.nav_host_fragment,
                        globalMessageFragment,
                        "putGlobalMessageFragment"
                    )
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_ledger -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, ledgerFragment, "putLedgerFragment")
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }
            }

            false
        }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // handle button activities
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
//        if (id == R.id.connection) { // do something here
//            Log.d("Clicked", "discover peers")
////            val builder = AlertDialog.Builder(this)
////            builder.setTitle("Enter your username:")
////            val input = EditText(this)
////            builder.setView(input)
////            builder.setPositiveButton("ENTER"){ dialog, id ->
////                dialog.cancel()
////            }
//
////            builder.create().show()
//            scanWifi()
//            mManager!!.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
//                override fun onSuccess() {
//                    Log.d("Discover Peers", "Discover karega ab")
//                }
//
//                override fun onFailure(reason: Int) {
//                    Log.d("Discover Peers", "Nahi shuru ho payi discovery")
//                }
//            })
//
//        }
//        if(id == R.id.connectionGO){
//            Log.d("Clicked", "create group")
//            if (!groupCreated) {
//                Log.d("Clicked", "group not created, trying to create a group")
////                    wifiManager.startScan();
////                    wifiManager.getScanResults();  //TODO not yet implemented
//                mManager!!.createGroup(mChannel, object : WifiP2pManager.ActionListener {
//                    override fun onSuccess() {
//                        Toast.makeText(applicationContext, "group successfully created ", Toast.LENGTH_SHORT).show()
//                        Log.d("createGroup", "Successfully created a group")
//                        groupCreated = true
//                    }
//
//                    override fun onFailure(reason: Int) {
//                        Toast.makeText(applicationContext, "group not created!!", Toast.LENGTH_SHORT).show()
//                        Log.d("createGroup", "group creation failure")
//                    }
//                })
//            }
//        }
//        if(id == R.id.scanWifi){
//            scanWifi()
//            mManager!!.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
//                override fun onSuccess() {
//                    Log.d("Discover Peers", "Discover karega ab")
//                }
//
//                override fun onFailure(reason: Int) {
//                    Log.d("Discover Peers", "Nahi shuru ho payi discovery")
//                }
//            })
//            var createGroupOrConnect = CreateGroupOrConnect(mManager, mChannel, applicationContext)
//            createGroupOrConnect.start()
//
//        }
        if(id == R.id.connection){
            scanWifi()
            mManager!!.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d("Discover Peers", "Discover karega ab")
                }

                override fun onFailure(reason: Int) {
                    Log.d("Discover Peers", "Nahi shuru ho payi discovery")
                }
            })
            var createGroupOrConnect = CreateGroupOrConnect(mManager, mChannel, applicationContext)
            createGroupOrConnect.start()
        }
        if(id == R.id.connectToHotspot){
            Log.d("connectToHotspot", "button clicked")
            for(deviceName in ssidList){
                Log.d("connectToHotspot", "checking device $deviceName")
                Log.d("connectToHotspot", "serverCreated = $serverCreated")
                if(!serverCreated){
                    //this code is very inefficient right now
                    Log.d("connectToHotspot", "device is legit")
                    for(wifiDevice in resultList){
                        Log.d("connectToHotspot", "checking if ${wifiDevice.SSID} is $deviceName")
                        if(wifiDevice.SSID.substring(10)==deviceName){
                            Log.d("connectToHotspot", "${wifiDevice.SSID} is $deviceName")
                            Log.d("connectToHotspot", "found the ssid's full name")
                            var wifiConfig = WifiConfiguration()
                            wifiConfig.SSID = "\"" + wifiDevice.SSID + "\""
                            wifiConfig.preSharedKey = "\""+ "aGkPCGl2" +"\""

//                            wifiConfig.SSID =  wifiDevice.SSID
                            Log.d("connectToHotspot", "attempting connect to ${wifiConfig.SSID}")
                            val list = wifiManager!!.configuredNetworks
                            for (i in list) {
                                Log.d("connectToHotspot", "inner checking ${i.SSID}")
                                if (i.SSID != null && i.SSID == "\"" + wifiDevice.SSID.toString() + "\"") {
                                    Log.d("connectToHotspot", "inner attempting connect to ${i.SSID}")
//                                    wifiManager!!.disconnect()
//                                    wifiManager!!.enableNetwork(i.networkId, true)
//                                    wifiManager!!.reconnect()
                                    Log.d("INETADDRESS", getLocalIpAddress()?.hostAddress)
//                                    clientClass = getLocalIpAddress()?.let { ClientClass(it) }
//                                    clientClass!!.start()
                                    break
                                }
                            }
                            try {
                                nsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager
                                nsdManager.discoverServices(
                                    SERVICE_TYPE,
                                    NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener
                                )
                            }catch (e:Exception){
                                e.printStackTrace()
                            }
//                            wifiManager?.disconnect()
//                            wifiManager?.enableNetwork(wifiConfig.networkId, true)
//                            wifiManager?.reconnect()
                            break
                        }
                    }
                }

            }
        }
        if (id == R.id.discoverPeersOnly) { // do something here
            Log.d("Clicked", "discover peers")
            scanWifi()
            mManager!!.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d("Discover Peers", "Discover karega ab")
                }

                override fun onFailure(reason: Int) {
                    Log.d("Discover Peers", "Nahi shuru ho payi discovery")
                }
            })

        }
        return super.onOptionsItemSelected(item)
    }
    fun getLocalIpAddress(): InetAddress? {
        try {
            var ii : Inet4Address? = null
            val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf: NetworkInterface = en.nextElement()
                val enumIpAddr: Enumeration<InetAddress> = intf.getInetAddresses()
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        Log.d("inetaddress = ", inetAddress.toString())
                        ii = inetAddress
                    }
                }
            }
            return ii
        } catch (ex: java.lang.Exception) {
            Log.e("IP Address", ex.toString())
        }
        return null
    }

    private val wifiReceiver = object : BroadcastReceiver() {
        // единственная необходимая функция в классе
        // она вызывается, если приходит какое-либо сообщения
        override fun onReceive(context: Context, intent: Intent) {
            // освобождаем приёмник, чтобы не расходовать ресурсы
            unregisterReceiver(this)
            Log.d("LegacyWifiOnReceive", "unregisterReceiver")

            // через интент проверяем, что сканирование было успешным
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            Log.d("LegacyWifiOnReceive", "intent.getBooleanExtra")
            // обрабатываем результаты сканирования
            if (success) {
                scanSuccess()
                Log.d("LegacyWifiOnReceive", "scan successful")
            }
            else {
                scanFailure()
                Log.d("LegacyWifiOnReceive", "scan failed")
            }

        }
    }
    private fun scanWifi() {
        registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager?.startScan()
        Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show()
    }

    private fun scanSuccess() {
        resultList.clear()
        ssidList.clear()
        // fillTheArrayWithNewWiFiPoints
        resultList = wifiManager?.scanResults as ArrayList<ScanResult>
        // updatingResultsInListView
//        uploadListView()
        // debugOnly
        Log.d("scansuccess()", resultList.toString())
        for (result in resultList) {
            Log.d("LegacyWifiScanSuccess", "SSID: ${result.SSID}")
            var ssid = StringBuilder(result.SSID)
            if(ssid.startsWith("DIRECT")){
                ssidList.add(ssid.substring(10))
                Log.d("LegacyWifiScanSuccess substring", ssid.substring(10))
            }
            else{
                Log.d("scanSuccess()", "not GO device = $ssid")
            }
            Log.d("LegacyWifiScanSuccess", "BSSID: ${result.BSSID}")
//            Log.d("LegacyWifiScanSuccess", "level: ${calculateSignalLevel(result.level)}")
            Log.d("LegacyWifiScanSuccess", "frequency: ${result.frequency} hHz")
            Log.d("LegacyWifiScanSuccess", "capabilities: ${result.capabilities}")
        }

        wifiScannedAtleastOnce = true
    }

    init{
    }

    fun testDisplay(str : String){
        Log.d("String", str)
    }

    var resolveListener:NsdManager.ResolveListener = object : NsdManager.ResolveListener{
        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            //do nothing for now
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
            Log.d("DiscoveryListener", "host is null, servicename = ${serviceInfo?.serviceName}, servicetype = ${serviceInfo?.serviceType}, port = ${serviceInfo?.port}")
            try{
                Log.d("Resolve", "about to create client class object alhamdulilah")
                clientClass = ClientClass(serviceInfo!!.host)
                clientClass!!.start()
                Log.d("Resolve", "created client object and started thread mashallah")
            }
            catch (e:Exception){
                e.printStackTrace()
            }
        }


    }

    var mDiscoveryListener: DiscoveryListener = object : DiscoveryListener {
        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
//            if (serviceInfo.serviceName == mThisDeviceName) {
//                return
//            }
            Log.d(
                "nsd onServiceFound",
                String.format(
                    "found \"%s\"; queued for resolving",
                    serviceInfo.serviceName
                )
            )
            if(serviceInfo.serviceType.contains(MainActivity.SERVICE_TYPE))
                nsdManager.resolveService(serviceInfo, resolveListener);
            try {
                Log.d("nsd", "About to create a clientclass object alhamdulilah")
                Log.d("DiscoveryListener", "host is null, servicename = ${serviceInfo.serviceName}, servicetype = ${serviceInfo.serviceType}, port = ${serviceInfo.port}")
                Log.d("nsd", "just created clientclass object and started the thread mashallah")
            }catch (e:Exception){
                e.printStackTrace()
            }
        }



        override fun onServiceLost(serviceInfo: NsdServiceInfo) {
            Log.d(
                "nsd onServiceLost",
                String.format("lost \"%s\"", serviceInfo.serviceName)
            )
        }

        override fun onDiscoveryStarted(serviceType: String) {
            Log.d(
                "nsd onDiscoveryStarted",
                "service discovery started"
            )
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.d(
                "nsd onDiscoveryStopped",
                "service discovery stopped"
            )
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(
                "nsd onStartDiscoveryFailed",
                "unable to start service discovery"
            )
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(
                "nsd onStopDiscoveryFailed",
                "unable to stop service discovery"
            )
        }
    }


    var peerListListener =
        PeerListListener { peerList ->
            if (peerList.deviceList != peers) {
                peers.clear()
                peers.addAll(peerList.deviceList)
                deviceNameArray = arrayOfNulls<String>(peerList.deviceList.size)

                deviceArray = arrayOfNulls<WifiP2pDevice>(peerList.deviceList.size)
                var index = 0
                for (device in peerList.deviceList) {
                    deviceNameArray[index] = device.deviceName
                    deviceArray[index] = device
                    index++
                }
                peerCount++
//                if(peerCount >= 2){
                    peersScannedAtleastOnce = true
//                }

//                val dialogBuilder = AlertDialog.Builder(this)
//                dialogBuilder.setSingleChoiceItems(deviceNameArray, 0) { d, n ->
//                    val device = deviceArray[n]!!
////                String client_mac_fixed = new String(device.deviceAddress).replace("99", "19");
//////                String clientIP = Utils.getIPFromMac(client_mac_fixed);
//                    //                String client_mac_fixed = new String(device.deviceAddress).replace("99", "19");
//////                String clientIP = Utils.getIPFromMac(client_mac_fixed);
//                    val config = WifiP2pConfig()
//                    config.deviceAddress = device.deviceAddress
//
//                    mManager!!.connect(mChannel, config, object : WifiP2pManager.ActionListener {
//                        override fun onSuccess() {
//                            Toast.makeText(
//                                applicationContext,
//                                "connected to " + device.deviceName,
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//
//                        override fun onFailure(reason: Int) {
//                            Toast.makeText(
//                                applicationContext,
//                                "Not connected",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    })
//                }
//                dialogBuilder.setNegativeButton("Cancel", null)
//                dialogBuilder.setTitle("Which one?")
//                dialogBuilder.show()
//                var mseg = "\nPeers"
//                for (dev in peerList.device List) {
//                    mseg += """
//
//                        ${dev.deviceAddress}
//                        """.trimIndent()
//                }
//                Log.d("onPeersAvailable", mseg)
            }
            if (peers.size == 0) {
                Toast.makeText(applicationContext, "No device Found", Toast.LENGTH_SHORT)
                    .show()
                return@PeerListListener
            }
        }

    var connectionInfoListener =
        ConnectionInfoListener { info ->
            val groupOwnerAddress = info.groupOwnerAddress
            if (info.groupFormed && info.isGroupOwner) {
                Log.d("Connection Status", "HOST")
                Log.d("ConnInfoListener", "I am GO")
                groupCreated = true
                if (!serverCreated) {
                    serverClass = ServerClass()
                    serverClass!!.start()
                    serverCreated = true

                    // Register the service
                    nsdManager = applicationContext.getSystemService(Context.NSD_SERVICE) as NsdManager
                    var nsdServiceInfo = NsdServiceInfo()
                    nsdServiceInfo.serviceType = SERVICE_TYPE
                    SERVICE_NAME = serverClass?.socket?.inetAddress.toString()
                    nsdServiceInfo.serviceName = SERVICE_NAME
                    nsdServiceInfo.port = 2323
                    nsdManager.registerService(nsdServiceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener)
                }
            } else if (info.groupFormed) {
                Log.d("Connection Status", "Client")
                clientClass = ClientClass(groupOwnerAddress)
                clientClass!!.start()
                serverCreated = false
                groupCreated = false

            } else {
                groupCreated = false
                serverCreated = false
            }
        }

    private val mRegistrationListener: RegistrationListener = object : RegistrationListener {
        override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
            Log.i(
                "mRegistrationListener",
                "service registered"
            )
        }

        override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
            Log.i(
                "mRegistrationListener",
                "service unregistered"
            )
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(
                "mRegistrationListener",
                String.format("registration failed: %d", errorCode)
            )
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(
                "mRegistrationListener",
                String.format("unregistration failed: %d", errorCode)
            )
        }
    }
    override fun onResume() {
        super.onResume()
        registerReceiver(mReceiver, mIntentFilter)

    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mReceiver)
    }

    companion object{
        var netAddrSendReceiveHashMap: ConcurrentHashMap<InetAddress, SendReceive>? = ConcurrentHashMap()
        var sendReceive: SendReceive? = null
        var groupCreated = false
        var serverCreated = false
        const val MESSAGE_READ = 1
        var peersScannedAtleastOnce = false
        var wifiScannedAtleastOnce = false
        var checkedForGroups = false

        const val SERVICE_TYPE = "_helpapp._tcp."
        var peers: ArrayList<WifiP2pDevice> = ArrayList<WifiP2pDevice>()
        var ssidList = ArrayList<String>()
        var resultList = ArrayList<ScanResult>()
        var SERVICE_NAME : String? = null
        lateinit var deviceNameArray: Array<String?>
        lateinit var deviceArray: Array<WifiP2pDevice?>

        var nameOfGO: String? = null
        var nameOfConnectedGOHotspot: String? = null

        fun broadcastMessage(msg: String, context: Context): Boolean {
            if(sendReceive != null || netAddrSendReceiveHashMap?.size!! > 0) {
                val broadcastMessageAsyncTask = BroadcastMessageAsyncTask()
                broadcastMessageAsyncTask.execute(msg)
                return true
            }
            else{
                Log.d("broadcastMessage", "connection not established, cant send message")
                Toast.makeText(context, "Connection not established. Cannot send message.", Toast.LENGTH_LONG)
                return false
            }
        }
    }

    class BroadcastMessageAsyncTask : AsyncTask<String, String, Boolean>() {
        override fun doInBackground(vararg params: String?): Boolean {
            val msg = params[0]
            try {
                if (serverCreated) {
                    for (sendReceiveDevice in netAddrSendReceiveHashMap!!.values) {
                        try {
                            sendReceiveDevice.write(msg?.toByteArray())
                        } catch (e: Exception) {
                            Log.e("Exception is ", e.toString())
                        }
                    }
                } else {
                    sendReceive!!.write(msg?.toByteArray())
                }
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("broadcastMessage", "Error occurred, device is probably not connected to anything")
                return false
            }
        }

    }
}
