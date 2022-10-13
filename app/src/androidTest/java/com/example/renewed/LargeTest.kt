package com.example.renewed

import androidx.core.os.bundleOf
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.example.renewed.models.MyEvent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.hasItem
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class LargeTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)


  // @get:Rule
    //val activityRule = ActivityScenarioRule(MainActivity::class.java)


    @Inject
    lateinit var rep:BaseSubredditsAndPostsRepo

    lateinit var vm :SubredditsAndPostsVM


    @Before
    fun init() {
        hiltRule.inject()

        val fragArgs = bundleOf()

        launchFragmentInHiltContainer<SubredditsSelectionFragment>()

    }



    @Test
    fun testAllDisplayedDBColumnsAreZeroOnRecreate(){
        val scenario = launchFragmentInHiltContainer<SubredditsSelectionFragment>()

    }

    @Test
    fun testIfButtonClickSelectsButton(){
        try {
            Thread.sleep(6000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        onView(withId(R.id.subreddits_rv))
     //       .perform(scrollToPosition<SubredditsAdapter.SubredditViewHolder>(10))
    //    onView(withId(R.id.subreddits_rv))

            .perform(RecyclerViewActions.actionOnItemAtPosition
            <SubredditsAdapter.SubredditViewHolder>(10,click()))
        onView(withId(R.id.subreddits_rv))
            .check(matches(withChild(isSelected())))



        //   .check(matches(isDisplayed()))
        //  .check(matches(hasDescendant(withText("TexttoMatch"))))
        //      val scenario = launchFragmentInContainer<SubredditsSelectionFragment>(fragArgs)

    }

    @Test
    fun loadFragment(){
        try {
            Thread.sleep(40000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
     //   onView(withId(R.id.subreddits_rv))
   //         .perform(scrollToPosition<SubredditsAdapter.SubredditViewHolder>(10))
        onView(withId(R.id.subreddits_rv))
            .perform(RecyclerViewActions.actionOnItemAtPosition
            <SubredditsAdapter.SubredditViewHolder>(10,click()))

        onView(allOf(withId(R.id.posts_rv))).check(matches(hasMinimumChildCount(5)))
         //   .check(matches(isDisplayed()))
          //  .check(matches(hasDescendant(withText("TexttoMatch"))))
  //      val scenario = launchFragmentInContainer<SubredditsSelectionFragment>(fragArgs)

    }

    @Test
    fun getActivity(){
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)

    }



}