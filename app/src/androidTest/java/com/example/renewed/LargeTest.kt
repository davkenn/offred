package com.example.renewed

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import com.example.renewed.models.FullViewState
import com.example.renewed.models.MyEvent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class LargeTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)


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

        assertThat("hello",l.values().first().toString(), equalTo("Aaaa") )
        assert(rep is SubredditsAndPostsRepository)

    }

}