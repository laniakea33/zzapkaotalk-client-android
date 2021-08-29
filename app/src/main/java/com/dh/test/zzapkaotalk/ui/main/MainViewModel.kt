package com.dh.test.zzapkaotalk.ui.main

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.MutableLiveData
import com.dh.test.zzapkaotalk.BaseViewModel
import com.dh.test.zzapkaotalk.model.RoomLastChatUpdateModel
import com.dh.test.zzapkaotalk.model.RoomModel
import com.dh.test.zzapkaotalk.model.UserModel
import com.dh.test.zzapkaotalk.network.Repository
import io.reactivex.rxkotlin.plusAssign

class MainViewModel(
    private val repository: Repository
): BaseViewModel() {

    val userState = MutableLiveData<UserModel>()
    val roomListItems = mutableStateListOf<RoomModel>()

    fun postUser(deviceId: String) {
        Log.d("dhlog", "MainViewModel postUser()")
        compositeDisposable += repository.postUser(deviceId)
            .subscribe({
                Log.d("dhlog", "MainViewModel postUser() 성공")
                userState.value = it.body() ?: UserModel()
            }, {
                Log.d("dhlog", "MainViewModel postUser() 실패")
                it.printStackTrace()
            })
    }

    fun getRooms() {
        Log.d("dhlog", "MainViewModel getRooms()")
        compositeDisposable += repository.getRooms()
            .subscribe({
                Log.d("dhlog", "MainViewModel getRooms() 성공")
                roomListItems.clear()
                roomListItems.addAll(it.body()?.toMutableList() ?: return@subscribe)
            }, {
                Log.d("dhlog", "MainViewModel getRooms() 실패")
                it.printStackTrace()
            })
    }

    fun roomReceived(room: RoomModel) {
        val index = roomListItems.indexOfFirst {
            it.id == room.id
        }
        if (index == -1) {
            roomListItems += room
        } else {
            roomListItems[index] = room
        }
    }

    fun lastChatUpdated(roomLastChat: RoomLastChatUpdateModel) {
        val index = roomListItems.indexOfFirst {
            it.id == roomLastChat.roomNo
        }
        val room = roomListItems[index].copy(
            lastChat = roomLastChat.lastChat
        )
        roomListItems[index] = room
    }

    fun removeRoomReceived(roomNo: Int) {
        val index = roomListItems.indexOfFirst {
            it.id == roomNo
        }
        if (index != -1) {
            roomListItems.removeAt(index)
        }
    }
}