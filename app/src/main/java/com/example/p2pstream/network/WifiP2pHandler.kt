package com.example.p2pstream.network

import android.content.Context
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import com.example.p2pstream.SOCKET_PORT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket
import javax.inject.Inject

class WifiP2pHandler @Inject constructor(
    context: Context,
    var connectionInfoListener: MyConnectionInfoListener = MyConnectionInfoListener
) {

    val manager: WifiP2pManager =
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    val channel: WifiP2pManager.Channel =
        manager.initialize(context, context.mainLooper, null)


    private fun discoverPeers() {
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("WiFiDirect", "Discover peers successfully")
            }

            override fun onFailure(reason: Int) {
                Log.e("WiFiDirect", "Discover peers failed: $reason")
            }
        })
    }

    fun connectToDevice(device: WifiP2pDevice, onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            wps.setup = WpsInfo.PBC
        }

        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("WiFiDirect", "Connection request sent successfully")
                onSuccess()


                // Move requestConnectionInfo here
                manager.requestConnectionInfo(channel, connectionInfoListener)
            }

            override fun onFailure(reason: Int) {
                Log.e("WiFiDirect", "Connection request failed: $reason")

                onFailure()
            }
        })


//        manager.requestConnectionInfo(channel, connectionInfoListener)
    }

    fun getDeviceList(onDeviceListAvailable: (List<WifiP2pDevice>) -> Unit) {
        discoverPeers()


        manager.requestPeers(channel) { peers ->
            onDeviceListAvailable(peers.deviceList.toList())
        }
    }
}


//fun CoroutineScope.startServer() {
//    this.launch(Dispatchers.IO) {
//        val serverSocket = ServerSocket(SOCKET_PORT)
//        while (isActive) {
//            val clientSocket = serverSocket.accept()
//            // 处理传入的连接
//            val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
//            val writer = BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream()))
//
//            // 读取客户端发送的数据
//            val clientData = reader.readLine()
//            Log.d("p2p Server", "Received data from client: $clientData")
//
//            // 向客户端发送 "1234"
//            writer.write("1234")
//            writer.newLine()
//            writer.flush()
//
//            // 关闭连接
//            clientSocket.close()
//        }
//        serverSocket.close()
//    }
//}
//
//fun CoroutineScope.connectToGroupOwner(groupOwnerAddress: String) {
//    this.launch(Dispatchers.IO) {
//        val socket = Socket(groupOwnerAddress, SOCKET_PORT)
//
//        // 已连接到组主
//        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
//        val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
//
//        // 向服务器发送数据
//        writer.write("Hello, server!")
//        writer.newLine()
//        writer.flush()
//
//        // 读取服务器响应
//        val serverResponse = reader.readLine()
//        Log.d("p2p Client", "Received response from server: $serverResponse")
//
//        // 关闭连接
//        socket.close()
//    }
//}
