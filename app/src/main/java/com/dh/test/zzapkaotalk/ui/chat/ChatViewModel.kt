package com.dh.test.zzapkaotalk.ui.chat

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.dh.test.zzapkaotalk.BaseViewModel
import com.dh.test.zzapkaotalk.model.ChatModel
import com.dh.test.zzapkaotalk.network.Repository
import io.reactivex.rxkotlin.plusAssign

class ChatViewModel(
    private val repository: Repository
): BaseViewModel() {
    val chatState = MutableLiveData<MutableList<ChatModel>>(ArrayList<ChatModel>())

    fun getChats(roomNo: Int) {
        compositeDisposable += repository.getChats(roomNo)
            .subscribe({
                chatState.value = it.body()?.toMutableList() ?: ArrayList()
            }, {
                Log.d("dhlog", "ChatViewModel getChats($roomNo) 실패")
                it.printStackTrace()
            })
    }

    fun chatReceived(chat: ChatModel) {
        val list = chatState.value
        list!!.add(chat)
        chatState.value = list!!
    }
}
