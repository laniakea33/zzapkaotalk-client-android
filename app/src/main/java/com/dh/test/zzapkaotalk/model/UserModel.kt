package com.dh.test.zzapkaotalk.model

import com.google.gson.annotations.SerializedName

data class UserModel(
        @SerializedName("id")
        var id: Int = -1,
        @SerializedName("device_id")
        var deviceId: String = "",
        @SerializedName("display_name")
        var displayName: String = "",
        @SerializedName("profile_image_url")
        var profileImageUrl: String = ""
) {
        fun isLoggedin(): Boolean = id != -1
}