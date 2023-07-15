package com.example.renewed

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


class RedirectInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        var response = chain.proceed(request)
        val url = response.headers["location"]
        //handle redirects for these particular urls in a special way
        if (response.isRedirect and request.url.toString().endsWith("/r/random.json")){
            response.close()
            response = chain.proceed(request.newBuilder()
                            .url((url?.substringBefore(".json?") ) +"about.json?")
                            .build())
        }
        //handle all other redirects normally
        if (response.isRedirect) {
            response.close()
            response = chain.proceed(request.newBuilder().url(url!!).build())
        }
        return response
    }
}
