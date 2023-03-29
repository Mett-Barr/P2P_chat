package com.example.p2pstream

import android.net.wifi.p2p.WifiP2pDevice
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.p2pstream.chatlogic.MessageData
import com.example.p2pstream.chatlogic.MessageHandler
import com.example.p2pstream.chatlogic.Sender
import com.example.p2pstream.network.P2pState
import com.example.p2pstream.network.WifiP2pHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.net.ServerSocket
import java.net.Socket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

const val FIND = "find"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val wifiP2pHandler: WifiP2pHandler,
    private val messageHandler: MessageHandler
) : ViewModel() {

    var pageState by mutableStateOf(PageState.NO_DEVICE)

    var list = mutableStateListOf<WifiP2pDevice>()

    private val _p2pState = MutableLiveData<P2pState>()
    val p2pState: LiveData<P2pState> = _p2pState


    var currentDeviceId = ""

    var connectingDevice = ""

    val messageList = mutableStateListOf<MessageData>()

    var textFieldValue by mutableStateOf(TextFieldValue())

    init {
        Log.d("!!!", "viewModel init")

//        viewModelScope.launch {
//        }
        wifiP2pHandler.connectionInfoListener.setOnConnectionInfoAvailableListener { info ->
            if (info.groupFormed && info.isGroupOwner) {
                _p2pState.postValue(P2pState.GroupOwner)
                Log.d("!!!", "state change viewModel")


//                setCurrentDeviceId(info.groupOwnerAddress?.hostAddress ?: "")
                startServer()
                pageState = PageState.CHAT
            } else if (info.groupFormed) {
                info.groupOwnerAddress.hostAddress?.let { groupOwnerAddress ->
                    P2pState.Client(groupOwnerAddress).let { _p2pState.postValue(it) }
                    Log.d("!!!", "state change viewModel")

//                    setCurrentDeviceId(groupOwnerAddress)
                    connectToGroupOwner(groupOwnerAddress)
                    pageState = PageState.CHAT
                }
            } else {
                if (searchJob == null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        delay(5000)
                        if (pageState != PageState.CHAT) {
                            pageState = PageState.NO_DEVICE
                            list.clear()
                        }
                    }
                }
            }
        }

        Log.d("!!!", "viewModel init - onConnectionInfoAvailableListener set")
    }

    fun findDevice() {
        list.clear()

        wifiP2pHandler.getDeviceList {
            if (it.isNotEmpty()) {
                list.addAll(it)
            }
        }
    }

    fun clickToConnect(wifiP2pDevice: WifiP2pDevice) {
        wifiP2pHandler.connectToDevice(wifiP2pDevice)
    }


    private fun startServer() {
        viewModelScope.launch(Dispatchers.IO) {
            val serverSocket = ServerSocket(SOCKET_PORT)
            while (isActive) {
                val clientSocket = serverSocket.accept()

                // 添加 Socket 到 MessageHandler
                val clientId = UUID.randomUUID().toString()
                messageHandler.addSocket(clientId, clientSocket)
                messageHandler.setSocket(clientSocket)

                currentDeviceId = clientId

                // 开始读取客户端发送的消息
                messageHandler.startReadingMessages(clientId) { message ->

                    Log.d("!!!", "Server ReadingMessages message = $message")

                    // 处理接收到的消息
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        messageList.add(
                            MessageData(
                                message = message,
                                time = LocalDateTime.now(),
                                sender = Sender.OTHERS
                            )
                        )
                    }

                    Log.d("!!!", "Server ReadingMessages messageList = ${messageList.size}")
                }
            }
            withContext(Dispatchers.IO) {
                serverSocket.close()
            }
        }
    }

    private fun connectToGroupOwner(groupOwnerAddress: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val socket = Socket(groupOwnerAddress, SOCKET_PORT)

            // 添加 Socket 到 MessageHandler
            val id = UUID.randomUUID().toString()
            messageHandler.addSocket(id, socket)
            messageHandler.setSocket(socket)

            currentDeviceId = id


            // 开始读取服务器发送的消息
            messageHandler.startReadingMessages(id) { message ->

                Log.d("!!!", "Client ReadingMessages message = $message")

                // 处理接收到的消息
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    messageList.add(
                        MessageData(
                            message = message,
                            time = LocalDateTime.now(),
                            sender = Sender.OTHERS
                        )
                    )
                }


                Log.d("!!!", "Client ReadingMessages messageList = ${messageList.size}")
            }
        }
    }


    fun sendMessage(message: String) {

        Log.d("!!!", "currentDeviceId = $currentDeviceId ")

        if (currentDeviceId.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    messageHandler.sendMessage(
                        currentDeviceId, LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        )
                    )
                }

                // 将发送的消息添加到 messageList
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    messageList.add(
                        MessageData(
                            message = LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            ),
//                            message = message,
                            time = LocalDateTime.now(),
                            sender = Sender.ME
                        )
                    )
                }
            }
        } else {
            //
        }
    }
    fun sendMessage() {

        Log.d("!!!", "currentDeviceId = $currentDeviceId ")

        if (currentDeviceId.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    messageHandler.sendMessage(currentDeviceId, textFieldValue.text)
                }

                // 将发送的消息添加到 messageList
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    messageList.add(
                        MessageData(
                            message = textFieldValue.text,
//                            message = message,
                            time = LocalDateTime.now(),
                            sender = Sender.ME
                        )
                    )
                }

                textFieldValue = TextFieldValue()
            }
        } else {
            //
        }
    }


    fun findOrStop() {
        when (pageState) {
            PageState.NO_DEVICE -> {
                pageState = PageState.FINDING
                startSearching()
            }
            PageState.FINDING -> {
                pageState = PageState.NO_DEVICE
                stopSearching()
            }
            PageState.FIND_OUT -> {
                pageState = PageState.FINDING
                stopSearching()
                startSearching()
            }
            PageState.CONNECTING -> {

            }
            PageState.CHAT -> {

            }
        }
    }


    // about connect
    private var searchJob: Job? = null

    private fun startSearching() {
        list.clear()

        searchJob = viewModelScope.launch(Dispatchers.IO) {

//            for (i in 0..3) {
//
//                wifiP2pHandler.getDeviceList {
//                    if (it.isNotEmpty()) {
//                        list.addAll(it)
//                        stopSearching()
////                        pageState = PageState.FIND_OUT
//                    }
//                }
//
//            }
            repeat(20) {
                wifiP2pHandler.getDeviceList {
                    if (it.isNotEmpty()) {
                        list.addAll(it)

                        pageState = PageState.FIND_OUT

                        Log.d(FIND, "startSearching: list isNotEmpty()")
                        Log.d(FIND, "startSearching: list size = ${list.size}")
                        Log.d(FIND, "pageState = $pageState")


                        stopSearching()
//                        pageState = PageState.FIND_OUT
                    }
                }

                delay(500)
                Log.d(FIND, "startSearching: repeat = $it")

                if (list.isNotEmpty()) {
                    pageState = PageState.FIND_OUT

                    Log.d(FIND, "startSearching: list isNotEmpty()")
                    Log.d(FIND, "pageState = $pageState")

                    return@repeat
                }
            }
            if (list.isEmpty()) {
                pageState = PageState.NO_DEVICE
                Log.d(FIND, "startSearching: list isEmpty()")
                Log.d(FIND, "pageState = $pageState")
            }
        }
    }

    private fun stopSearching() {
        searchJob?.cancel()
        searchJob = null

    }


    override fun onCleared() {
        super.onCleared()

        messageHandler.close()
        viewModelScope.cancel()
//        CoroutineScope(Dispatchers.IO).cancel()
    }
}

enum class PageState {
    NO_DEVICE, FINDING, FIND_OUT, CONNECTING, CHAT
}

//enum class P2PState {
//    OFF_LINE, SERVER, CLIENT
//}