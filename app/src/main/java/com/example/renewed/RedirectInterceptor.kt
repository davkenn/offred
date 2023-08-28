package com.example.renewed

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


class RedirectInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        var response = chain.proceed(request)
        val url = response.headers["location"]
        //handle redirects for find random subreddit urls in a special way
        if (response.isRedirect and request.url.toString().endsWith("/r/random.json")){
            response.close()
            response = chain.proceed(request.newBuilder()
                .url((url?.substringBefore(".json?") ) +"about.json?").build())
        }
        //handle random posts from specific subreddits. It's redirect returns www and we need oauth
        else if (response.isRedirect and request.url.toString().endsWith("/random.json")){
            response.close()
            response = chain.proceed(request.newBuilder()
                .url(url!!.replace("www","oauth"))
                .build())
        }
        //handle all other redirects normally
        else if (response.isRedirect) {
            response.close()
            response = chain.proceed(request.newBuilder()
                             .url(url!!).build())
        }
        return response
    }
}
