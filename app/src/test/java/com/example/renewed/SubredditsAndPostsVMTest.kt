package com.example.renewed

import com.example.renewed.TestTools.Companion.loadJsonResponse
import com.example.renewed.models.FullViewState
import com.example.renewed.models.MyEvent
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse

import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.hamcrest.MatcherAssert.assertThat


import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeUnit


class SubredditsAndPostsVMTest {
    private lateinit var viewModel: SubredditsAndPostsVM
    private lateinit var fakerepo: FakeRepo2
    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var apiService: API
    @Before
    public fun setUp() {
        mockWebServer = MockWebServer()

        var end = loadJsonResponse("Berserk.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end!!))
        mockWebServer.start()
        apiService =setupTestRetrofit(mockWebServer,true)
        fakerepo = FakeRepo2(apiService)
        viewModel = SubredditsAndPostsVM(fakerepo)


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
        var l = res.await(1,TimeUnit.SECONDS)

        assertThat("Is there a subscrier?",res.hasSubscription())
        res.assertNotComplete()
        res.assertNoErrors()

        res.assertValueCount(3)



    }
    @Test
    fun getRandomSubreddit() {


        var r = fakerepo.getSubreddits()
        var t = r.test()


           var l = t.await(1,TimeUnit.SECONDS)
        t.assertValueCount(1)
        t.assertComplete()


    }

    @Test
    fun processNetworkError() {
        mockWebServer = MockWebServer()

        var end = TestTools.loadJsonResponse("Berserk.json")
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