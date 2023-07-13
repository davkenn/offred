package com.example.renewed

import android.util.Base64
import okhttp3.Interceptor
import okhttp3.Response

//copied (mostly) from example at https://www.reddit.com/r/redditdev/comments/kh6wov/userless_response_returning_null_access_tokens/
class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()

            .addHeader("User-Agent", "android:com.example.renewed:v1.0 (by /u/WorrySufficient4009)")
            .build()

        return chain.proceed(request)
    }
}