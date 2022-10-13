package com.example.renewed

import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.Rule
import org.junit.Test

class FragmentScenarioTests {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)


     @get:Rule()
     val activityRule = ActivityScenarioRule(MainActivity::class.java)


    @Test
    fun setUpActivity(){activityRule
    }

}

