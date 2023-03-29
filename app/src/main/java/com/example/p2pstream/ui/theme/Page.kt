package com.example.p2pstream.ui.theme

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.p2pstream.FIND
import com.example.p2pstream.MainViewModel
import com.example.p2pstream.PageState
import com.example.p2pstream.chatlogic.Sender
import com.example.p2pstream.network.P2pState

@Composable
fun TestPage(viewModel: MainViewModel = viewModel()) {
    Box(modifier = Modifier.fillMaxSize()) {
        Button(onClick = {}, Modifier.align(alignment = Alignment.Center)) {
            Text(text = "test")
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun ConnectPage(viewModel: MainViewModel = viewModel()) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), contentAlignment = Alignment.Center
        ) {
            when (viewModel.pageState) {
                PageState.NO_DEVICE -> {
                    Text(text = "No Device")
                }
                PageState.FINDING -> {
                    CircularProgressIndicator()
                }
                PageState.FIND_OUT -> {

                    Log.d(FIND, "list size = ${viewModel.list.size}")
                    Log.d(FIND, "list = ${viewModel.list}")


                    LazyColumn(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(viewModel.list) {
                            Card(onClick = { viewModel.clickToConnect(it) }) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = it.deviceAddress.toString())
                                    Text(text = it.deviceName.toString())
                                    Text(text = it.primaryDeviceType.toString())
                                }
                            }
                        }
                    }
                }
                PageState.CONNECTING -> {
                    CircularProgressIndicator()
                }
                PageState.CHAT -> {}
            }
        }
        Button(onClick = { viewModel.findOrStop() }) {
            AnimatedContent(viewModel.pageState) {
                Text(
                    text =
                    when (it) {
                        PageState.NO_DEVICE -> "Find Devices"
                        PageState.FINDING -> "Stop"
                        PageState.FIND_OUT -> "Find Devices"
                        PageState.CONNECTING -> ""
                        PageState.CHAT -> ""
                    }
                )
            }
        }
    }
}

@Composable
fun ChatPage(viewModel: MainViewModel = viewModel()) {

    val state by viewModel.p2pState.observeAsState()

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally,) {
        Text(
            text = when (state) {
                is P2pState.OffLine -> "Off Line"
                is P2pState.Client -> "Client"
                is P2pState.GroupOwner -> "Server"
                else -> "Off Line"
            },
            style = MaterialTheme.typography.h4
        )

//        Text(viewModel.currentDeviceId)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(10.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colors.onBackground.copy(0.25f))
                .padding(8.dp)
        ) {
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            LazyColumn(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(16.dp),
//                reverseLayout = true
            ) {
                items(viewModel.messageList) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Card(
                            modifier = Modifier
                                .align(
                                    when (it.sender) {
                                        Sender.ME -> Alignment.CenterEnd
                                        Sender.OTHERS -> Alignment.CenterStart
                                    }
                                ),
//                                .padding(16.dp),
                            shape = when (it.sender) {
                                Sender.ME -> RoundedCornerShape(topEnd = 24.dp, topStart = 24.dp, bottomStart = 24.dp)
                                Sender.OTHERS -> RoundedCornerShape(topEnd = 24.dp, topStart = 24.dp, bottomEnd = 24.dp)
                            },

                        ) {
                            Text(
                                text = it.message,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = viewModel.textFieldValue,
                onValueChange = { viewModel.textFieldValue = it },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(16.dp))
            Button(onClick = { viewModel.sendMessage() }) {
                Text(text = "Send")
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Testing(viewModel: MainViewModel = viewModel()) {

    val state by viewModel.p2pState.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { viewModel.findDevice() }) {
            Text(text = "find device")
        }
        Text(
            text = when (state) {
                is P2pState.OffLine -> "Off Line"
                is P2pState.Client -> "Client"
                is P2pState.GroupOwner -> "Server"
                else -> "Off Line"
            }
        )
        Text(viewModel.currentDeviceId)
        LazyColumn(Modifier.height(150.dp)) {
            items(viewModel.list) {
                Card(onClick = { viewModel.clickToConnect(it) }) {
                    Column {
                        Text(text = it.deviceAddress.toString())
                        Text(text = it.deviceName.toString())
                        Text(text = it.primaryDeviceType.toString())
//                        Text(text = it.secondaryDeviceType.toString())
                    }
                }
            }
        }

        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(viewModel.messageList) {
                Box(modifier = Modifier.fillMaxWidth()) {
//                    Text(text = it.message, color =
//                        when(it.sender) {
//                            Sender.ME -> Color.White
//                            Sender.OTHERS -> Color.Green
//                        }
//                    )
                    Text(
                        text = it.message, modifier = Modifier.align(
                            when (it.sender) {
                                Sender.ME -> Alignment.CenterEnd
                                Sender.OTHERS -> Alignment.CenterStart
                            }
                        )
                    )
                }
            }
        }

        Button(onClick = { viewModel.sendMessage("") }, enabled = state != P2pState.OffLine) {
            Text(text = "send")
        }

//        viewModel.currentDeviceId.value = ""
    }
}