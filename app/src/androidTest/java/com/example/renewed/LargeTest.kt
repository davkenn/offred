package com.example.renewed

import android.content.Context
import androidx.room.Room
import androidx.test.espresso.Espresso.onView

import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.renewed.Room.FavoritesDAO
import com.example.renewed.Room.RedditDatabase
import com.example.renewed.Room.T3DAO
import com.example.renewed.Room.T5DAO
import com.example.renewed.Screen1.PostsAdapter

import com.example.renewed.Screen1.SubredditsSelectionFragment
import com.example.renewed.di.TestDbModule
import com.example.renewed.models.RoomT3
import com.example.renewed.models.RoomT5
import com.example.renewed.models.T3
import com.example.renewed.models.T5
import com.example.renewed.models.toDbModel
import com.squareup.moshi.Moshi
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
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LargeTest {

    @get:Rule()
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var t5Dao: T5DAO
    @Inject
    lateinit var t3Dao: T3DAO
    @Inject
    lateinit var db: RedditDatabase
    @Inject
    lateinit var moshi: Moshi

    @Before
    fun init() {
        hiltRule.inject()

        // --- Populate the database from local JSON files using Moshi ---
        val t5Adapter = moshi.adapter(T5::class.java)
        val t3Adapter = moshi.adapter(T3::class.java)

        val t5List = emptyList<String>()
            .mapNotNull { fileName ->
                val jsonString = readJsonFromResources(fileName)
                t5Adapter.fromJson(jsonString)?.toDbModel()
            }

        val t3List = listOf("lana.json")//"handledeletedpost.json","handleimagegallery.json","handlelink.json","handlensdwclickthru.json")
            .mapNotNull { fileName ->
                val jsonString = readJsonFromResources(fileName)
                t3Adapter.fromJson(jsonString)?.toDbModel()
            }

        // Insert the converted data into the database
        t5Dao.insertAll(t5List).blockingAwait()
        t3Dao.insertAll(t3List).blockingAwait()
        // --- End of data population ---

        launchFragmentInHiltContainer<SubredditsSelectionFragment>()
        Thread.sleep(2000)
    }

    /**
     * Helper function to read a JSON file from the test assets directory.
     */
    private fun readJsonFromResources(fileName: String): String {
        val context = InstrumentationRegistry.getInstrumentation().context
        val inputStream = context.assets.open("api_responses/$fileName")
        val reader = InputStreamReader(inputStream)
        return reader.readText()
    }

    @After
    fun tearDown() {
        db.clearAllTables()
        db.close()
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
  /**  @Test
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
    }**/


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




}

/**
        @Test
        fun getActivity() {
            val activityScenario = ActivityScenario.launch(MainActivity::class.java)

        }**/


