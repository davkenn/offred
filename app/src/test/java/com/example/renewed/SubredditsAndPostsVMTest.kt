package com.example.renewed


 import com.example.renewed.Screen1.SubredditsAndPostsVM
 import com.example.renewed.TestTools.*
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
    fun sendScreenLoadEventAndCheckThatTheViewStateIsUpdatedAndStaysOpen() {
        //GIVEN
        mockWebServer.enqueueResponse("Berserk.json",200)

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
    fun whenRepoFetchesASubredditItReturnsItAndEndsRxStream() {
        //GIVEN
        mockWebServer.enqueueResponse("Berserk.json",200)

        //WHEN
        val r = fakerepo.getSubreddits("aaa")
        val t = r.test()
        var l = t.await(1,TimeUnit.SECONDS)

        //THEN
        //there will be one list with one item and stream will finish
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

        mockWebServer.enqueueResponse("crtgamingabout.json",200)
        mockWebServer.enqueueResponse("crtgamingposts.json",200)


        val res = viewModel.vs.test()

        viewModel.processInput(Screen1Event.ScreenLoadEvent(""))
        res.await(1,TimeUnit.SECONDS)
        viewModel.processInput(Screen1Event.ClickOnT5ViewEvent("t5_3c23m"))
        res.await(1,TimeUnit.SECONDS)
        res.assertValueCount(3)
        //loads 3 images for galeery
        res.assertValueAt(2) {
            it.t3ListForRV!!.vsT3!![3].galleryUrls!!.size == 3
        }
        res.assertNotComplete()
    }

    @Test
    fun gallerySubredditIsOnlyGalleryTypes() {

        mockWebServer.enqueueResponse("crtgamingabout.json",200)
        mockWebServer.enqueueResponse("crtgamingposts.json",200)



        val res = viewModel.vs.test()

        viewModel.processInput(Screen1Event.ScreenLoadEvent(""))

        res.await(1,TimeUnit.SECONDS)
        //On clicking a subreddit (a T5), a list of posts (T3s) for that subreddit will be returned
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
        mockWebServer.enqueueResponse("crtgamingabout.json",200)
        mockWebServer.enqueueResponse("crtgamingposts.json",200)

        val res = viewModel.vs.test()

        viewModel.processInput(Screen1Event.ScreenLoadEvent(""))
        res.await(1,TimeUnit.SECONDS)
        //On clicking a subreddit (a T5), a list of posts (T3s) for that subreddit will be returned
        viewModel.processInput(Screen1Event.ClickOnT5ViewEvent("t5_3c23m"))
        res.await(1,TimeUnit.SECONDS)
        res.assertValueCount(3)
        //ensure that video post url is properly loaded
        res.assertValueAt(2) {
            it.t3ListForRV!!.vsT3!![0].url.contains("cx5ll43oe31a1")}
        res.assertNotComplete()
    }

    /**
     * The scan function in the viewmodel that combines partial view states into full view states
     * should not return the first emission from scan, which is an empty view state. Wait until
     * View State has content to load it.
     */
    @Test
    fun getRidOfEmptyFullViewStateAsFirstEmission()
    {
        mockWebServer.enqueueResponse("Berserk.json",200)
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