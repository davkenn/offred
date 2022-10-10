package com.example.renewed

import androidx.core.os.bundleOf
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.renewed.models.MyEvent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class LargeTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

  //  @get:Rule
   // val activityRule = ActivityScenarioRule(MainActivity::class.java)


    @Inject
    lateinit var rep:BaseSubredditsAndPostsRepo

    lateinit var vm :SubredditsAndPostsVM


    @Before
    fun init() {
        hiltRule.inject()
         vm = SubredditsAndPostsVM(rep)
        vm.prefetch().subscribe()

    }

    @Test
    fun get_this(){
        var l = vm.vs.test()
        vm.processInput(MyEvent.ScreenLoadEvent(""))

    //    assertThat("hello",l.values().first().toString(), equalTo("Aaaa") )
        assert(rep is SubredditsAndPostsRepository)

    }

    @Test
    fun loadFragment(){
       val fragArgs = bundleOf()
  //      val scenario = launchFragmentInContainer<SubredditsSelectionFragment>(fragArgs)

    }



}