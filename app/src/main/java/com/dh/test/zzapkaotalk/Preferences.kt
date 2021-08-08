package com.dh.test.zzapkaotalk

import androidx.preference.PreferenceManager

object Preferences {
    private val context = App.instance
    private val pref = PreferenceManager.getDefaultSharedPreferences(context)

    var deviceId: String
        get() {
            var id = pref.getString("deviceId", "")
            if (id.isNullOrEmpty()) {
                id = OsUtil.getDeviceId(context)
                deviceId = id
            }
            return id
        }
        private set(value) {
            pref.edit().putString("deviceId", value).apply()
        }
}