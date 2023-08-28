package com.example.renewed

import android.util.Base64
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(val sm:SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sm.fetchAuthToken()
        val request = chain.request().newBuilder()
        if (chain.request().url.toString().contains("oauth")) {
            request.addHeader("Authorization","bearer ${token ?: ""}")
        }
        request.addHeader("User-Agent",
            "android:com.example.renewed:v1.0 (by /u/WorrySufficient4009)")
        return chain.proceed(request.build())
    }
}