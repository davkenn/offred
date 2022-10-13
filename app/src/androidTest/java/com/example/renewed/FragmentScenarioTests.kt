package com.example.renewed

import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

class FragmentScenarioTests {
 /**   @get:Rule
    var rule: RuleChain = RuleChain.outerRule(HiltAndroidRule(this)).around(
        ActivityScenarioRule(MainActivity::class.java)
    )**/

    /**   @get:Rule
    var activityRule= RuleChain.outerRule(HiltAndroidRule(this)).around(
    ActivityScenarioRule(MainActivity::class.java)
    )**/



    @Test
    fun setUpActivity(){
    }


}
