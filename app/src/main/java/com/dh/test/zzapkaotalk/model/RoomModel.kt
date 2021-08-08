package com.dh.test.zzapkaotalk.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class RoomModel(
        @SerializedName("id")
        var id: Int = -1,
        @SerializedName("image_src")
        var imageSrc: String? = null,
        @SerializedName("title")
        var title: String = "",
        @SerializedName("last_chat")
        var lastChat: String = "메시지가 없습니다.",
        @SerializedName("created_date_time")
        var createdDateTime: Date? = null,
        @SerializedName("unread_count")
        var unreadCount: Int = 0,
        @SerializedName("owner")
        var owner: Int = -1
)