package com.dh.test.zzapkaotalk

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log

object OsUtil {
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String = try {
        Log.d("dhlog", "context : $context")
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}
