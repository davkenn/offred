package com.example.renewed

import androidx.test.espresso.Espresso.onView

import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.renewed.Room.RedditDatabase
import com.example.renewed.Room.T3DAO
import com.example.renewed.Room.T5DAO
import com.example.renewed.Screen1.PostsAdapter

import com.example.renewed.Screen1.SubredditsSelectionFragment
import com.example.renewed.models.Holder
import com.example.renewed.models.Listing
import com.example.renewed.models.T3
import com.example.renewed.models.T5
import com.example.renewed.models.toDbModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.*
import org.junit.runner.RunWith
import java.io.InputStreamReader
import javax.inject.Inject


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LargeTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1)
    val disableAnimationsRule = DisableAnimationsRule()


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
        val listingAdapter = moshi.adapter(Listing::class.java)
        val holderAdapter = moshi.adapter(Holder::class.java)
        val t3Adapter = moshi.adapter(T3::class.java)


        val jsonFiles = listOf(
            "crtgamingabout.json",
            "crtgamingpost1.json",// A single T5 object
            "lanapost1.json",        // A single T5 object
            "lana.json",           // A single T3 object
            "interestingasfuck.json",
            "interestingasfuckpost1.json",
             "blackpeopletwitter.json",
            "blackpeopletwitterpost1.json",
            "blueskyskeets.json",
            "blueskyskeetspost1.json"
        )


        val allHolders = jsonFiles.flatMap { fileName ->
            val jsonString = readJsonFromResources(fileName)
            parsePolymorphicJson(jsonString)
        }

        // Separate the Holders into T3s and T5s
        val t5List = allHolders.mapNotNull { it.data as? T5 }.map { it.toDbModel() }
        val t3List = allHolders.mapNotNull { it.data as? T3 }.map { it.toDbModel() }

        // Insert the converted data into the database
        if (t5List.isNotEmpty()) t5Dao.insertAll(t5List).blockingAwait()
        if (t3List.isNotEmpty()) t3Dao.insertAll(t3List).blockingAwait()
        // --- End of data population ---

        launchFragmentInHiltContainer<SubredditsSelectionFragment>()
        Thread.sleep(2000)
    }


    private fun parsePolymorphicJson(jsonString: String): List<Holder> {
        if (jsonString.trim().startsWith("[")) {
            // It's a list. Parse it as a List of Listings.
            val listType = Types.newParameterizedType(List::class.java, Listing::class.java)
            val listAdapter = moshi.adapter<List<Listing>>(listType)
            val listings = listAdapter.fromJson(jsonString) ?: emptyList()
            // Extract the 'children' (which are Holders) from each Listing and flatten the result.
            return listings.flatMap { it.data.children }
        } else if (jsonString.trim().startsWith("{")) {
            // It's a single object. Parse it as one Holder.
            val holderAdapter = moshi.adapter(Holder::class.java)
            val holder = holderAdapter.fromJson(jsonString)
            return if (holder != null) listOf(holder) else emptyList()
        }
        return emptyList() // Return empty for invalid or empty JSON
    }
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


