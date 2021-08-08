package com.dh.test.zzapkaotalk.model

import com.google.gson.annotations.SerializedName

data class RoomRemoveModel(
        @SerializedName("room_no")
        val roomNo: Int = -1
)