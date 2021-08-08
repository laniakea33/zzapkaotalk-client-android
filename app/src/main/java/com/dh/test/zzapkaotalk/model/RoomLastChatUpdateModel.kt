package com.dh.test.zzapkaotalk.model

import com.google.gson.annotations.SerializedName

data class RoomLastChatUpdateModel(
        @SerializedName("room_no")
        val roomNo: Int = -1,
        @SerializedName("last_chat")
        val lastChat: String = "",
)