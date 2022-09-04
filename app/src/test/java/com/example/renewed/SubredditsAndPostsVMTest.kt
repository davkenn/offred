package com.example.renewed

import android.util.Log
import com.example.renewed.models.FullViewState
import com.example.renewed.models.MyEvent
import com.example.renewed.moshiadapters.DescriptionAdapter
import com.example.renewed.moshiadapters.RedditHolderAdapter
import com.example.renewed.moshiadapters.RedditPostAdapter
import com.squareup.moshi.Moshi
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.internal.schedulers.TrampolineScheduler
import io.reactivex.rxjava3.subscribers.TestSubscriber
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse

import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import okio.buffer
import okio.source
import org.hamcrest.MatcherAssert.assertThat


import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit


class SubredditsAndPostsVMTest {
    private lateinit var viewModel: SubredditsAndPostsVM
    private lateinit var fakerepo: FakeRepo2
    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var apiService: API
    @Before
    public fun setUp() {
  /**      mockWebServer = MockWebServer()

        var end = loadJsonResponse("Berserk.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end!!))
        mockWebServer.start()
        apiService =setupTestRetrofit(mockWebServer,true)
        fakerepo = FakeRepo2(apiService)
        viewModel = SubredditsAndPostsVM(fakerepo)
**/

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

        viewModel.processInput(MyEvent.ScreenLoadEvent(""))
      //    var c = b.blockingGet()
        var l = res.await(1,TimeUnit.SECONDS)


        assertThat("Is there a subscrier?",res.hasSubscription())
        res.assertNotComplete()
        res.assertNoErrors()

        res.assertValueCount(3)



    }
    @Test
    fun getRandomSubreddit() {


      //  fakerepo.prefetchSubreddits().test()
    //   var res =  fakerepo.getSubreddits().blockingGet()
      //  var l = res.get(0)

        var r = fakerepo.getSubreddits()
        var t = r.test()


           var l = t.await(1,TimeUnit.SECONDS)
        t.assertValueCount(1)
        t.assertComplete()


    }

    @Test
    fun processNetworkError() {
        mockWebServer = MockWebServer()

        var end = loadJsonResponse("Berserk.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end!!).
                setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        mockWebServer.start()
        apiService =setupTestRetrofit(mockWebServer,true)
        fakerepo = FakeRepo2(apiService)
        viewModel = SubredditsAndPostsVM(fakerepo)


        var res = viewModel.vs.test()

        viewModel.processInput(MyEvent.ScreenLoadEvent(""))

        var n = res.await(1,TimeUnit.SECONDS)

        res.assertError(IOException::class.java)

    }

    fun loadJsonResponse(e:String): String? {

        val inputStream = this.javaClass.classLoader!!.getResource(e)
            .openStream()
        val source = inputStream?.let { inputStream.source().buffer() }
        var res = source?.let { it.readString(StandardCharsets.UTF_8) }
        res
        return res
    }

    @Test
    fun processRefreshEvent() {
        viewModel.processInput(MyEvent.RemoveAllSubreddits(listOf("1fasf","asdasdas")))
        var res = viewModel.vs.test()

        //     res.assertValueCount(2)
        res.assertValueAt(0, FullViewState() )
    }

    @Test
    fun ExceptionThrown() {
    }
}