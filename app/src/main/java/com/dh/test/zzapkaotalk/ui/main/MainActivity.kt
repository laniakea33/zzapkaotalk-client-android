package com.dh.test.zzapkaotalk.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.dh.test.zzapkaotalk.*
import com.dh.test.zzapkaotalk.databinding.ActivityMainBinding
import com.dh.test.zzapkaotalk.model.RoomLastChatUpdateModel
import com.dh.test.zzapkaotalk.model.RoomModel
import com.dh.test.zzapkaotalk.model.RoomRemoveModel
import com.dh.test.zzapkaotalk.network.Repository
import com.dh.test.zzapkaotalk.ui.chat.ChatActivity
import com.dh.test.zzapkaotalk.ui.user.UserActivity
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.EngineIOException
import org.json.JSONObject

class MainActivity : BaseActivity() {

    override lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainAdapter

    private lateinit var socket: Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = MainViewModel(Repository)
        binding = DataBindingUtil
            .setContentView<ActivityMainBinding>(this, R.layout.activity_main)
            .apply {
                this.lifecycleOwner = this@MainActivity
                this.viewModel = this@MainActivity.viewModel
            }

        title = "짭카오톡"

        binding.viewModel = viewModel
        adapter = MainAdapter(this, viewModel)
        binding.recyclerView.adapter = adapter

        initViewModel()
        viewModel.postUser(Preferences.deviceId)
    }

    private fun initSocket() {
        try {
            socket = IO.socket("${Const.BASE_URL}/room")

            socket.on(Socket.EVENT_CONNECT) {
                Log.d("dhlog", "연결 성공")
                initViews()
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

    private fun initViews() {
        binding.addRoomButton.setOnClickListener {
            sendMakeRoomMessage()
        }
        binding.profileButton.setOnClickListener {
            moveToProfile()
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

    private fun initViewModel() {
        viewModel.userState.observe(this) {
            Log.d("dhlog", "MainActivity userState observe : $it")
            UserHolder.userModel = it
            viewModel.getRooms()
            initSocket()
        }

        viewModel.roomListState.observe(this) {
            Log.d("dhlog", "MainActivity roomListState observe : ${it.size}")
            adapter.notifyDataSetChanged()
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
}