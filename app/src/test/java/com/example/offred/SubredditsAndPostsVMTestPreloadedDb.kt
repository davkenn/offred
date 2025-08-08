package com.example.offred

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.offred.Room.RedditDatabase
import com.example.offred.Screen1.SubredditsAndPostsVM
import com.example.offred.models.Screen1Event
import com.example.offred.repos.BaseSubredditsAndPostsRepo
import com.example.offred.repos.SubredditsAndPostsRepo
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test


class SubredditsAndPostsVMTestPreloadedDb {

    private lateinit var viewModel: SubredditsAndPostsVM
    private lateinit var fakerepo: BaseSubredditsAndPostsRepo
    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var apiService: API

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        var a=  Room.inMemoryDatabaseBuilder(context,
            RedditDatabase::class.java
        )//.createFromAsset("RedditDB")
            .build()
        mockWebServer = MockWebServer()
        apiService = setupTestRetrofit(mockWebServer,true)
        fakerepo = SubredditsAndPostsRepo(a.subredditDao(),a.postsDao(),apiService )
        viewModel = SubredditsAndPostsVM(fakerepo)

    }


    @Test
    fun processInput() {
        viewModel.processInput(Screen1Event.ScreenLoadEvent(""))
    }
    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
}