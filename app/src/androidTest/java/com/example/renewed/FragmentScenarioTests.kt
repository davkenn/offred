package com.example.renewed

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FragmentScenarioTests {


//    @Inject
  //  lateinit var rep:BaseSubredditsAndPostsRepo

    @get:Rule(order=0)
    var hiltRule = HiltAndroidRule(this)


   //  @get:Rule(order=1)
    // val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule(order=1)
    val fragmentRule = HiltFragmentScenarioRule(SubredditsSelectionFragment::class)

    @Before
    fun init() {
        hiltRule.inject()
  //      activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        fragmentRule.launchFragment(R.style.Theme_Renewed)

    }

    @Test
    fun setUpActivity(){



        fragmentRule.fragmentScenario?.moveToState(Lifecycle.State.RESUMED)

        Espresso.onView(ViewMatchers.withId(R.id.subreddits_rv))

            .perform(
                RecyclerViewActions.actionOnItemAtPosition
            <SubredditsAdapter.SubredditViewHolder>(0, ViewActions.click()))

        Espresso.onView(ViewMatchers.withId(R.id.posts_rv)).perform(
                RecyclerViewActions.actionOnItemAtPosition
                <PostsAdapter.PostViewHolder>(0, ViewActions.click()))
        fragmentRule.fragmentScenario?.recreate()
        Espresso.onView(ViewMatchers.withId(R.id.back_button))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))


      //  fragmentRule.fragmentScenario?.activityScenario?.moveToState(Lifecycle.State.DESTROYED)
        }




        }





