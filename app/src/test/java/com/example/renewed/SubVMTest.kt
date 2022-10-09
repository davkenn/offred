package com.example.renewed

import com.example.renewed.models.MyViewState
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class SubVMTest {
    private lateinit var viewModel: SubVM
    private lateinit var fakerepo: FakeRepo2
    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var apiService: API

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        apiService =setupTestRetrofit(mockWebServer,true)
        fakerepo = FakeRepo2(apiService)
        viewModel = SubVM(fakerepo)


    }



    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun fetchWrongUrl(){
        val emptySubreddit = TestTools.loadJsonResponse("handleUrlNotPointingToSubreddit.json")

        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody(emptySubreddit!!))
        //Not that I COULDNT pass a real reddit name here"
        val tester = viewModel.setSub("NOTAREALSUB").test()

        tester.await(300,TimeUnit.MILLISECONDS)

        //RETURNS A DISPLAYABLE VIEW FROM A NETWORK ERROR, JUST DISPLAY IT LIKE ANY OTHER
        tester.assertValue { it.name=="ERROR" }
        tester.assertValue{it is MyViewState.T5ForViewing}


    }

}