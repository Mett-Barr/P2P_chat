package com.example.p2pstream.network

import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log

object MyConnectionInfoListener: WifiP2pManager.ConnectionInfoListener {

    private var onConnectionInfoAvailableCallback: ((WifiP2pInfo) -> Unit)? = null

    fun setOnConnectionInfoAvailableListener(onConnectionInfoAvailableCallback: (WifiP2pInfo) -> Unit) {
        this.onConnectionInfoAvailableCallback = onConnectionInfoAvailableCallback

        Log.d("!!!", "onConnectionInfoAvailable: callback == null ${isCallbackNull()}")

    }

    override fun onConnectionInfoAvailable(info: WifiP2pInfo) {
        onConnectionInfoAvailableCallback?.let { callback ->
            Log.d("!!!", "onConnectionInfoAvailable: $info")
            callback(info)
        } ?: run {
            Log.d("!!!", "onConnectionInfoAvailable: callback not set")
        }
    }

    private fun isCallbackNull(): Boolean = onConnectionInfoAvailableCallback == null
}

sealed class P2pState {
    object OffLine : P2pState()
    object GroupOwner : P2pState()
    data class Client(val address: String) : P2pState()
}