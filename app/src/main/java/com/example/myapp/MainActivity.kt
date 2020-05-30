package com.example.myapp

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.DiscoveryListener
import android.net.nsd.NsdManager.RegistrationListener
import android.net.nsd.NsdServiceInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener
import android.net.wifi.p2p.WifiP2pManager.PeerListListener
import android.os.AsyncTask
import android.os.Bundle
import android.os.IBinder
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker.*
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.amitshekhar.DebugDB
import com.example.myapp.connections.*
import com.example.myapp.db.AppDatabase
import com.example.myapp.ui.groupmessage.GroupMessageFragment
import com.example.myapp.ui.directmessage.DirectMessageFragment
import com.example.myapp.ui.groupmessage.GroupMessageFragment.Companion.appDatabaseCompanion
import com.example.myapp.ui.ledger.LedgerFragment
import com.example.myapp.ui.main.ModalBottomSheet
import com.example.myapp.utils.Constants
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*
import java.util.concurrent.*


class MainActivity : AppCompatActivity() {

    lateinit var nsdManager: NsdManager
    private var peerCount = 0

    var directMessageFragment: Fragment = GroupMessageFragment()
    var globalMessageFragment: Fragment = DirectMessageFragment()
    var ledgerFragment: Fragment = LedgerFragment()

    var wifiManager: WifiManager? = null
    var mManager: WifiP2pManager? = null
    var mChannel: WifiP2pManager.Channel? = null
    var mReceiver: BroadcastReceiver? = null
    var mIntentFilter: IntentFilter? = null

    var wifiIntentFilter: IntentFilter? = null
    var wifiScanReceiver: BroadcastReceiver? = null

    var serverClass: ServerClass? = null
    var clientClass: ClientClass? = null

    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    private val modalBottomSheet = ModalBottomSheet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("DBDEBUG", DebugDB.getAddressLog())

        //Request Location Permission if not given
        if (checkCallingOrSelfPermission(applicationContext,permissions[0]) == PERMISSION_DENIED
            || checkCallingOrSelfPermission(applicationContext,permissions[1])== PERMISSION_DENIED)
        {requestPermissions(permissions, 10)}

        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()){
            putInt("com.example.myapp.MSG_ID", MSG_ID)
            commit()
        }
        val USERID = sharedPref.getString(getString(R.string.SHARED_PREF_USERID), "")
        val USERNAME = sharedPref.getString(getString(R.string.SHARED_PREF_USERNAME), "")
        var MSG_ID = sharedPref.getInt("com.example.myapp.MSG_ID", 1)
        Log.d("USERID", USERID)
        Log.d("USERNAME", USERNAME)

        if(USERID.isNullOrEmpty()){
            val randomString = UUID.randomUUID().toString().substring(0,8)
            with(sharedPref.edit()){
                putString(getString(R.string.SHARED_PREF_USERID), randomString)
                commit()
            }
            NETWORK_USERID = randomString
        } else{
            NETWORK_USERID = USERID
        }

        if(USERNAME.isNullOrEmpty()){
            NETWORK_USERNAME = NETWORK_USERID
        } else{
            NETWORK_USERNAME = USERNAME
        }

        userIdUserNameHashMap.put(NETWORK_USERID, NETWORK_USERNAME)

        Log.d("USERID-", NETWORK_USERID)
        Log.d("USERNAME-", NETWORK_USERNAME)


        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)

        mainActivityCompanion = this

        if (savedInstanceState == null) {

            MAIN_EXECUTOR = Executors.newSingleThreadExecutor()

            var appDatabase = AppDatabase.getDatabase(this.application)
            appDatabaseCompanion = appDatabase
            //supportFragmentManager.beginTransaction()
            //  .add(R.id.nav_host_fragment, globalMessageFragment, "putGlobalMessageFragment")
            //.commit()
            Log.d("first", "run once")

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

//        DEVICEMAC = wifiManager!!.connectionInfo.macAddress     //get mac address of wifi (not wifi direct)

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
            showAlertDialogForUsername()

            var createGroupOrConnect = CreateGroupOrConnect(mManager, mChannel, applicationContext)
            createGroupOrConnect.start()
        }
        /*if(id == R.id.connectToHotspot){
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Enter Username")
            val input =  EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)
            builder.setPositiveButton("Ok") { _, id ->
                var text = input.getText().toString()
                networkUsername = text
            }
            if(networkUsername.isNullOrEmpty())
                builder.show()

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
                            wifiConfig.SSID = "\"" + wifiDevice.SSID + "\""     //make sure device is connected to GO's hotspot once before
//                            wifiConfig.preSharedKey = "\""+ "aGkPCGl2" +"\""    //TODO check without this line!!!

//                            wifiConfig.SSID =  wifiDevice.SSID
                            Log.d("connectToHotspot", "attempting connect to ${wifiConfig.SSID}")
                            val list = wifiManager!!.configuredNetworks
                            for (i in list) {
                                Log.d("connectToHotspot", "inner checking ${i.SSID}")
                                if (i.SSID != null && i.SSID == "\"" + wifiDevice.SSID.toString() + "\"") {
                                    Log.d("connectToHotspot", "inner attempting connect to ${i.SSID}")
                                    wifiManager!!.disconnect()
                                    wifiManager!!.enableNetwork(i.networkId, true)
                                    wifiManager!!.reconnect()
//                                    Log.d("INETADDRESS", getLocalIpAddress()?.hostAddress)
//                                    clientClass = getLocalIpAddress()?.let { ClientClass(it) }
//                                    clientClass!!.start()
                                    break
                                }
                            }
                            try {
                                nsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager
                                nsdManager.discoverServices(
                                    SERVICE_TYPE,
                                    NsdManager.PROTOCOL_DNS_SD,
                                    mDiscoveryListener
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
                }else{
                    Toast.makeText(applicationContext, "can't join if you are GO", Toast.LENGTH_SHORT)
                }

            }
        }*/
        if (id == R.id.connectToHotspot){       // single button to connect to a hotspot
            showAlertDialogForUsername()
            wifiScannedAtleastOnce = false
            scanWifi()
            mManager!!.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d("Discover Peers", "Discover karega ab")
                }

                override fun onFailure(reason: Int) {
                    Log.d("Discover Peers", "Nahi shuru ho payi discovery")
                }
            })
            HotspotConnection(this).start()
        }
        if (id == R.id.showNetworkInformation) { // do something here
            val alert = AlertDialog.Builder(this)
            alert.setTitle("Please select")
            var input = TextView (this);
            alert.setView(input);
            input.text = ""
            var inputText = ""

            if(groupCreated){
                inputText += "Group created = true\n"
            }
            if(serverCreated && groupCreated){
                mManager!!.requestGroupInfo(mChannel) { group ->
                    inputText += ("PASSPHRASE =  ${group.passphrase}\n")
                }
            } else{
                alert.setMessage("this device is not GO, click \"Create Group\" to manually become a GO")
            }

            if(groupCreated){
                inputText += "Group created = true\n"
            }
            if(serverCreated){
                inputText += "Server created = true\n"
            }
            inputText += "USERNAME = $NETWORK_USERNAME\n"
            inputText += "USERID = $NETWORK_USERID\n"
            if(!groupCreated && !serverCreated){
                if(netAddrSendReceiveHashMap!!.size > 0)
                    inputText += "I am a client\n"
                else
                    inputText += "Not yet connected to a network\n"
            }
            inputText += "No of items in netAddrSendReceiveHashMap = ${netAddrSendReceiveHashMap?.size}"

            alert.setPositiveButton("Ok") { _, id ->

            }

            alert.setNegativeButton("Create Group"){ _, id ->
                Log.d("selected manual GO", "trying to create a group")
                showAlertDialogForUsername()
                if(!groupCreated) {
                    mManager!!.createGroup(mChannel, object : WifiP2pManager.ActionListener {
                        override fun onSuccess() {
                            Toast.makeText(
                                applicationContext,
                                "group successfully created ",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("createGroup", "Successfully created a group")
                            MainActivity.groupCreated = true

                        }

                        override fun onFailure(reason: Int) {
                            Toast.makeText(
                                applicationContext,
                                "group not created!!",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("createGroup", "group creation failure")
                        }
                    })
                }
            }
            input.text = inputText
            alert.show()
        }
        if(id == R.id.setUsername){
            showAlertDialogForUsername(true)
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

    fun showAlertDialogForUsername(forceShow: Boolean = false){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Username")
        val input =  EditText(this)
        var dialog:AlertDialog? = null
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)
        builder.setCancelable(false)
        builder.setPositiveButton("Set Username") { _, id ->
            var text = input.text.toString()
            NETWORK_USERNAME = text.trim().replace("\n", "")
            if(!NETWORK_USERNAME.isNullOrEmpty()){
//                dialog?.cancel()
            }
        }
        builder.setNegativeButton("Cancel") { _, id ->

        }
        if(forceShow || NETWORK_USERNAME.equals(NETWORK_USERID)) {
            dialog = builder.create()
            dialog?.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    var text = input.text.toString()
                    NETWORK_USERNAME = text.trim().replace("\n", "")
                    userIdUserNameHashMap.put(NETWORK_USERID, NETWORK_USERNAME)
                    val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
                    with(sharedPref.edit()){
                        putString(getString(com.example.myapp.R.string.SHARED_PREF_USERNAME), NETWORK_USERNAME)
                        commit()
                    }
                    if(!NETWORK_USERNAME.isNullOrEmpty() && !NETWORK_USERNAME.equals(NETWORK_USERID)){
                        dialog?.cancel()
                    }
                }
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setOnClickListener {
                    if(!NETWORK_USERNAME.equals(NETWORK_USERID)){
                        dialog?.cancel()
                    }
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
                Log.d("ConnInfoListener", "Broadcasting userlist")
                if(USERLIST_EXECUTOR == null){
                    USERLIST_EXECUTOR = Executors.newSingleThreadScheduledExecutor()
                    USERLIST_EXECUTOR!!.scheduleWithFixedDelay(BroadcastUserListRunnable(), 15, 10, TimeUnit.SECONDS)
                }
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
//        var sendReceive: SendReceive? = null
        var groupCreated = false
        var serverCreated = false
        const val MESSAGE_READ = 1
        var peersScannedAtleastOnce = false
        var wifiScannedAtleastOnce = false
        var checkedForGroups = false
        var receivedGroupMessage: String = ""
        var DEVICEMAC : String? = null

        var MAIN_EXECUTOR: Executor? = null
        var USERLIST_EXECUTOR: ScheduledExecutorService? = null

        lateinit var mainActivityCompanion:MainActivity

        var userIdUserNameHashMap = ConcurrentHashMap<String, String>()

        const val SERVICE_TYPE = "_helpapp._tcp."
        var peers: ArrayList<WifiP2pDevice> = ArrayList<WifiP2pDevice>()
        var ssidList = ArrayList<String>()
        var resultList = ArrayList<ScanResult>()
        var SERVICE_NAME : String? = null
        lateinit var deviceNameArray: Array<String?>
        lateinit var deviceArray: Array<WifiP2pDevice?>
        var NETWORK_USERNAME:String = ""
        var NETWORK_USERID:String = ""

        var nameOfGO: String? = null
        var nameOfConnectedGOHotspot: String? = null
        var MSG_ID : Int = 0

        fun broadcastUserList(){
            var msg = ""
            for((userid, username) in userIdUserNameHashMap){
                msg += "$userid $username\n"
            }
            broadcastMessage(msg, Constants.MESSAGE_TYPE_UNIQID_USERNAME)
        }

        fun sendDirectMessage(msg:String, recipientId: String, messageId:Int, date:Date){
            // usage - sendDirectMessage(message, userid_of_recipient, messageId, date)
            var messageType: String = Constants.MESSAGE_TYPE_DIRECT
            if(msg.isNullOrEmpty() || recipientId.isNullOrEmpty()) return
            var directMessage = "$recipientId\n$NETWORK_USERID\n$messageId\n${date.toString()}\n$msg"   //recipientid, networkuserid, messageid, date, msg
            broadcastMessage(directMessage, messageType)
        }

        fun updateSharedPref():Int{
            val sharedPref = mainActivityCompanion!!.getPreferences(Context.MODE_PRIVATE)
            with(sharedPref.edit()){
                putInt("com.example.myapp.MSG_ID", ++MSG_ID)
                commit()
            }
            return MSG_ID
        }

        fun broadcastMessage(msg: String, messageType:String = Constants.MESSAGE_TYPE_GROUP): Boolean {
            var msg = msg
            if(netAddrSendReceiveHashMap?.size!! > 0) {
                val broadcastMessageAsyncTask = BroadcastMessageAsyncTask()
                var username:String = ""
                var msgWithStartEndString = ""
                if(msg.endsWith("\n") && !msg.isNullOrEmpty()){ //what if msgtype, msgtype?
                    msg = msg.substring(0, msg.length - 1)
                }
                if(NETWORK_USERNAME.isNullOrEmpty()){
                    username = NETWORK_USERID
                } else{
                    username = NETWORK_USERNAME
                }
                msgWithStartEndString = if(messageType == Constants.MESSAGE_TYPE_GROUP){
                    messageType + "\n" + username + "\n" + msg + "\n" + messageType + "\n"
                }else if(messageType == Constants.REQUEST_TYPE_LEDGER_LIST){
                    messageType + "\n" + messageType
                } else if (messageType == Constants.MESSAGE_TYPE_DIRECT) {
                    messageType + "\n" + msg + "\n" + messageType + "\n"
                } else if(messageType == Constants.RESPONSE_TYPE_DIRECT){
                    messageType + "\n" + msg + "\n" + messageType + "\n"
                } else {
                    messageType + "\n" + msg + "\n" + messageType + "\n"
                }
//                broadcastMessageAsyncTask.execute(msgWithStartEndString)
                if(MAIN_EXECUTOR!=null)
                    MAIN_EXECUTOR!!.execute(BroadcastMessageRunnable(msgWithStartEndString))
                return true
            }
            else{
                Log.d("broadcastMessage", "connection not established, cant send message")
//                Toast.makeText(context, "Connection not established. Cannot send message.", Toast.LENGTH_LONG)
                return false
            }
        }

    }

    class BroadcastUserListRunnable : Runnable{
        override fun run() {
            broadcastUserList()
        }
    }

    class BroadcastMessageRunnable(msg:String) : Runnable {
        var msg = msg

        override fun run() {
            try {
                if (netAddrSendReceiveHashMap?.size!! > 0) {
                    for (sendReceiveDevice in netAddrSendReceiveHashMap!!.values) {
                        try {
                            sendReceiveDevice.write(msg?.toByteArray())
                        } catch (e: Exception) {
                            Log.e("Exception is ", e.toString())
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("broadcastMessage", "Error occurred, device is probably not connected to anything")
            }
        }

    }

    class BroadcastMessageAsyncTask : AsyncTask<String, String, Boolean>() {
        override fun doInBackground(vararg params: String?): Boolean {
            val msg = params[0]
            try {
                if (netAddrSendReceiveHashMap?.size!! > 0) {
                    for (sendReceiveDevice in netAddrSendReceiveHashMap!!.values) {
                        try {
                            sendReceiveDevice.write(msg?.toByteArray())
                        } catch (e: Exception) {
                            Log.e("Exception is ", e.toString())
                        }
                    }
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