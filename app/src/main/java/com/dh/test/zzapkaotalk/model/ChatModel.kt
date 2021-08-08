package com.dh.test.zzapkaotalk.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class ChatModel(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String,   //  text, image, system
    @SerializedName("message") val message: String,
    @SerializedName("date_time") val dateTime: Date,
    @SerializedName("room_no") val roomNo: Int,
    @SerializedName("User") val user: UserModel,
)