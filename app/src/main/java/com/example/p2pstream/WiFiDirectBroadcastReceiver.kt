package com.example.p2pstream

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
//import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.p2pstream.network.WifiP2pHandler


class WiFiDirectBroadcastReceiver(
    private val wifiP2pHandler: WifiP2pHandler,
    private val viewModel: MainViewModel
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Log.d("WiFiDirect", "Wi-Fi Direct is enabled")
                } else {
                    Log.d("WiFiDirect", "Wi-Fi Direct is not enabled")
                }
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                wifiP2pHandler.manager.requestPeers(wifiP2pHandler.channel) { peers ->
                    Log.d("WiFiDirect", "Peers available: ${peers.deviceList}")
                }
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                // Respond to new connection or disconnections
                if (isNetworkAvailable(context)) {
                    // We are connected with the other device, request connection info
                    wifiP2pHandler.manager.requestConnectionInfo(wifiP2pHandler.channel) { info ->
                        Log.d("WiFiDirect", "Connected to ${info.groupOwnerAddress}")


                        wifiP2pHandler.connectionInfoListener.onConnectionInfoAvailable(info)
//                        viewModel.onConnectionInfoAvailable(info)
                    }
                } else {
                    // We are not connected to the other device
                    Log.d("WiFiDirect", "Disconnected")
                }

            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                // Respond to this device's Wi-Fi Direct status changing
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH))
    }
}