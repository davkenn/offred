package com.example.renewed

import androidx.core.os.bundleOf
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.renewed.models.MyEvent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LargeTest {
    @get:Rule()
    var hiltRule = HiltAndroidRule(this)


    @Before
    fun init() {
        hiltRule.inject()
        val fragArgs = bundleOf()
        launchFragmentInHiltContainer<SubredditsSelectionFragment>()

    }


    @Test
    fun testAllDisplayedDBColumnsAreZeroOnRecreate() {
        val scenario = launchFragmentInHiltContainer<SubredditsSelectionFragment>()

    }

    @Test
    fun testIfButtonClickSelectsButton() {

        onView(withId(R.id.subreddits_rv))
            .perform(scrollToPosition<SubredditsAdapter.SubredditViewHolder>(10))
        onView(withId(R.id.subreddits_rv))

            .perform(
                RecyclerViewActions.actionOnItemAtPosition
                <SubredditsAdapter.SubredditViewHolder>(10, click())
            )
        onView(withId(R.id.subreddits_rv))
            .check(matches(withChild(isSelected())))


        //   .check(matches(isDisplayed()))
        //  .check(matches(hasDescendant(withText("TexttoMatch"))))
        //      val scenario = launchFragmentInContainer<SubredditsSelectionFragment>(fragArgs)

    }

    @Test
    fun clickSubredditThenVerifyPostsLoaded() {
        onView(withId(R.id.subreddits_rv))
            .perform(scrollToPosition<SubredditsAdapter.SubredditViewHolder>(10))
        onView(withId(R.id.subreddits_rv))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition
                <SubredditsAdapter.SubredditViewHolder>(10, click())
            )
        onView(allOf(withId(R.id.posts_rv))).check(matches(hasMinimumChildCount(5)))
    }

    @Test
    fun clickSubredditThenVerifySubredditViewLoaded() {
        onView(withId(R.id.subreddits_rv))
            .perform(scrollToPosition<SubredditsAdapter.SubredditViewHolder>(10))
        onView(withId(R.id.subreddits_rv))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition
                <SubredditsAdapter.SubredditViewHolder>(10, click())
            )

        onView(allOf(withId(R.id.subscreen_nav_container))).check(matches(hasDescendant(withId(R.id.subname))))
        onView(withId(R.id.subname)).check(matches(withText("CompanyOfHeroes")))
    }

    @Test
    fun clickSubredditThenClickPostVerifyPostViewLoaded() {
        onView(withId(R.id.subreddits_rv))
            .perform(scrollToPosition<SubredditsAdapter.SubredditViewHolder>(10))
        onView(withId(R.id.subreddits_rv))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition
                <SubredditsAdapter.SubredditViewHolder>(10, click())
            )
        onView(withId(R.id.posts_rv))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition
                <SubredditsAdapter.SubredditViewHolder>(0, click())
            )
        onView(allOf(withId(R.id.subscreen_nav_container)))
            .check(matches(hasDescendant(withId(R.id.post_name))))
        onView(withId(R.id.post_name))
            .check(matches(withText("German U boat going through Lyon canal 1944 recolorized")))
    }

    @Test
    fun testIfRefreshButtonBringsNewPostsAndClearsSelected() {

        onView(withId(R.id.subreddits_rv))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition
                <SubredditsAdapter.SubredditViewHolder>(0, click())
            )
        onView(withId(R.id.subreddits_rv))
            .check(matches(allOf(hasDescendant(isSelected()), hasDescendant(withText("AZURE")))))


        onView(withId(R.id.refresh_button)).perform(click())


        onView(withId(R.id.subreddits_rv))
            .check(
                matches(
                    allOf(
                        not(hasDescendant(isSelected())),
                        not(hasDescendant(withText("AZURE")))
                    )
                )
            )

//        @Test
  //      fun testIfFourRefreshesWithNothingClickedBringsBackInitial() {
    //    }

            /**   onView(withId(R.id.subreddits_rv))
            .perform(
            RecyclerViewActions.actionOnItemAtPosition
            <SubredditsAdapter.SubredditViewHolder>(0, click())
            )
            onView(withId(R.id.subreddits_rv))
            .check(matches(allOf(withChild(isSelected()), withChild(withText("Anarchism")))))
             **/

            //   onView(withId(R.id.subreddits_rv))
            //     .check(matches()


            //   .check(matches(isDisplayed()))
            //  .check(matches(hasDescendant(withText("TexttoMatch"))))
            //      val scenario = launchFragmentInContainer<SubredditsSelectionFragment>(fragArgs)

        }


        @Test
        fun getActivity() {
            val activityScenario = ActivityScenario.launch(MainActivity::class.java)

        }
    }

