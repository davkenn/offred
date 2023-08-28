package com.example.renewed

import com.example.renewed.moshiadapters.DescriptionAdapter
import com.example.renewed.moshiadapters.MediaList
import com.example.renewed.moshiadapters.RedditHolderAdapter
import com.example.renewed.moshiadapters.RedditPostAdapter
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.nio.charset.StandardCharsets


class TestTools {
    companion object {
        fun loadJsonResponse(e: String): String? {

            val inputStream = this.javaClass.classLoader!!.getResource(e).openStream()
            val source = inputStream?.let { inputStream.source().buffer() }
            return source?.readString(StandardCharsets.UTF_8)
        }
    }
}
fun MockWebServer.enqueueResponse(fileName: String, code: Int) {

    val inputStream = this.javaClass.classLoader!!.getResource(fileName)
        .openStream()
    val source = inputStream?.let { inputStream.source().buffer() }
    source?.let {
        enqueue(
            MockResponse()
                .setResponseCode(code)
                .setBody(source.readString(StandardCharsets.UTF_8))
        )
    }}


fun setupTestRetrofit(
    mockWebServer: MockWebServer,
    attachRXAdapter: Boolean,
    isHostnameError: Boolean = false
): API {
    val mosh = Moshi.Builder()
        .add(RedditPostAdapter())
        .add(RedditHolderAdapter())
        .add(DescriptionAdapter())
        .add(MediaList())
        .build()
    val okHttpClient = OkHttpClient.Builder()
        .build()

    return Retrofit.Builder()
        .baseUrl(mockWebServer.url("/"))
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(mosh))
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()
        .create(API::class.java)
}