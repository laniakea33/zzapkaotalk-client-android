package com.dh.test.zzapkaotalk.network

import com.dh.test.zzapkaotalk.model.ChatModel
import com.dh.test.zzapkaotalk.model.RoomModel
import com.dh.test.zzapkaotalk.model.UserModel
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*

interface API {
    @POST("/user")
    fun postUser(@Body param: HashMap<String, Any>): Single<Response<UserModel>>

    @PUT("/user")
    fun putUser(@Body param: HashMap<String, Any>): Single<Response<UserModel>>

    @GET("/rooms")
    fun getRooms(): Single<Response<List<RoomModel>>>

    @GET("/chats")
    fun getChats(@Query("room_no") roomNo: Int): Single<Response<List<ChatModel>>>
}