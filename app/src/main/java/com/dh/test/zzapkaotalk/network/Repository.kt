package com.dh.test.zzapkaotalk.network

import com.dh.test.zzapkaotalk.model.ChatModel
import com.dh.test.zzapkaotalk.model.RoomModel
import com.dh.test.zzapkaotalk.model.UserModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

object Repository {
    private val api = RetroAPI.create()

    fun postUser(deviceId: String): Single<Response<UserModel>> {
        val params = HashMap<String, Any>()
        params["device_id"] = deviceId

        return api.postUser(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun putUserDisplayName(id: Int, displayName: String): Single<Response<UserModel>> {
        val params = HashMap<String, Any>()
        params["id"] = id
        params["display_name"] = displayName

        return api.putUser(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun putUserProfileImageUrl(id: Int, profileImageUrl: String): Single<Response<UserModel>> {
        val params = HashMap<String, Any>()
        params["id"] = id
        params["profile_image_url"] = profileImageUrl

        return api.putUser(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getRooms(): Single<Response<List<RoomModel>>> {
        return api.getRooms()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getChats(roomNo: Int): Single<Response<List<ChatModel>>> {
        return api.getChats(roomNo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}