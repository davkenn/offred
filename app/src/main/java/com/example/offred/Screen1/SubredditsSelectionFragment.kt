@file:Suppress("SpellCheckingInspection")

package com.example.offred.Screen1

import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.offred.*
import com.example.offred.Screen1.Subscreen.ContentFragment
import com.example.offred.databinding.FragmentSubredditsSelectionBinding
import com.example.offred.models.Screen1Effect
import com.example.offred.models.Screen1Event
import com.example.offred.models.PartialViewStateScreen1
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import timber.log.Timber
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SubredditsSelectionFragment : Fragment(R.layout.fragment_subreddits_selection) {

    private val subsAndPostsVM: SubredditsAndPostsVM by viewModels()
    private var subredditAdapter: SubredditsAdapter? = null
    private var postAdapter: PostsAdapter? = null
    private var viewDisposables: CompositeDisposable? = null
    private var fragmentSelectionBinding: FragmentSubredditsSelectionBinding? = null

    private lateinit var navHostFragment: NavHostFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDisposables = CompositeDisposable()
        navHostFragment = childFragmentManager
            .findFragmentById(R.id.subscreen_nav_container) as NavHostFragment
        val binding = FragmentSubredditsSelectionBinding.bind(view)

        postAdapter = PostsAdapter {
                x -> subsAndPostsVM.processInput(Screen1Event.ClickOnT3ViewEvent(x.name))
        }
        subredditAdapter = SubredditsAdapter { x ->
            val inBackStack = navHostFragment.navController.currentBackStack.value
                .any { entry -> entry.arguments?.getString("key") ==x.name }

            if (inBackStack) {
                subsAndPostsVM.processInput(Screen1Event.MakeSnackBarEffect)
            }else{
                subsAndPostsVM.processInput(Screen1Event.ClickOnT5ViewEvent(x.name))
            }
        }

        fragmentSelectionBinding = binding.apply {
            postsRv.layoutManager = LinearLayoutManager(requireContext())
            postsRv.adapter = postAdapter
            subredditsRv.layoutManager = LinearLayoutManager(requireContext())
            subredditsRv.adapter = subredditAdapter
        }

        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.postFragment -> {
                    // When viewing a post, only the back button is enabled.
                    enableButtons(onlyBack = true)
                }
                R.id.subredditFragment -> {
                    // When viewing a subreddit, both back and save are enabled.
                    enableButtons(onlyBack = false)
                }
                else -> {
                    // On the start destination (blank fragment), all buttons are disabled.
                    disableButtons()
                }
            }
        }
        //Gets rid of db errors when you rapidly click on one button still are errors when you
        // click different buttons rapidly . I could remove these for back and refresh combos but
        // not for save and delete bc dsave and delete each make 2 events so no easy way to throttle
        val backClicks :Observable<Screen1Event> = binding.backButton.clicks()
            .map{Screen1Event.UpdateViewingState(getSubNameOrNull())}

        val refreshClicks :Observable<Screen1Event> = binding.refreshButton.clicks()
            .doOnNext { subredditAdapter?.clearSelected() }
            .map{ Screen1Event.RemoveAllSubreddits(subredditAdapter?.currentList?.map{it.displayName}?: emptyList())}

        val backRefreshClicks = backClicks.mergeWith(refreshClicks)
            .throttleFirst(200,TimeUnit.MILLISECONDS)

        val saveClicks = binding.saveButton.clicks()
            .throttleFirst(200,TimeUnit.MILLISECONDS)
            .flatMap {
                Observable.just(
                    Screen1Event.UpdateViewingState(getSubNameOrNull()),
                    Screen1Event.SaveEvent(getSubNameOrNull(), subredditAdapter?.currentList?: emptyList())
                )
            }

        Observable.merge(backRefreshClicks,saveClicks).subscribe {
            subsAndPostsVM.processInput(it)
        }.addTo(viewDisposables!!)

        subsAndPostsVM.vs.observeOn(AndroidSchedulers.mainThread()).subscribe(
            { x-> x.t5ListForRV?.let { subredditAdapter?.submitList(it.vsT5) }
                postAdapter?.submitList(x.t3ListForRV?.vsT3 ?: emptyList())
                x.latestEvent3?.let { t3 -> navigateToPostOrSubreddit(R.id.postFragment, t3) }
                x.latestEvent5?.let { t5 -> navigateToPostOrSubreddit(R.id.subredditFragment, t5) }
                if (x.effect != null){
                    when (x.effect) {
                        Screen1Effect.DELETE_OR_SAVE ->
                        {
                            backPressedPopCurrentSubscreen()
                            subredditAdapter?.clearSelected()

                        }
                        Screen1Effect.SNACKBAR ->
                        {
                                Snackbar.make(
                                    binding.root, "Already clicked. Press back, find",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                subredditAdapter?.setSelected()
                        }
                    }
                        //Clear the effect in case process is recreated so we don't repeat it
                    subsAndPostsVM.processInput(Screen1Event.ClearEffectEvent)
                    }
            },
            { Timber.e("error fetching vs: ${it.localizedMessage}") }
        ).addTo(viewDisposables!!)
    }

    private fun backPressedPopCurrentSubscreen() {
        val navHost = navHostFragment.navController
        val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment
        navHost.navigateUp()
    }

    private fun getSubNameOrNull(): String? {
        val t = navHostFragment.childFragmentManager.primaryNavigationFragment
        val name = (t as? ContentFragment)?.getName()
        return if (name == "BlankFragment") null else name
    }


    private fun navigateToPostOrSubreddit(@IdRes resId: Int, t3OrT5: PartialViewStateScreen1) {
        navHostFragment.navController.navigate(resId, bundleOf("key" to t3OrT5.name))

    }

    private fun disableButtons() {
        fragmentSelectionBinding?.backButton?.visibility = INVISIBLE
        fragmentSelectionBinding?.backButton?.isClickable = false
        fragmentSelectionBinding?.saveButton?.visibility = INVISIBLE
        fragmentSelectionBinding?.saveButton?.isClickable = false
    }


    private fun enableButtons(onlyBack: Boolean) {
        fragmentSelectionBinding?.backButton?.visibility = VISIBLE
        fragmentSelectionBinding?.backButton?.isClickable = true

        if (onlyBack) {
            // Explicitly hide the save button when only the back button should be visible.
            fragmentSelectionBinding?.saveButton?.visibility = INVISIBLE
            fragmentSelectionBinding?.saveButton?.isClickable = false
        } else {
            // Show the save button otherwise.
            fragmentSelectionBinding?.saveButton?.visibility = VISIBLE
            fragmentSelectionBinding?.saveButton?.isClickable = true
        }
    }
    override fun onDestroyView() {

        fragmentSelectionBinding?.postsRv?.adapter = null
        fragmentSelectionBinding?.subredditsRv?.adapter = null
        subredditAdapter = null
        postAdapter = null
        fragmentSelectionBinding = null
        viewDisposables?.clear()  // Clear view-specific subscriptions
        viewDisposables = null
        super.onDestroyView()

    }
}

