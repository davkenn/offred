package com.example.renewed

import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.renewed.Screen1.PostsAdapter

import com.example.renewed.Screen1.SubredditsSelectionFragment
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 *
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ScreenRotationTests {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val fragmentRule = HiltFragmentScenarioRule(SubredditsSelectionFragment::class)

    @Before
    fun init() {
        hiltRule.inject()
        fragmentRule.launchFragment(R.style.Theme_Renewed)
        fragmentRule.fragmentScenario?.moveToState(Lifecycle.State.RESUMED)
        try {
            Thread.sleep(3000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    @Test
    fun backButtonEnabledThenRotateScreenAndBackIsStillEnabled() {
        onView(withId(R.id.subreddits_rv))
            .perform(
                actionOnItemAtPosition
                <SubredditsAdapter.SubredditViewHolder>(0, ViewActions.click())
            )

        onView(withId(R.id.posts_rv))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition
                <PostsAdapter.PostViewHolder>(0, ViewActions.click())
            )

        //this will destroy the fragment and recreate it as is done on rotation
        fragmentRule.fragmentScenario?.recreate()
        onView(withId(R.id.back_button))
            .check(matches(isDisplayed()))

    }

    @Test
    fun recreatedActivityHasRightButtonStateForPostView() {
        onView(withId(R.id.subreddits_rv))
            .perform(
                actionOnItemAtPosition<SubredditsAdapter.SubredditViewHolder>
                    (0, ViewActions.click())
            )

        onView(withId(R.id.posts_rv)).perform(
            RecyclerViewActions.actionOnItemAtPosition
            <PostsAdapter.PostViewHolder>(0, ViewActions.click())
        )
        fragmentRule.fragmentScenario?.recreate()

        onView(withId(R.id.back_button))
            .check(matches(isDisplayed()))
        onView(withId(R.id.save_button))
            .check(matches(not(isDisplayed())))

    }

    @Test
    fun recreatedActivityHasRightButtonStateForSubredditView() {
        onView(withId(R.id.subreddits_rv))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition
                <SubredditsAdapter.SubredditViewHolder>(0, ViewActions.click())
            )

        fragmentRule.fragmentScenario?.recreate()

        onView(withId(R.id.back_button))
            .check(matches(isDisplayed()))
        onView(withId(R.id.save_button))
            .check(matches(isDisplayed()))
    }


    @Test
    fun recreatedActivityHasRightButtonStateForBlankView() {
        fragmentRule.fragmentScenario?.recreate()

        onView(withId(R.id.back_button))
            .check(matches(not(isDisplayed())))
        onView(withId(R.id.save_button))
            .check(matches(not(isDisplayed())))
    }
}


