package com.example.renewed

import android.content.Context
import androidx.core.os.bundleOf
import androidx.room.Room
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.renewed.Room.FavoritesDAO
import com.example.renewed.Room.RedditDatabase
import com.example.renewed.Room.T3DAO
import com.example.renewed.Room.T5DAO
import com.example.renewed.Screen1.PostsAdapter

import com.example.renewed.Screen1.SubredditsSelectionFragment
import com.example.renewed.di.DbModule
import com.example.renewed.models.RoomT3
import com.example.renewed.models.RoomT5
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.*
import org.junit.runner.RunWith
import javax.inject.Inject
import javax.inject.Singleton


var allData5: List<RoomT5>?=null
var allData3: List<RoomT3>?=null

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@UninstallModules(  DbModule::class)
class LargeTest {
    @get:Rule()
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var t5: T5DAO

    @Inject
    lateinit var t3: T3DAO

    @Inject
    lateinit var db: RedditDatabase


    @Before
    fun init() {
        hiltRule.inject()
        //only null first call

        if (allData5 == null) {
 //TODO kinda a cheat and would ned to do it for isdisplayed and also what if I delete
   //         t5.getAllRows()
       //     t5.clearViews()
            allData5 = t5.getAllRows()
            allData3 = t3.getAllRows()
        }

        val fragArgs = bundleOf()
        launchFragmentInHiltContainer<SubredditsSelectionFragment>()
    }

    @After
    fun resetDBContents() {
        db.clearAllTables()
        t5.fillDb(allData5!!)
        t3.fillDb(allData3!!)

    }

    companion object {
        init {
            // things that may need to be setup before companion class member variables are instantiated
        }

        @BeforeClass @JvmStatic fun setup() {
            // things to execute once and keep around for the class
        }

        @AfterClass @JvmStatic fun teardown() {

            allData5=null
            allData3=null
        }
    }

    @Test
    fun testAllDisplayedDBColumnsAreZeroOnRecreate() {


        val scenario = launchFragmentInHiltContainer<SubredditsSelectionFragment>()

    }

    @Test
    fun testIfButtonClickSelectsButton() {

        onView(withId(R.id.subreddits_rv))

            .perform(
                scrollToPosition<SubredditsAdapter.SubredditViewHolder>(10),
                actionOnItemAtPosition<SubredditsAdapter.SubredditViewHolder>(10, click())
            )
        onView(withId(R.id.subreddits_rv))
            .check(matches(withChild(isSelected())))

    }

    @Test
    fun clickSubredditThenVerifyPostsLoaded() {
        onView(withId(R.id.subreddits_rv))
            .perform(
                scrollToPosition<SubredditsAdapter.SubredditViewHolder>(10),
                actionOnItemAtPosition<SubredditsAdapter.SubredditViewHolder>(10, click())
            )
        onView(allOf(withId(R.id.posts_rv))).check(matches(hasMinimumChildCount(5)))
    }

    @Test
    fun clickSubredditThenVerifySubredditViewLoaded() {

        onView(withId(R.id.subreddits_rv))
            .perform(
                scrollToPosition<SubredditsAdapter.SubredditViewHolder>(10),
                actionOnItemAtPosition<SubredditsAdapter.SubredditViewHolder>(10, click())
            )

        onView(allOf(withId(R.id.subscreen_nav_container))).check(matches(hasDescendant(withId(R.id.subname))))
        onView(withId(R.id.subname)).check(matches(withText("DiscoElysium")))
    }

    @Test
    fun clickSubredditThenClickPostVerifyPostViewLoaded() {

        onView(withId(R.id.subreddits_rv))
            .perform(
                scrollToPosition<SubredditsAdapter.SubredditViewHolder>(10),
                actionOnItemAtPosition
                <SubredditsAdapter.SubredditViewHolder>(10, click())
            )
        onView(withId(R.id.posts_rv))
            .perform(
                scrollToPosition<SubredditsAdapter.SubredditViewHolder>(10),
                RecyclerViewActions.actionOnItemAtPosition
                <PostsAdapter.PostViewHolder>(1, click())
            )

        onView(allOf(withId(R.id.subscreen_nav_container)))
            .check(matches(hasDescendant(withId(R.id.post_name))))

        onView(withId(R.id.post_name))
            .check(matches(withText("Authority, is that you?")))
    }


    @Test
    fun testIfRefreshButtonBringsNewPostsAndClearsSelected() {

        onView(withId(R.id.subreddits_rv)).perform(
            actionOnItemAtPosition<SubredditsAdapter.SubredditViewHolder>(0, click())
        )

        onView(withId(R.id.subreddits_rv)).check(
            matches(
                allOf(
                    hasDescendant(isSelected()),
                    hasDescendant(withText("30PlusSkinCare"))
                )
            )
        )

        onView(withId(R.id.refresh_button)).perform(click())

        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        onView(withId(R.id.subreddits_rv)).check(
            matches(
                allOf(
                    not(hasDescendant(isSelected())),
                    not(hasDescendant(withText("30PlusSkinCare")))
                )
            )
        )
    }

    //TODO the db needs to be recreated from asset after every test
    @Test
    fun refreshButton4FourTimesBringsUpSameList() {

        onView(withId(R.id.subreddits_rv)).perform(
            RecyclerViewActions.actionOnItemAtPosition
            <SubredditsAdapter.SubredditViewHolder>(0, click())
        )

        onView(withId(R.id.subreddits_rv)).check(
            matches(hasDescendant(withText("30PlusSkinCare"))))

        repeat(4) {
            onView(withId(R.id.refresh_button)).perform(click())
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        onView(withId(R.id.subreddits_rv)).check(
            matches(hasDescendant(withText("30PlusSkinCare")))
        )

    }

    @Test
    fun clickSubredditThenClickDeleteVerifyRecyclerViewReloaded() {

        onView(withId(R.id.subreddits_rv))
            .perform(
                scrollToPosition<SubredditsAdapter.SubredditViewHolder>(0),
                actionOnItemAtPosition<SubredditsAdapter.SubredditViewHolder>(0, click())
            )

        onView(withId(R.id.delete_button)).perform(click())

        onView(withId(R.id.subreddits_rv)).check(
            matches(not(hasDescendant(withText("ATT")))))
    }



    @Module
    @InstallIn(SingletonComponent::class)
    object TestRepoModule {

        @Provides
        @Singleton
        fun provideDB(@ApplicationContext ctxt: Context): RedditDatabase {

            return Room.
            databaseBuilder(
                ctxt,
                RedditDatabase::class.java,
                "RedditDB1"
            ).createFromAsset("RedditDBTest")
                .build()
        }

        @Provides
        @Singleton
        fun provideFavsDAO(db: RedditDatabase): FavoritesDAO = db.favoritesDao()


        @Provides
        @Singleton
        fun provideT5DAO(db: RedditDatabase): T5DAO = db.subredditDao()

        @Provides
        @Singleton
        fun provideT3DAO(db: RedditDatabase): T3DAO = db.postsDao()
    }

}

/**
        @Test
        fun getActivity() {
            val activityScenario = ActivityScenario.launch(MainActivity::class.java)

        }**/


