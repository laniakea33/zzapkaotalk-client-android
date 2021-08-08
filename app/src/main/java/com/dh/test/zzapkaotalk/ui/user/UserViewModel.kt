package com.dh.test.zzapkaotalk.ui.user

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.dh.test.zzapkaotalk.BaseViewModel
import com.dh.test.zzapkaotalk.model.UserModel
import com.dh.test.zzapkaotalk.network.Repository
import io.reactivex.rxkotlin.plusAssign

class UserViewModel(
    private val repository: Repository
): BaseViewModel() {

    val userState = MutableLiveData<UserModel>()

    fun postUser(deviceId: String) {
        Log.d("dhlog", "UserViewModel postUser()")
        compositeDisposable += repository.postUser(deviceId)
            .subscribe({
                Log.d("dhlog", "UserViewModel postUser() 성공")
                userState.value = it.body() ?: UserModel()
            }, {
                Log.d("dhlog", "UserViewModel postUser() 실패")
                it.printStackTrace()
            })
    }

    fun putUser(id: Int, displayName: String, profileImageUrl: String) {
        Log.d("dhlog", "UserViewModel putUser()")
        compositeDisposable += repository.putUser(id, displayName, profileImageUrl)
            .subscribe({
                Log.d("dhlog", "UserViewModel putUser() 성공")
                userState.value = it.body() ?: UserModel()
            }, {
                Log.d("dhlog", "UserViewModel putUser() 실패")
                it.printStackTrace()
            })
    }
}