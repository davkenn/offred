package com.example.renewed

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


    @Inject
    lateinit var rep:BaseSubredditsAndPostsRepo

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun get_this(){
        assert(rep is SubredditsAndPostsRepository)

    }

}