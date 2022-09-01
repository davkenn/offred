package com.example.renewed

import com.example.renewed.models.FullViewState
import com.example.renewed.models.MyEvent

import org.junit.After
import org.junit.Before
import org.junit.Test

class SubredditsAndPostsVMTest {
    private lateinit var viewModel: SubredditsAndPostsVM
    private lateinit var fakerepo: FakeRepo2
    @Before
    public fun setUp() {
        fakerepo = FakeRepo2()
        viewModel = SubredditsAndPostsVM(fakerepo)

      //    viewModel = SubredditsAndPostsVM(SubredditsAndPostsRepository(API., null,null))
    }

    @After
    public fun tearDown() {

    }

    @Test
    fun prefetch() {
        fakerepo.prefetchSubreddits()
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