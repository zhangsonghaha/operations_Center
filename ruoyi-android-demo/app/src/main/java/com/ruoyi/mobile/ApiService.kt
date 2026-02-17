package com.ruoyi.mobile

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @POST("/login")
    fun login(@Body body: LoginBody): Call<LoginResponse>

    @GET("/getInfo")
    fun getInfo(): Call<UserInfoResponse>

    @POST("/login/qr/scan")
    fun scanQr(@Body body: QrBody): Call<Void>

    @POST("/login/qr/confirm")
    fun confirmQr(@Body body: QrConfirmBody): Call<Void>
}

data class LoginBody(val username: String, val password: String, val code: String = "", val uuid: String = "")
data class LoginResponse(val token: String, val msg: String, val code: Int)
data class UserInfoResponse(val msg: String, val code: Int, val user: UserInfo, val roles: List<String>, val permissions: List<String>)
data class UserInfo(val userId: Long, val userName: String, val nickName: String, val email: String?, val phonenumber: String?, val avatar: String?)
data class QrBody(val uuid: String)
data class QrConfirmBody(val uuid: String, val username: String)
