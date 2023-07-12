package com.example.renewed

import android.util.Base64
import okhttp3.Interceptor
import okhttp3.Response

//copied (mostly) from example at https://www.reddit.com/r/redditdev/comments/kh6wov/userless_response_returning_null_access_tokens/
class AuthInterceptor : Interceptor {
    private val credentials = "u3MaMah0dOe1IA:"
    private val encodedCredentials: String = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("Authorization", "Basic $encodedCredentials")
            .addHeader("User-Agent", "android:com.example.renewed:v1.0 (by /u/WorrySufficient4009)")
            .build()

        return chain.proceed(request)
    }
}