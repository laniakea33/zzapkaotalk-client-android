package com.dh.test.zzapkaotalk.ui.chat

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.dh.test.zzapkaotalk.BaseViewModel
import com.dh.test.zzapkaotalk.model.ChatModel
import com.dh.test.zzapkaotalk.network.Repository
import io.reactivex.rxkotlin.plusAssign

class ChatViewModel(
    private val repository: Repository
): BaseViewModel() {
    val chatList = mutableStateListOf<ChatModel>()

    val editingText = mutableStateOf("")

    fun getChats(roomNo: Int) {
        compositeDisposable += repository.getChats(roomNo)
            .subscribe({
                chatList.clear()
                chatList.addAll(it.body() ?: return@subscribe)
            }, {
                Log.d("dhlog", "ChatViewModel getChats($roomNo) 실패")
                it.printStackTrace()
            })
    }

    fun chatReceived(chat: ChatModel) {
        chatList += chat
    }

    fun onEditText(s: String) {
        editingText.value = s
    }
}
