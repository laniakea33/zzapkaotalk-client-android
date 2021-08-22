package com.dh.test.zzapkaotalk.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.dh.test.zzapkaotalk.*
import com.dh.test.zzapkaotalk.R
import com.dh.test.zzapkaotalk.model.RoomLastChatUpdateModel
import com.dh.test.zzapkaotalk.model.RoomModel
import com.dh.test.zzapkaotalk.model.RoomRemoveModel
import com.dh.test.zzapkaotalk.network.Repository
import com.dh.test.zzapkaotalk.ui.chat.ChatActivity
import com.dh.test.zzapkaotalk.ui.main.ui.theme.ZzapkaotalkTheme
import com.dh.test.zzapkaotalk.ui.user.UserActivity
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.EngineIOException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : BaseActivity() {

    override lateinit var viewModel: MainViewModel
    private lateinit var socket: Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
        viewModel.postUser(Preferences.deviceId)

        setContent {
            ZzapkaotalkTheme {
                ZzapKaotalkScreen(
                    viewModel,
                    this::moveToProfile,
                    this::sendMakeRoomMessage
                )
            }
        }
    }

    private fun initSocket() {
        try {
            socket = IO.socket("${Const.BASE_URL}/room")

            socket.on(Socket.EVENT_CONNECT) {
                Log.d("dhlog", "연결 성공")
            }

            socket.on(Socket.EVENT_CONNECT_ERROR) {
                Log.d("dhlog", "연결실패")
                val e = it[0] as EngineIOException
                e.printStackTrace()
            }

            socket.on(Socket.EVENT_DISCONNECT) {
                Log.d("dhlog", "연결 끊김")
            }

            socket.on("room") {
                Log.d("dhlog", "메시지 수신")
                val room = Gson().fromJson(it[0].toString(), RoomModel::class.java)
                runOnUiThread {
                    viewModel.roomReceived(room)
                    if (room.owner == UserHolder.userModel.id) {
                        moveInToRoom(room.id, room.title)
                    }
                }
                Log.d("dhlog", room.toString())
            }

            socket.on("update") {
                Log.d("dhlog", "ROOM update 메시지 수신")
                val roomLastChatUpdateModel = Gson().fromJson(it[0].toString(), RoomLastChatUpdateModel::class.java)
                Log.d("dhlog", roomLastChatUpdateModel.toString())
                runOnUiThread {
                    viewModel.lastChatUpdated(roomLastChatUpdateModel)
                }
            }

            socket.on("remove") {
                Log.d("dhlog", "REMOVE 메시지 수신 : ${it.size}")
                Log.d("dhlog", it[0].toString())
                val roomRemoveModel = Gson().fromJson(it[0].toString(), RoomRemoveModel::class.java)
                runOnUiThread {
                    Log.d("dhlog", "roomRemoveModel.roomNo : ${roomRemoveModel.roomNo}")
                    viewModel.removeRoomReceived(roomRemoveModel.roomNo)
                }
                Log.d("dhlog", roomRemoveModel.roomNo.toString())
            }

            socket.connect()
        } catch (e: Exception) {
            Log.d("dhlog", "${e.message}")
            e.printStackTrace()
        }
    }

    private fun initViewModel() {
        viewModel = MainViewModel(Repository)

        viewModel.userState.observe(this) {
            Log.d("dhlog", "MainActivity userState observe : $it")
            UserHolder.userModel = it
            viewModel.getRooms()
            initSocket()
        }

        viewModel.roomListState.observe(this) {
            Log.d("dhlog", "MainActivity roomListState observe : ${it.size}")
        }

        viewModel.itemClickState.observe(this) {
            moveInToRoom(it.id, it.title)
        }
    }

    private fun moveInToRoom(roomNo: Int, roomName: String) {
        Intent(this, ChatActivity::class.java)
            .apply {
                putExtra("roomNo", roomNo)
                putExtra("roomName", roomName)
            }
            .also {
                startActivity(it)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::socket.isInitialized && socket.connected()) {
            Log.d("dhlog", "소켓 연결 해제")
            try {
                socket.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun moveToProfile() {
        Intent(this, UserActivity::class.java)
            .also { startActivity(it) }
    }

    private fun sendMakeRoomMessage() {
        val j = JSONObject()
        j.put("title", UserHolder.userModel.displayName)
        j.put("unread_count", 0)
        j.put("owner", UserHolder.userModel.id)

        socket.emit("create", j)
        Log.d("dhlog", "ROOM 메시지 발신")
        Log.d("dhlog", j.toString())
    }
}

@Composable
fun ZzapKaotalkScreen(
    viewModel: MainViewModel,
    onProfileClick: () -> Unit,
    onMakeRoomClick: () -> Unit
) {
    val list by viewModel.roomListState.observeAsState(listOf())
    Scaffold(
        topBar = {
            ZzapTopAppBar(
                onProfileClick,
                onMakeRoomClick
            )
        },
    ) {
        Surface {
            RoomList(
                list = list,
                viewModel::onClick
            )
        }
    }
}

@Composable
fun ZzapTopAppBar(
    onProfileClick: () -> Unit,
    onMakeRoomClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(text = "짭카오톡")
        },
        actions = {
            IconButton(onClick = {
                onMakeRoomClick()
            }) {
                Icon(
                    imageVector = Icons.Filled.Chat,
                    contentDescription = null
                )
            }
            IconButton(onClick = {
                onProfileClick()
            }) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ZzapTopAppBarPreview() {
    ZzapkaotalkTheme {
        ZzapTopAppBar({}, {})
    }
}

@Composable
fun RoomList(
    list: List<RoomModel>,
    onClick: (RoomModel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        Log.d("dhlog", "MainActivity RoomList : ${list.size}")
        items(list.size) {
            RoomItem(room = list[it], onClick = onClick)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RoomListPreview() {
    ZzapkaotalkTheme {
        val list = listOf<RoomModel>().toMutableList()
        for(i in 0..100) {
            list.add(makeDummyRoom())
        }
        RoomList(list, {})
    }
}

@Composable
fun RoomItem(room: RoomModel, onClick: (RoomModel) -> Unit, modifier: Modifier = Modifier) {
    Log.d("dhlog", room.toString())
    Row(modifier = modifier
        .background(MaterialTheme.colors.surface)
        .clickable(onClick = { onClick(room) })
        .padding(8.dp)
        .fillMaxWidth()
        .height(80.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape
        ) {
            Image(
                painter = rememberImagePainter(
                    data = room.imageSrc ?: R.drawable.ic_baseline_account_circle_24
                ),
                contentScale = ContentScale.Crop,
                contentDescription = "",
                modifier = Modifier.size(80.dp)
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .weight(1f)
                .fillMaxHeight()
        ) {
            val title = if (room.title.isNotBlank()) room.title else "타이틀 없음"
            val lastChat = if (!room.lastChat.isNullOrBlank()) room.lastChat else "대화가 없어요ㅠㅠ"

            Text(
                text = title,
                style = MaterialTheme.typography.h6,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Text(
                text = lastChat,
                style = MaterialTheme.typography.body2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
        ) {
            Text(
                text = getTimeText(room.createdDateTime),
                style = MaterialTheme.typography.body2
            )

            if (room.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .width(32.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                ) {
                    val unreadCount = room.unreadCount
                    Text(
                        text = if (unreadCount > 99) { "99+" } else { unreadCount.toString() },
                        style = MaterialTheme.typography.body2,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(Color.Red)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

private fun getTimeText(time: Date?) = if (time != null) {
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(time)
} else {
    "null"
}

@Composable
@Preview(showBackground = true)
fun RoomItemPreview() {
    ZzapkaotalkTheme {
        RoomItem(
            room = makeDummyRoom(),
            onClick = {}
        )
    }
}

private fun makeDummyRoom(): RoomModel {
    return RoomModel(
        id = 1,
        imageSrc = null,
        title = "타이틀",
        lastChat = "라스트 챗 라스트 챗 라스트 챗 라스트 챗 라스트 챗 라스트 챗 라스트 챗 라스트 챗 라스트 챗 라스트 챗 라스트 챗",
        createdDateTime = Date(),
        unreadCount = 129,
        owner = 3
    )
}