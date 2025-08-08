package com.example.offred


import io.reactivex.rxjava3.core.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthAPI {
        @FormUrlEncoded
        @POST("https://www.reddit.com/api/v1/access_token")
        fun installedClient(
            @Header("Authorization") token: String,
            @Field("grant_type") grantType: String,
            @Field("device_id") deviceId: String
        ) : Single<Map<String,String>>

}