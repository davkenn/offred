package com.example.renewed

import android.content.Context
import androidx.core.os.bundleOf
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.renewed.Room.RedditDatabase
import com.example.renewed.Room.T3DAO
import com.example.renewed.Room.T5DAO
import com.example.renewed.Screen1.SubredditsSelectionFragment
import com.example.renewed.di.DbModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import okio.ArrayIndexOutOfBoundsException
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Singleton

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)

@UninstallModules(  DbModule::class)
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
        onView(withId(R.id.subname)).check(matches(withText("DiscoElysium")))
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
                <PostsAdapter.PostViewHolder>(1, click())
            )
        onView(allOf(withId(R.id.subscreen_nav_container)))
            .check(matches(hasDescendant(withId(R.id.post_name))))
        onView(withId(R.id.post_name))
            .check(matches(withText("Authority, is that you?")))
    }
/**@Module
@InstallIn(SingletonComponent::class)
object TestRepoModule {

    @Provides
    @Singleton
    fun provideDB(@ApplicationContext ctxt: Context): RedditDatabase {

        return Room.databaseBuilder(
            ctxt,
            RedditDatabase::class.java,
            "RedditDB1"
        ).createFromAsset("RedditDBTest")
            .build()
    }

    @Provides
    @Singleton
    fun provideT5DAO(db: RedditDatabase): T5DAO = db.subredditDao()

    @Provides
    @Singleton
    fun provideT3DAO(db: RedditDatabase): T3DAO = db.postsDao()
}**/
    @Test
    fun testIfRefreshButtonBringsNewPostsAndClearsSelected() {
        try {
            Thread.sleep(3000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        onView(withId(R.id.subreddits_rv)).perform(
            RecyclerViewActions.actionOnItemAtPosition
            <SubredditsAdapter.SubredditViewHolder>(0, click())
        )

        onView(withId(R.id.subreddits_rv)).check(
            matches(
                allOf(
                    hasDescendant(isSelected()),
                    hasDescendant(withText("ATT"))
                )
            )
        )

        onView(withId(R.id.refresh_button)).perform(click())

        try {
            Thread.sleep(3000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        onView(withId(R.id.subreddits_rv)).check(
            matches(
                allOf(
                    not(hasDescendant(isSelected())),
                    not(hasDescendant(withText("ATT")))
                )
            )
        )
        }
    //TODO the db needs to be recreated from asset after every test
        @Test
        fun refreshButton4FourTimesBringsUpSameList() {


            try {
                Thread.sleep(3000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            onView(withId(R.id.subreddits_rv)).perform(
                RecyclerViewActions.actionOnItemAtPosition
                <SubredditsAdapter.SubredditViewHolder>(0, click())
            )

            onView(withId(R.id.subreddits_rv)).check(
                matches(
                    allOf(
                        hasDescendant(isSelected()),
                        hasDescendant(withText("ATT"))
                    )
                )
            )

            repeat(4) { onView(withId(R.id.refresh_button)).perform(click())
                try {
                    Thread.sleep(3000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }

            onView(withId(R.id.subreddits_rv)).check(
                matches(hasDescendant(withText("ATT"))))

        }

/**
        @Test
        fun getActivity() {
            val activityScenario = ActivityScenario.launch(MainActivity::class.java)

        }**/
    }

