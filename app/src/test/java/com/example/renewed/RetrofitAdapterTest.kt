package com.example.renewed

import com.example.renewed.TestTools.Companion.loadJsonResponse
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before

class RetrofitAdapterTest {
    private lateinit var viewModel: SubVM
    private lateinit var fakerepo: BaseSubredditsAndPostsRepo
    private lateinit var apiService: API
    private lateinit var mockWebServer: MockWebServer

    @Before
    public fun setUp() {
        mockWebServer = MockWebServer()

        var coinedResponse = loadJsonResponse("Berserk.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(coinedResponse!!))
        mockWebServer.start()
        apiService =setupTestRetrofit(mockWebServer,false)

        fakerepo = FakeRepo2(apiService)
        viewModel = SubVM(fakerepo)


        //    viewModel = SubredditsAndPostsVM(SubredditsAndPostsRepository(API., null,null))
    }
}