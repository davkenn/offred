@file:Suppress("SpellCheckingInspection")

package com.example.renewed.Screen1

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
import com.example.renewed.*
import com.example.renewed.Screen1.Subscreen.ContentFragment
import com.example.renewed.Screen1.Subscreen.PostFragment
import com.example.renewed.Screen1.Subscreen.SubredditFragment
import com.example.renewed.databinding.FragmentSubredditsSelectionBinding
import com.example.renewed.models.Screen1Effect
import com.example.renewed.models.Screen1Event
import com.example.renewed.models.PartialViewStateScreen1
import com.example.renewed.test.CountingIdleResource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import timber.log.Timber
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SubredditsSelectionFragment : Fragment(R.layout.fragment_subreddits_selection) {

    private val subsAndPostsVM: SubredditsAndPostsVM by viewModels()
    private lateinit var subredditAdapter: SubredditsAdapter
    private lateinit var postAdapter: PostsAdapter
    private val disposables = CompositeDisposable()
    private var fragmentSelectionBinding: FragmentSubredditsSelectionBinding? = null
    private var saveEnabled: Boolean by atomic(false)
    private var backEnabled: Boolean by atomic(false)
    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate in SubredditsSelectionFragment")
         savedInstanceState?.let {
             saveEnabled = it.getBoolean("delete_enabled")
             backEnabled = it.getBoolean("back_enabled")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
            putBoolean("delete_enabled", saveEnabled)
            putBoolean("back_enabled", backEnabled)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated in home Fragment")
        navHostFragment = childFragmentManager
            .findFragmentById(R.id.subscreen_nav_container) as NavHostFragment

        val binding = FragmentSubredditsSelectionBinding.bind(view)
        postAdapter = PostsAdapter {
                x -> subsAndPostsVM.processInput(Screen1Event.ClickOnT3ViewEvent(x.name))
        }
        subredditAdapter = SubredditsAdapter {
                x -> subsAndPostsVM.processInput(Screen1Event.ClickOnT5ViewEvent(x.name))
        }

        fragmentSelectionBinding = binding.apply {
            postsRv.layoutManager = LinearLayoutManager(requireContext())
            postsRv.adapter = postAdapter
            subredditsRv.layoutManager = LinearLayoutManager(requireContext())
            subredditsRv.adapter = subredditAdapter
        }

            //Gets rid of db errors when you rapidly click on one button still are errors when you
        // click different buttons rapidly . I could remove these for back and refresh combos but
        // not for save and delete bc dsave and delete each make 2 events so no easy way to throttle
        val backClicks :Observable<Screen1Event> = binding.backButton.clicks()
            .map{Screen1Event.UpdateViewingState(getSubNameOrNull())}

        val refreshClicks :Observable<Screen1Event> = binding.refreshButton.clicks()
            .doOnNext { subredditAdapter.clearSelected() }
            .map{ Screen1Event.RemoveAllSubreddits(subredditAdapter.currentList.map { it.displayName })}

        val backRefreshClicks = backClicks.mergeWith(refreshClicks)
            .throttleFirst(200,TimeUnit.MILLISECONDS)


        val saveClicks = binding.saveButton.clicks()
            .throttleFirst(200,TimeUnit.MILLISECONDS)
            .flatMap {
                Observable.just(
                    Screen1Event.UpdateViewingState(getSubNameOrNull()),
                    Screen1Event.SaveEvent(getSubNameOrNull(), subredditAdapter.currentList)
                )
            }

        Observable.merge(backRefreshClicks,saveClicks).subscribe {
            subsAndPostsVM.processInput(it)
        }

    //    subsAndPostsVM.processInput(Screen1Event.ScreenLoadEvent(""))
        subsAndPostsVM.vs.observeOn(AndroidSchedulers.mainThread()).subscribe(
            { x-> x.t5ListForRV?.let { subredditAdapter.submitList(it.vsT5) }
                postAdapter.submitList(x.t3ListForRV?.vsT3 ?: emptyList())
                x.latestEvent3?.let { t3 -> navigateToPostOrSubreddit(R.id.postFragment, t3) }
                x.latestEvent5?.let { t5 -> navigateToPostOrSubreddit(R.id.subredditFragment, t5) }
                if (x.effect != null){
                    when (x.effect) {
                        Screen1Effect.DELETE_OR_SAVE ->
                        {
                            backPressedPopCurrentSubscreen()
                            subredditAdapter.clearSelected()
                        }
                            Screen1Effect.SNACKBAR ->
                                Snackbar.make(binding.root,"Already clicked. Press back, find",
                                    Snackbar.LENGTH_SHORT).show()
                        }
                        //Clear the effect in case process is recreated so we don't repeat it
                        subsAndPostsVM.processInput(Screen1Event.ClearEffectEvent)
                    }
            },
            { Timber.e("error fetching vs: ${it.localizedMessage}") }
        ).addTo(disposables)
    }

    private fun backPressedPopCurrentSubscreen() {

        val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment
        if (currentFragment is PostFragment) { //If subscreen is a subreddit post
            navHostFragment.navController.popBackStack(R.id.subredditFragment, false)
        }
        else if (currentFragment is SubredditFragment) {  //if subscreen is a subreddit
            navHostFragment.navController.popBackStack(R.id.subredditFragment, true)
            navHostFragment.navController.popBackStack(R.id.subredditFragment, false)
        }
        //after popping the stack, its either a subreddit....
        if (navHostFragment.navController.backQueue.size > 2) enableButtons(onlyBack = false)
        else disableButtons(true)       //...or a blank fragment
    }

    private fun getSubNameOrNull(): String? {
        val t = navHostFragment.childFragmentManager.primaryNavigationFragment
        var name: String? = (t as ContentFragment).getName()
        return if (name == "BlankFragment") null else name
    }

    private fun navigateToPostOrSubreddit(@IdRes resId: Int, t3OrT5: PartialViewStateScreen1) {
        val inBackStack = navHostFragment.navController.backQueue
            .any { t3OrT5.name == (it.arguments?.get("key") ?: "NOMATCH") }

        if (inBackStack && (t3OrT5 is PartialViewStateScreen1.T5ForViewing)) {
            subsAndPostsVM.processInput(Screen1Event.MakeSnackBarEffect)
            return
        }
        navHostFragment.navController.navigate(resId, bundleOf("key" to t3OrT5.name))
        if (t3OrT5 is PartialViewStateScreen1.T3ForViewing) disableButtons(includingBack = false)
                                                            else enableButtons(onlyBack = false)
    }

    private fun disableButtons(includingBack:Boolean) {
        if (includingBack){
            fragmentSelectionBinding?.backButton?.visibility= INVISIBLE
            fragmentSelectionBinding?.backButton?.isClickable=false
            backEnabled=false
        }
        fragmentSelectionBinding?.saveButton?.visibility = INVISIBLE
        fragmentSelectionBinding?.saveButton?.isClickable = false
        saveEnabled=false
    }

    private fun enableButtons(onlyBack:Boolean) {
        fragmentSelectionBinding?.backButton?.visibility= VISIBLE
        fragmentSelectionBinding?.backButton?.isClickable=true
        backEnabled=true

        if (onlyBack) return

        fragmentSelectionBinding?.saveButton?.visibility = VISIBLE
        fragmentSelectionBinding?.saveButton?.isClickable = true
        saveEnabled=true
    }

    override fun onResume() {
        Timber.d("onResume in SubredditSelectionFragment")
        super.onResume()
        if (saveEnabled) enableButtons(onlyBack = false)
        else if (backEnabled) enableButtons(onlyBack = true)
        else disableButtons(true)
    }

    override fun onDestroyView() {
        fragmentSelectionBinding = null
        disposables.clear()
        super.onDestroyView()
    }
}

