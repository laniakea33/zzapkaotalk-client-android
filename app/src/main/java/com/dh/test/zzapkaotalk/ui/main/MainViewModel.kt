package com.dh.test.zzapkaotalk.ui.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.dh.test.zzapkaotalk.BaseViewModel
import com.dh.test.zzapkaotalk.model.RoomModel
import com.dh.test.zzapkaotalk.model.RoomLastChatUpdateModel
import com.dh.test.zzapkaotalk.model.UserModel
import com.dh.test.zzapkaotalk.network.Repository
import io.reactivex.rxkotlin.plusAssign

class MainViewModel(
    private val repository: Repository
): BaseViewModel() {

    val userState = MutableLiveData<UserModel>()
    val roomListState = MutableLiveData<MutableList<RoomModel>>(ArrayList())
    val itemClickState = MutableLiveData<RoomModel>()

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
                roomListState.value = it.body()?.toMutableList() ?: ArrayList()
            }, {
                Log.d("dhlog", "MainViewModel getRooms() 실패")
                it.printStackTrace()
            })
    }

    fun roomReceived(room: RoomModel) {
        val list = roomListState.value ?: return
        val index = list.indexOfFirst {
            it.id == room.id
        }
        if (index == -1) {
            list.add(room)
        } else {
            list[index] = room
        }
        roomListState.value = list
    }

    fun lastChatUpdated(roomLastChat: RoomLastChatUpdateModel) {
        val list = roomListState.value ?: return
        val index = list.indexOfFirst {
            it.id == roomLastChat.roomNo
        }
        val item = list[index]
        item.lastChat = roomLastChat.lastChat
        roomListState.value = list
    }

    fun onClick(room: RoomModel) {
        itemClickState.value = room
    }

    fun removeRoomReceived(roomNo: Int) {
        val list = roomListState.value
        val i = list!!.indexOfFirst {
            it.id == roomNo
        }
        if (i != -1) {
            list.removeAt(i)
            roomListState.value = list!!
        }
    }
}