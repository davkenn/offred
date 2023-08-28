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



@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@UninstallModules(DbModule::class)
class LargeTest {

    var initialDbContentsT5: List<RoomT5>?=null
    var initialDbContentsT3: List<RoomT3>?=null


    @get:Rule()
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var t5Dao: T5DAO

    @Inject
    lateinit var t3DAO: T3DAO

    @Inject
    lateinit var db: RedditDatabase


    @Before
    fun init() {
        hiltRule.inject()
        t5Dao.clearViews()

        //When the first test runs, save the initial db contents to two local variables
        if (initialDbContentsT5 == null) {
            initialDbContentsT5 = t5Dao.getAllRows()
            initialDbContentsT3 = t3DAO.getAllRows()

        }
        //Start Screen1 Fragment and then...
        launchFragmentInHiltContainer<SubredditsSelectionFragment>()
        //...give The UI time to load for each test
        Thread.sleep(3000)
    }

    @After
    fun resetDBContents() {
        db.close()
    }

    companion object {
        init {}
        @BeforeClass @JvmStatic fun setup() {}
        @AfterClass @JvmStatic fun teardown() {}
    }

    @Test
    fun testIfButtonClickSelectsButton() {

        onView(withId(R.id.subreddits_rv))
            .perform(
                scrollToPosition<SubredditsAdapter.SubredditViewHolder>(9),
                actionOnItemAtPosition<SubredditsAdapter.SubredditViewHolder>(9, click())
            )

        onView(withId(R.id.subreddits_rv))
            .check(matches(withChild(isSelected())))

    }

    @Test
    fun clickSubredditThenVerifyPostsLoaded() {
        //the event of clicking on a subreddit returns two results so we need an extra increment
        onView(withId(R.id.subreddits_rv))
            .perform(
                scrollToPosition<SubredditsAdapter.SubredditViewHolder>(9),
                actionOnItemAtPosition<SubredditsAdapter.SubredditViewHolder>(9, click())
            )
        onView(withId(R.id.posts_rv)).check(matches(hasMinimumChildCount(5)))
    }

    @Test
    fun clickSubredditThenVerifySubredditViewLoaded() {
        //the event of clicking on a subreddit returns two results so we need an extra increment
     //   CountingIdleResource.increment()

        onView(withId(R.id.subreddits_rv))
            .perform(
                actionOnItemAtPosition<SubredditsAdapter.SubredditViewHolder>(0, click())
            )

        onView(allOf(withId(R.id.subscreen_nav_container))).check(matches(hasDescendant(withId(R.id.subname))))
        onView(withId(R.id.subname)).check(matches(withText("CATHELP")))
    }

    @Test
    fun clickSubredditThenClickPostVerifyPostViewLoaded() {
        //Two events both produce two results so add two increments up front
        onView(withId(R.id.subreddits_rv))
            .perform(
                actionOnItemAtPosition<SubredditsAdapter.SubredditViewHolder>(0, click())
            )
        onView(withId(R.id.posts_rv))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition
                <PostsAdapter.PostViewHolder>(0, click())
            )

        onView(allOf(withId(R.id.subscreen_nav_container)))
            .check(matches(hasDescendant(withId(R.id.post_name))))

        onView(withId(R.id.post_name))
            .check(matches(withSubstring("Should I be worried")))
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
                    hasDescendant(withText("CATHELP"))
                )
            )
        )

        onView(withId(R.id.refresh_button)).perform(click())
        Thread.sleep(3000)
        onView(withId(R.id.subreddits_rv)).check(
            matches(
                allOf(
                    not(hasDescendant(isSelected())),
                    not(hasDescendant(withText("CATHELP")))
                )
            )
        )
    }
    @Test
    fun refreshButtonThreeTimesAllNewSubredditsInList() {

        onView(withId(R.id.subreddits_rv)).perform(
            RecyclerViewActions.actionOnItemAtPosition
            <SubredditsAdapter.SubredditViewHolder>(0, click())
        )

        onView(withId(R.id.subreddits_rv)).check(
            matches(hasDescendant(withText("CATHELP")))
        )

        repeat(3) {
            Thread.sleep(2000)
            onView(withId(R.id.refresh_button)).perform(click())
        }

        for (name in initialDbContentsT5!!) {
            onView(withId(R.id.subreddits_rv)).check(
                matches(not(hasDescendant(withText(name.displayName)))))
        }
    }


    @Test
    fun clickSubredditThenClickSaveVerifyRecyclerViewRemovesSubreddit() {

        onView(withId(R.id.subreddits_rv))
            .perform(
                scrollToPosition<SubredditsAdapter.SubredditViewHolder>(0),
                actionOnItemAtPosition<SubredditsAdapter.SubredditViewHolder>(0, click())
            )

        onView(withId(R.id.save_button)).perform(click())

        onView(withId(R.id.subreddits_rv)).check(
            matches(not(hasDescendant(withText("CATHELP")))))
    }


    @Module
    @InstallIn(SingletonComponent::class)
    object TestRepoModule {

        @Provides
        @Singleton
        fun provideDB(@ApplicationContext ctxt: Context): RedditDatabase {
        //this function is a workaround for the fact that a Room in-memory database
            //can not be created from an asset file. This function gives
            //the db loaded from the asset a random name and then deletes all dbs
            //before creating a new one for a test. This ensures that even though we
            // are creating a persistent database, that database is deleted and
            // recreated with a different name for each test
            val c = java.util.UUID.randomUUID().toString()
            for (f in ctxt.databaseList()) {
                if (f.endsWith("tmp1")) ctxt.deleteDatabase(f)
            }

            return Room.databaseBuilder(
                ctxt,
                RedditDatabase::class.java,c+".tmp1"
            ).createFromAsset("RedditDB4").build()
        }


        @Provides
        @Singleton
        fun provideFavsDAO(db: RedditDatabase): FavoritesDAO = db.favoritesDao()

        @Singleton
        @Provides
        fun provideT5DAO(db: RedditDatabase): T5DAO = db.subredditDao()

        @Singleton
        @Provides
        fun provideT3DAO(db: RedditDatabase): T3DAO = db.postsDao()
    }

}

/**
        @Test
        fun getActivity() {
            val activityScenario = ActivityScenario.launch(MainActivity::class.java)

        }**/


