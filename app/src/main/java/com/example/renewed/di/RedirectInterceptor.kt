package com.example.renewed.di

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


class RedirectInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        val request: Request = chain.request()
        var response = chain.proceed(request)
        val url = response.headers["location"]
        if (response.isRedirect and request.url.toString().endsWith("random.json")){
            response.close()
            response = chain.proceed(request.newBuilder().url((url?.substringBefore(".json?") ) +"about.json?").build())
        }

        return response
    }

}
