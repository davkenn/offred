package com.example.renewed

import com.example.renewed.models.FullViewState
import com.example.renewed.models.MyEvent

import org.junit.After
import org.junit.Before
import org.junit.Test

class SubredditsAndPostsVMTest {
    private lateinit var viewModel: SubredditsAndPostsVM
    @Before
    public fun setUp() {
        viewModel = SubredditsAndPostsVM(FakeRepo2())

      //    viewModel = SubredditsAndPostsVM(SubredditsAndPostsRepository(API., null,null))
    }

    @After
    public fun tearDown() {

    }

    @Test
    fun prefetch() {
        FakeRepo2().prefetchSubreddits()
    }

    @Test
    fun processInput() {
        viewModel.processInput(MyEvent.ScreenLoadEvent(""))
        var res = viewModel.vs.test()

        res.assertValueCount(1)
    }

    @Test
    fun processRefreshEvent() {
        viewModel.processInput(MyEvent.RemoveAllSubreddits(listOf("1fasf","asdasdas")))
        var res = viewModel.vs.test()

   //     res.assertValueCount(2)
        res.assertValueAt(0, FullViewState() )
    }

    @Test
    fun getVs() {
    }
}