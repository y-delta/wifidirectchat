package com.example.myapp.connections

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.widget.Toast
import com.example.myapp.MainActivity


class WifiDirectBroadcastReceiver(
    private val mManger: WifiP2pManager?,
    private val mChannel: WifiP2pManager.Channel,
    mActivity: MainActivity
) :
    BroadcastReceiver() {
    private val mActivity: MainActivity = mActivity

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION == action) {
            val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
//                Toast.makeText(context, "WIFI IS ON", Toast.LENGTH_SHORT).show()
            } else {
//                Toast.makeText(context, "WIFI IS OFF", Toast.LENGTH_SHORT).show()
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION == action) {
            mManger?.requestPeers(mChannel, mActivity.peerListListener)
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION == action) {
            if (mManger == null) {
                return
            }
            val networkInfo = intent.getParcelableExtra<NetworkInfo>(
                WifiP2pManager.EXTRA_NETWORK_INFO
            )
            if (networkInfo.isConnected) {
                mManger.requestConnectionInfo(mChannel, mActivity.connectionInfoListener)
            } else {

            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION == action) {
            //
        }
    }

}
