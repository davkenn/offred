package com.example.renewed


 import com.example.renewed.TestTools.Companion.loadJsonResponse
import com.example.renewed.models.FullViewState
import com.example.renewed.models.MyEvent
import com.example.renewed.models.PartialViewState
import com.example.renewed.repos.BaseSubredditsAndPostsRepo
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
    private lateinit var fakerepo: BaseSubredditsAndPostsRepo
    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var apiService: API

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        apiService =setupTestRetrofit(mockWebServer,true)
        fakerepo = FakeRepo2(apiService)
        viewModel = SubredditsAndPostsVM(fakerepo)


        //    viewModel = SubredditsAndPostsVM(SubredditsAndPostsRepository(API., null,null))
    }



    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun prefetch() {
        fakerepo.prefetchSubreddits()
        fakerepo.prefetchPosts()
    }

    @Test
    fun processInput() {
        //GIVEN
        val end = loadJsonResponse("Berserk.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end!!))
     //   mockWebServer.start()

        //WHEN
        val res = viewModel.vs.test()
        viewModel.processInput(MyEvent.ScreenLoadEvent(""))
        var l = res.await(1,TimeUnit.SECONDS)

        //THEN
        assertThat("Is there a subscrier?",res.hasSubscription())
        res.assertNotComplete()
        res.assertNoErrors()
        res.assertValueCount(2)








    }
    @Test
    fun getRandomSubreddit() {
        //GIVEN
        val end = loadJsonResponse("Berserk.json")

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end!!))

        //WHEN
        val r = fakerepo.getSubreddits("aaa")
        val t = r.test()
        var l = t.await(1,TimeUnit.SECONDS)

        //THEN
        t.assertValueCount(1)
        t.assertValue { it.size==1 }
        t.assertComplete()


    }

    @Test
    fun processNonExistingSubredditError(){
        val emptySubreddit = loadJsonResponse("handleUrlNotPointingToSubreddit.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody(emptySubreddit!!))




    }

    @Test
    fun processNetworkError() {

        val end = loadJsonResponse("Berserk.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end!!).
                setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))


        val res = viewModel.vs.test()
        viewModel.processInput(MyEvent.ScreenLoadEvent(""))
        var n = res.await(1,TimeUnit.SECONDS)
        res.assertError(IOException::class.java)

    }

    @Test
    fun getVideoSubreddit() {

        val end1 = loadJsonResponse("crtgamingabout.json")

        val end2 = loadJsonResponse("crtgamingposts.json")

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end1!!))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end2!!))


        val res = viewModel.vs.test()

        viewModel.processInput(MyEvent.ScreenLoadEvent(""))
        res.await(1,TimeUnit.SECONDS)
        res.assertValueCount(2)
        res.assertValueAt(1) { it.t3ListForRV!!.vsT3!![0].url=="https://v.redd.it/cx5ll43oe31a1/DASH_1080.mp4?source=fallback" }
        res.assertNotComplete()

    }

    @Test
    fun gallerySubredditLoadsImageUrls() {

        val end1 = loadJsonResponse("crtgamingabout.json")

        val end2 = loadJsonResponse("crtgamingposts.json")

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end1!!))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end2!!))


        val res = viewModel.vs.test()

        viewModel.processInput(MyEvent.ScreenLoadEvent(""))
        res.await(3,TimeUnit.SECONDS)
        res.assertValueCount(2)
        res.assertValueAt(1) { it.t3ListForRV!!.vsT3!![3].galleryUrls!=null }
        res.assertNotComplete()

    }

    @Test
    fun getVideoPost()
    {
        val end1 = loadJsonResponse("crtgamingabout.json")

        val end2 = loadJsonResponse("crtgamingposts.json")

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end1!!))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end2!!))

        val res = viewModel.vs.test()

        viewModel.processInput(MyEvent.ScreenLoadEvent(""))
        res.await(1,TimeUnit.SECONDS)
        res.assertValueCount(2)
        res.assertValueAt(1) {
            it.t3ListForRV!!.vsT3!![0].url=="https://v.redd.it/cx5ll43oe31a1/DASHPlaylist" +
                    ".mpd?a=1671522034%2CZWMzZTFjMzc5ZjI4OTViOGM1MWQ5MmJiYjAxNGMxZWYwNTlkMTM4YT" +
                                        "YxMjQzYTU0MTIwNmQ1NDIxM2ZiNmZiMw%3D%3D&amp;v=1&amp;f=sd" }
        res.assertNotComplete()


    }

    @Test
    fun getRidOfEmptyFullViewStateAsFirstEmission()
    {
        val end = loadJsonResponse("Berserk.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end!!))
        val res = viewModel.vs.test()
        viewModel.processInput(MyEvent.ScreenLoadEvent(""))
        var n = res.await(1,TimeUnit.SECONDS)

        res.assertValueAt(0) { it != FullViewState() }

    }

/**
    @Test
    fun processRefreshEvent() {
        viewModel.processInput(MyEvent.RemoveAllSubreddits(listOf("1fasf","asdasdas")))
        var res = viewModel.vs.test()

        //     res.assertValueCount(2)
        res.assertValueAt(0, FullViewState() )
    }
**/
    @Test
    fun exceptionThrown() {


}

    @Test
    fun doNotFirstReturnEmptyViewState() {
        val end = loadJsonResponse("Berserk.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end!!))



    }

}