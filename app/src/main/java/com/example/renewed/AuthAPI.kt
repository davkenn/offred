package com.example.renewed


import io.reactivex.rxjava3.core.Single
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthAPI {
        @FormUrlEncoded
        @POST("/api/v1/access_token")
        fun installedClient(
            @Header("Authorization") token: String,
            @Field("grant_type") grantType: String,
            @Field("device_id") deviceId: String
        ) : Single<Map<String,String>>

}