package com.example.renewed

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FragmentScenarioTests {
    @get:Rule(order=0)
    var hiltRule = HiltAndroidRule(this)


     @get:Rule(order=1)
     val activityRule = ActivityScenarioRule(MainActivity::class.java)


    @Test
    fun setUpActivity(){
        var a = activityRule.scenario
        a.onActivity {  }
    }


}
