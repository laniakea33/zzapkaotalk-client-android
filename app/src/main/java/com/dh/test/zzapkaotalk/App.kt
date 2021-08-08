package com.dh.test.zzapkaotalk

import android.app.Application
import android.util.Log
import com.dh.test.zzapkaotalk.model.UserModel

class App: Application() {

    init {
        Log.d("dhlog", "App init()")
        instance = this
    }

    companion object {
        lateinit var instance: App
    }
}