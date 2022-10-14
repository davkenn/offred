package com.example.renewed

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.renewed.Room.RedditDatabase
import com.example.renewed.models.FullViewState
import com.example.renewed.models.MyEvent
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeUnit

class SubredditsAndPostsVMTest2 {
    private lateinit var viewModel: SubredditsAndPostsVM
    private lateinit var fakerepo: BaseSubredditsAndPostsRepo




    @Before
    fun setUp() {

        fakerepo = FakeRepo()
        viewModel = SubredditsAndPostsVM(fakerepo)


        //    viewModel = SubredditsAndPostsVM(SubredditsAndPostsRepository(API., null,null))
    }



    @After
    fun tearDown() {
    }

    @Test
    fun prefetch() {
        fakerepo.prefetchSubreddits()
    }

    @Test
    fun processInput() {
        //GIVEN

        //WHEN
        val res = viewModel.vs.test()
        viewModel.processInput(MyEvent.ScreenLoadEvent(""))
       viewModel.processInput(MyEvent.ClickOnT5ViewEvent("t5_tu4j3"))

        var l = res.await(1, TimeUnit.SECONDS)

        //THEN
        MatcherAssert.assertThat("Is there a subscrier?", res.hasSubscription())
        res.assertNotComplete()
        res.assertNoErrors()
        res.assertValueCount(2)



}}