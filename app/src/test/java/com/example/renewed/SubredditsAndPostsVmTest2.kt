package com.example.renewed

import com.example.renewed.Screen1.SubredditsAndPostsVM
import com.example.renewed.models.Screen1Event
import com.example.renewed.repos.BaseSubredditsAndPostsRepo
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Test
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
        viewModel.processInput(Screen1Event.ScreenLoadEvent(""))
       viewModel.processInput(Screen1Event.ClickOnT5ViewEvent("t5_tu4j3"))

        var l = res.await(1, TimeUnit.SECONDS)

        //THEN
        MatcherAssert.assertThat("Is there a subscrier?", res.hasSubscription())
        res.assertNotComplete()
        res.assertNoErrors()
        res.assertValueCount(2)



}}