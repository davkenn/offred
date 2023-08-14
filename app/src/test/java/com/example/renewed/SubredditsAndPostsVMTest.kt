package com.example.renewed


 import com.example.renewed.Screen1.SubredditsAndPostsVM
 import com.example.renewed.TestTools.Companion.loadJsonResponse
 import com.example.renewed.models.*
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
        viewModel.processInput(Screen1Event.ScreenLoadEvent(""))
        var l = res.await(1,TimeUnit.SECONDS)

        //THEN
        assertThat("Is there a subscrier?",res.hasSubscription())
        res.assertNotComplete()
        res.assertNoErrors()
        res.assertValueCount(1)
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
        viewModel.processInput(Screen1Event.ScreenLoadEvent(""))
        var n = res.await(1,TimeUnit.SECONDS)
        res.assertError(IOException::class.java)

    }

    @Test
    fun gallerySubredditLoadsImageUrls() {

        val end1 = loadJsonResponse("crtgamingabout.json")

        val end2 = loadJsonResponse("crtgamingposts.json")

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end1!!))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end2!!))


        val res = viewModel.vs.test()

        viewModel.processInput(Screen1Event.ScreenLoadEvent(""))
        res.await(1,TimeUnit.SECONDS)
        viewModel.processInput(Screen1Event.ClickOnT5ViewEvent("t5_3c23m"))
        res.await(1,TimeUnit.SECONDS)
        res.assertValueCount(3)
        //loads 3 images for galeery
        res.assertValueAt(2) {

            it.t3ListForRV!!.vsT3!![3].galleryUrls!!.size == 3}
        res.assertNotComplete()
    }

    @Test
    fun gallerySubredditIsOnlyGalleryTypes() {

        val end1 = loadJsonResponse("crtgamingabout.json")

        val end2 = loadJsonResponse("crtgamingposts.json")

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end1!!))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end2!!))


        val res = viewModel.vs.test()

        viewModel.processInput(Screen1Event.ScreenLoadEvent(""))

        res.await(1,TimeUnit.SECONDS)
        viewModel.processInput(Screen1Event.ClickOnT5ViewEvent("t5_3c23m"))
        res.await(1,TimeUnit.SECONDS)
   //     viewModel.processInput(Screen1Event.ClickOnT3ViewEvent("t3_yzv2c3"))
     //   res.await(3,TimeUnit.SECONDS)

    //    res.assertValueCount(2)
        //loads 3 images for galeery
        res.assertValueAt(2) { it.t3ListForRV!!.vsT3!![3].hasNoThumbnail() }
        res.assertValueAt(2) { !it.t3ListForRV!!.vsT3!![3].isImagePost()}
        res.assertValueAt(2) { !it.t3ListForRV!!.vsT3!![3].isVideoPost() }
        res.assertValueAt(2) { !it.t3ListForRV!!.vsT3!![3].isUrlPost()}
        res.assertValueAt(2) { it.t3ListForRV!!.vsT3!![3].isGalleryPost()}

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

        viewModel.processInput(Screen1Event.ScreenLoadEvent(""))
        res.await(1,TimeUnit.SECONDS)
        viewModel.processInput(Screen1Event.ClickOnT5ViewEvent("t5_3c23m"))
        res.await(1,TimeUnit.SECONDS)
        res.assertValueCount(3)
        res.assertValueAt(2) {
            it.t3ListForRV!!.vsT3!![0].url.contains("cx5ll43oe31a1")}
        res.assertNotComplete()
    }

    @Test
    fun getRidOfEmptyFullViewStateAsFirstEmission()
    {
        val end = loadJsonResponse("Berserk.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(end!!))
        val res = viewModel.vs.test()
        viewModel.processInput(Screen1Event.ScreenLoadEvent(""))
        var n = res.await(1,TimeUnit.SECONDS)
        res.assertValueAt(0) { it != FullViewStateScreen1() }
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