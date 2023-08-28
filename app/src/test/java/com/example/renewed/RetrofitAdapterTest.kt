package com.example.renewed

import com.example.renewed.Screen1.Subscreen.SubVM
import com.example.renewed.TestTools.Companion.loadJsonResponse
import com.example.renewed.repos.BaseSubredditsAndPostsRepo
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before

class RetrofitAdapterTest {
    private lateinit var viewModel: SubVM
    private lateinit var fakerepo: BaseSubredditsAndPostsRepo
    private lateinit var apiService: API
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()

        mockWebServer.enqueueResponse("Berserk.json",200)
        mockWebServer.start()
        apiService =setupTestRetrofit(mockWebServer,false)

        fakerepo = FakeRepo2(apiService)
        viewModel = SubVM(fakerepo)


        //    viewModel = SubredditsAndPostsVM(SubredditsAndPostsRepository(API., null,null))
    }
}