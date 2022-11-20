package com.example.renewed

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.renewed.Room.RedditDatabase
import com.example.renewed.models.MyEvent
import com.example.renewed.repos.BaseSubredditsAndPostsRepo
import com.example.renewed.repos.SubredditsAndPostsRepo
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
        fakerepo = SubredditsAndPostsRepo(apiService,a.subredditDao(),a.postsDao() )
        viewModel = SubredditsAndPostsVM(fakerepo)

    }


    @Test
    fun processInput() {
        viewModel.processInput(MyEvent.ScreenLoadEvent(""))
    }
    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
}