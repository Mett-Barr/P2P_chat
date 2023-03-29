package com.example.p2pstream

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.p2pstream.network.WifiP2pHandler
import com.example.p2pstream.ui.theme.ChatPage
import com.example.p2pstream.ui.theme.Testing
import com.example.p2pstream.ui.theme.ConnectPage
import com.example.p2pstream.ui.theme.P2PStreamTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket
import javax.inject.Inject

const val SOCKET_PORT = 8888

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var wifiP2pHandler: WifiP2pHandler

//    private lateinit var manager: WifiP2pManager
//    private lateinit var channel: WifiP2pManager.Channel
//    private lateinit var connectionInfoListener: MyConnectionInfoListener

    private lateinit var receiver: BroadcastReceiver
    private lateinit var intentFilter: IntentFilter

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        wifiP2pHandler
        WindowCompat.setDecorFitsSystemWindows(window, false)


//        viewModel

        setContent {
            P2PStreamTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                    color = MaterialTheme.colors.background
                ) {
//                    Greeting("Android")
//                    TestPage()

//                    Box(modifier = Modifier.fillMaxSize()) {
//                        Button(onClick = {
//                            CoroutineScope(Dispatchers.IO).launch{
//                                click()
//                            }
//                        }, Modifier.align(alignment = Alignment.Center)) {
//                            Text(text = "test")
//                        }
//                    }

//                    ChatPage()
                    if (viewModel.pageState != PageState.CHAT) {
                        ConnectPage()
                    } else {
//                        Testing()
                        ChatPage()
                    }
                }
            }
        }

        requestLocationPermission()
        requestNearbyWifiDevicesPermission()

        intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }

//        init()
    }

//    private fun init() {
//        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
//        channel = manager.initialize(this, mainLooper, null)
////        connectionInfoListener = MyConnectionInfoListener(
////            { startServer() }
////        ) { groupOwnerAddress ->
////            connectToGroupOwner(groupOwnerAddress)
////        }
//
//        intentFilter = IntentFilter().apply {
//            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
//            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
//            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
//            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
//        }
//
////        CoroutineScope(Dispatchers.Default).launch {
////            while (true) {
////                delay(3000)
////                delay(3000)
////            }
////        }
//    }

//    private suspend fun click() {
//        discoverPeers()
//        connectToFirstAvailableDevice()
//    }

    override fun onResume() {
        super.onResume()
        receiver = WiFiDirectBroadcastReceiver(wifiP2pHandler, viewModel)
//        receiver = WiFiDirectBroadcastReceiver(wifiP2pHandler.manager, wifiP2pHandler.channel, this)
        registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

//    private fun discoverPeers() {
//        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
//            override fun onSuccess() {
//                Log.d("WiFiDirect", "Discover peers successfully")
//            }
//
//            override fun onFailure(reason: Int) {
//                Log.e("WiFiDirect", "Discover peers failed: $reason")
//            }
//        })
//    }

//    private fun connectToDevice(device: WifiP2pDevice) {
//        val config = WifiP2pConfig().apply {
//            deviceAddress = device.deviceAddress
//            wps.setup = WpsInfo.PBC
//        }
//
//        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
//            override fun onSuccess() {
//                Log.d("WiFiDirect", "Connection request sent successfully")
//            }
//
//            override fun onFailure(reason: Int) {
//                Log.e("WiFiDirect", "Connection request failed: $reason")
//            }
//        })
//
//
//        manager.requestConnectionInfo(channel, connectionInfoListener)
//    }

//    private fun connectToFirstAvailableDevice() {
//        manager.requestPeers(channel) { peers ->
//            val firstDevice = peers.deviceList.firstOrNull()
//            if (firstDevice != null) {
//                connectToDevice(firstDevice)
//            } else {
//                Log.e("WiFiDirect", "No devices available")
//            }
//        }
//    }

    private val locationPermissionRequestCode = 1

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                locationPermissionRequestCode
            )
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, continue with Wi-Fi Direct functionality
            } else {
                // Permission denied, show a message or disable Wi-Fi Direct functionality
            }
        }

    private fun requestNearbyWifiDevicesPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.NEARBY_WIFI_DEVICES
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted, continue with Wi-Fi Direct functionality
            }

            shouldShowRequestPermissionRationale(android.Manifest.permission.NEARBY_WIFI_DEVICES) -> {
// Show an explanation to the user as to why your app needs this permission.
// After showing the explanation, request the permission again.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(android.Manifest.permission.NEARBY_WIFI_DEVICES)
                }
            }
            else -> {
// No explanation needed, directly request the permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(android.Manifest.permission.NEARBY_WIFI_DEVICES)
                }
            }
        }
    }


    private fun startServer() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {
                val serverSocket = ServerSocket(SOCKET_PORT)
                while (isActive) {
                    val clientSocket = serverSocket.accept()
                    // 处理传入的连接
                    val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                    val writer = BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream()))

                    // 读取客户端发送的数据
                    val clientData = reader.readLine()
                    Log.d("p2p Server", "Received data from client: $clientData")

                    // 向客户端发送 "1234"
                    writer.write("1234")
                    writer.newLine()
                    writer.flush()

                    // 关闭连接
                    clientSocket.close()
                }
                serverSocket.close()
            }
        }
    }

    private fun connectToGroupOwner(groupOwnerAddress: String) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {
                val socket = Socket(groupOwnerAddress, SOCKET_PORT)

                // 已连接到组主
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))

                // 向服务器发送数据
                writer.write("Hello, server!")
                writer.newLine()
                writer.flush()

                // 读取服务器响应
                val serverResponse = reader.readLine()
                Log.d("p2p Client", "Received response from server: $serverResponse")

                // 关闭连接
                socket.close()
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    P2PStreamTheme {
        Greeting("Android")
    }
}