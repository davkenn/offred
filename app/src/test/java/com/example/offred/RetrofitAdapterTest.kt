package com.example.offred

import com.example.offred.Screen1.Subscreen.SubVM
import com.example.offred.repos.BaseSubredditsAndPostsRepo
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