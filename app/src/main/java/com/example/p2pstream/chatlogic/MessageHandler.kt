package com.example.p2pstream.chatlogic

import android.util.Log
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

object MessageHandler {

    private val socketMap = mutableMapOf<String, Socket>()

    fun addSocket(id: String, socket: Socket) {
        socketMap[id] = socket
    }

    fun removeSocket(id: String) {
        socketMap[id]?.close()
        socketMap.remove(id)
    }


    private var socket: Socket? = null

    fun setSocket(newSocket: Socket) {
        socket = newSocket
    }

    fun close() {
        socket = null
    }


    suspend fun sendMessage(id: String, message: String) {
        withContext(Dispatchers.IO) {
//            val socket = socketMap[id]

            Log.d("!!!", "sendMessage socket == null: ${(socket == null)}")

            if (socket != null) {
//            if (socket != null && !socket.isClosed) {

                val it = socket
                if (!it!!.isClosed) {

                    val writer = BufferedWriter(OutputStreamWriter(it.getOutputStream()))
                    writer.write(message)
                    writer.newLine()
                    writer.flush()
                }
            }
        }
    }

    fun startReadingMessages(id: String, onMessageReceived: (String) -> Unit) {
//        val socket = socketMap[id]
        if (socket != null) {
            //            if (socket != null && !socket.isClosed) {


            val it = socket
            if (!it!!.isClosed) {
                CoroutineScope(Dispatchers.IO).launch {
                    val reader = BufferedReader(InputStreamReader(it.getInputStream()))
                    while (isActive) {
                        val message =
                            withContext(Dispatchers.IO) {
                                reader.readLine()
                            }
                        if (message == null) {
                            // 读取到null，表示连接已关闭，终止循环
                            break
                        }
                        withContext(Dispatchers.Main) {
                            onMessageReceived(message)
                        }


                        Log.d("!!!", "MessageHandler ReadingMessages message = $message")
                    }
                }
            }
        }
    }

}