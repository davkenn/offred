package com.example.renewed

import com.example.renewed.models.FullViewState
import com.example.renewed.models.MyEvent
import com.example.renewed.moshiadapters.DescriptionAdapter
import com.example.renewed.moshiadapters.RedditHolderAdapter
import com.example.renewed.moshiadapters.RedditPostAdapter
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse

import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source

import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.nio.charset.StandardCharsets


class SubredditsAndPostsVMTest {
    private lateinit var viewModel: SubredditsAndPostsVM
    private lateinit var fakerepo: FakeRepo2
    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient
    @Before
    public fun setUp() {

        mockWebServer= MockWebServer()
        var mosh =Moshi.Builder()

            //TODO does order matter here?
            .add(RedditPostAdapter())
            .add(RedditHolderAdapter())
            .add(DescriptionAdapter())
            .build()
okHttpClient=     OkHttpClient.Builder()

            .build()
        var apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))

            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(mosh))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
            .create(API::class.java)
        fakerepo = FakeRepo2(apiService)
        viewModel = SubredditsAndPostsVM(fakerepo)

        val inputStream = this.javaClass.classLoader!!.getResource("Berserk.json")
            .openStream()
        val source = inputStream?.let { inputStream.source().buffer() }
        var res = source?.let{it.readString(StandardCharsets.UTF_8)}

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(res!!))
        //    viewModel = SubredditsAndPostsVM(SubredditsAndPostsRepository(API., null,null))
    }

    @After
    public fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun prefetch() {
        fakerepo.prefetchSubreddits()
    }

    @Test
    fun processInput() {
        var res = viewModel.vs.test()
        fakerepo.prefetchSubreddits()
      //  var b = fakerepo.getSubreddits()
        viewModel.processInput(MyEvent.ScreenLoadEvent(""))
      //    var c = b.blockingGet()
        res.assertNotComplete()
        res.assertNoErrors()
        res.hasSubscription()
        res.assertValueCount(4)

    }

    @Test
    fun processRefreshEvent() {
        viewModel.processInput(MyEvent.RemoveAllSubreddits(listOf("1fasf","asdasdas")))
        var res = viewModel.vs.test()

        //     res.assertValueCount(2)
        res.assertValueAt(0, FullViewState() )
    }

    @Test
    fun getVs() {
    }
}