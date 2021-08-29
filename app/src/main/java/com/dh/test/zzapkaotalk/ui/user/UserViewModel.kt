package com.dh.test.zzapkaotalk.ui.user

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.dh.test.zzapkaotalk.BaseViewModel
import com.dh.test.zzapkaotalk.UserHolder
import com.dh.test.zzapkaotalk.model.UserModel
import com.dh.test.zzapkaotalk.network.Repository
import io.reactivex.rxkotlin.plusAssign

class UserViewModel(
    private val repository: Repository
): BaseViewModel() {

    val userDisplayName = mutableStateOf(UserHolder.userModel.displayName)
    val userProfileImageUrl = mutableStateOf(UserHolder.userModel.profileImageUrl)

    fun postUser(deviceId: String) {
        Log.d("dhlog", "UserViewModel postUser()")
        compositeDisposable += repository.postUser(deviceId)
            .subscribe({
                Log.d("dhlog", "UserViewModel postUser() 성공")
                val userModel = it.body() ?: UserModel()
                UserHolder.userModel = userModel
                userDisplayName.value = UserHolder.userModel.displayName
                userProfileImageUrl.value = UserHolder.userModel.profileImageUrl
            }, {
                Log.d("dhlog", "UserViewModel postUser() 실패")
                it.printStackTrace()
            })
    }

    fun putUserDisplayName() {
        Log.d("dhlog", "UserViewModel putUserDisplayName()")
        val id = UserHolder.userModel.id
        val displayName = userDisplayName.value

        compositeDisposable += repository.putUserDisplayName(id, displayName)
            .subscribe({
                Log.d("dhlog", "UserViewModel putUserDisplayName() 성공")
                val userModel = it.body() ?: UserModel()
                UserHolder.userModel = userModel
                userDisplayName.value = UserHolder.userModel.displayName
            }, {
                Log.d("dhlog", "UserViewModel putUserDisplayName() 실패")
                it.printStackTrace()
            })
    }

    fun putUserProfileImageUrl(profileImageUrl: String) {
        Log.d("dhlog", "UserViewModel putUserProfileImageUrl()")
        val id = UserHolder.userModel.id

        compositeDisposable += repository.putUserProfileImageUrl(id, profileImageUrl)
            .subscribe({
                Log.d("dhlog", "UserViewModel putUserProfileImageUrl() 성공")
                val userModel = it.body() ?: UserModel()
                UserHolder.userModel = userModel
                userProfileImageUrl.value = UserHolder.userModel.profileImageUrl
            }, {
                Log.d("dhlog", "UserViewModel putUserProfileImageUrl() 실패")
                it.printStackTrace()
            })
    }

    fun onDisplayNameChanged(s: String) {
        Log.d("dhlog", "UserViewModel onDisplayNameChanged >> $s")
        userDisplayName.value = s
    }
}