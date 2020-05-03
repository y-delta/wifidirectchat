package com.example.myapp

import LedgerFragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pConfig
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.connections.ClientClass
import com.example.myapp.connections.SendReceive
import com.example.myapp.connections.ServerClass
import com.example.myapp.connections.WifiDirectBroadcastReceiver
import com.example.myapp.ui.directmessage.DirectMessageFragment
import com.example.myapp.ui.globalmessage.GlobalMessageFragment
import com.example.myapp.ui.main.ModalBottomSheet
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.net.InetAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class MainActivity : AppCompatActivity() {

    var directMessageFragment: DirectMessageFragment = DirectMessageFragment()
    var globalMessageFragment: GlobalMessageFragment = GlobalMessageFragment()
    var ledgerFragment: LedgerFragment = LedgerFragment()

    var wifiManager: WifiManager? = null
    var mManager: WifiP2pManager? = null
    var mChannel: WifiP2pManager.Channel? = null
    var mReceiver: BroadcastReceiver? = null
    var mIntentFilter: IntentFilter? = null
    var peers: ArrayList<WifiP2pDevice> = ArrayList<WifiP2pDevice>()
    lateinit var deviceNameArray: Array<String?>
    lateinit var deviceArray: Array<WifiP2pDevice?>
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
                applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            mManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
            mChannel = mManager!!.initialize(this, mainLooper, null)

            mReceiver = WifiDirectBroadcastReceiver(mManager, mChannel!!, this)
            mIntentFilter = IntentFilter()

            mIntentFilter!!.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            mIntentFilter!!.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            mIntentFilter!!.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            mIntentFilter!!.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
            mIntentFilter!!.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        }

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

//        initialWork()

        modalBottomSheet.show(supportFragmentManager, modalBottomSheet.tag) //implement this on WiFiDirectBroadcastReceiver when Prashant sends code
        modalBottomSheet.isCancelable = false //prevents cancelling
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
        if (id == R.id.connection) { // do something here
            Log.d("Clicked", "discover peers")
//            val builder = AlertDialog.Builder(this)
//            builder.setTitle("Enter your username:")
//            val input = EditText(this)
//            builder.setView(input)
//            builder.setPositiveButton("ENTER"){ dialog, id ->
//                dialog.cancel()
//            }

//            builder.create().show()
            mManager!!.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d("Discover Peers", "Discover karega ab")
                }

                override fun onFailure(reason: Int) {
                    Log.d("Discover Peers", "Nahi shuru ho payi discovery")
                }
            })
        }
        if(id == R.id.connectionGO){
            Log.d("Clicked", "create group")
            if (!groupCreated) {
                Log.d("Clicked", "group not created, trying to create a group")
//                    wifiManager.startScan();
//                    wifiManager.getScanResults();  //TODO not yet implemented
                mManager!!.createGroup(mChannel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Toast.makeText(applicationContext, "group successfully created ", Toast.LENGTH_SHORT).show()
                        Log.d("createGroup", "Successfully created a group")
                        groupCreated = true
                    }

                    override fun onFailure(reason: Int) {
                        Toast.makeText(applicationContext, "group not created!!", Toast.LENGTH_SHORT).show()
                        Log.d("createGroup", "group creation failure")
                    }
                })
            }
        }
        return super.onOptionsItemSelected(item)
    }

    init{
    }

    fun testDisplay(str : String){
        Log.d("String", str)
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


                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setSingleChoiceItems(deviceNameArray, 0) { d, n ->
                    val device = deviceArray[n]!!
//                String client_mac_fixed = new String(device.deviceAddress).replace("99", "19");
////                String clientIP = Utils.getIPFromMac(client_mac_fixed);
                    //                String client_mac_fixed = new String(device.deviceAddress).replace("99", "19");
////                String clientIP = Utils.getIPFromMac(client_mac_fixed);
                    val config = WifiP2pConfig()
                    config.deviceAddress = device.deviceAddress

                    mManager!!.connect(mChannel, config, object : WifiP2pManager.ActionListener {
                        override fun onSuccess() {
                            Toast.makeText(
                                applicationContext,
                                "connected to " + device.deviceName,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onFailure(reason: Int) {
                            Toast.makeText(
                                applicationContext,
                                "Not connected",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }
                dialogBuilder.setNegativeButton("Cancel", null)
                dialogBuilder.setTitle("Which one?")
                dialogBuilder.show()
                var mseg = "\nPeers"
                for (dev in peerList.deviceList) {
                    mseg += """
                        
                        ${dev.deviceAddress}
                        """.trimIndent()
                }
                Log.d("onPeersAvailable", mseg)
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
                }
            } else if (info.groupFormed) {
                Log.d("Connection Status", "Client")
                clientClass = ClientClass(groupOwnerAddress)
                clientClass!!.start()
                serverCreated = false
            } else {
                groupCreated = false
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


        fun broadcastMessage(msg: String) {
            if(sendReceive != null || netAddrSendReceiveHashMap?.size!! > 0) {
                val broadcastMessageAsyncTask = BroadcastMessageAsyncTask()
                broadcastMessageAsyncTask.execute(msg)
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
