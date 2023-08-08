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
import com.example.renewed.models.EffectType
import com.example.renewed.models.MyEvent
import com.example.renewed.models.PartialViewState
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
    private var saveEnabled: Boolean? by atomic(null)
    private var backEnabled: Boolean? by atomic(null)

    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate in SubredditsSelectionFragment")
        if (savedInstanceState==null) {
                  disposables.add(subsAndPostsVM.prefetch()
                    .andThen{
                      subsAndPostsVM.processInput(
                        MyEvent.ScreenLoadEvent(""))
                   }
            .subscribeOn(Schedulers.io())
            .subscribe({ Timber.d("----done fetching both ") },
                { Timber.e("----error fetching is ${it.localizedMessage}") }))
        }
        saveEnabled = savedInstanceState?.getBoolean("delete_enabled")
        backEnabled = savedInstanceState?.getBoolean("back_enabled")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
            saveEnabled?.let { putBoolean("delete_enabled", it) }
            backEnabled?.let { putBoolean("back_enabled", it) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated in home Fragment")

        navHostFragment = childFragmentManager
            .findFragmentById(R.id.subscreen_nav_container) as NavHostFragment

        val binding = FragmentSubredditsSelectionBinding.bind(view)
        postAdapter = PostsAdapter {
                             x -> subsAndPostsVM.processInput(MyEvent.ClickOnT3ViewEvent(x.name))
                           }
        subredditAdapter = SubredditsAdapter {
                             x -> subsAndPostsVM.processInput(MyEvent.ClickOnT5ViewEvent(x.name))
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
        val backClicks :Observable<MyEvent> = binding.backButton.clicks()
                                            .map{MyEvent.UpdateViewingState(getSubNameOrNull())}

        val refreshClicks :Observable<MyEvent> = binding.refreshButton.clicks()
                                     .doOnNext { subredditAdapter.clearSelected() }
                                     .map{ MyEvent.RemoveAllSubreddits(
                                              subredditAdapter.currentList.map { it.displayName })}

        val backRefreshClicks = backClicks.mergeWith(refreshClicks)
                                          .throttleFirst(200,TimeUnit.MILLISECONDS)


        val saveClicks = binding.saveButton.clicks()
                                    .throttleFirst(200,TimeUnit.MILLISECONDS)
                                    .flatMap { Observable.just(MyEvent.UpdateViewingState(
                                                                                getSubNameOrNull()),
                                                       MyEvent.SaveEvent(getSubNameOrNull(),
                                                 subredditAdapter.currentList)) }

        Observable.merge(backRefreshClicks,saveClicks)
                  .subscribe { subsAndPostsVM.processInput(it) }

        subsAndPostsVM.vs.observeOn(AndroidSchedulers.mainThread()).subscribe(
                        { x -> x.t5ListForRV?.let { subredditAdapter.submitList(it.vsT5) }
                        postAdapter.submitList(x.t3ListForRV?.vsT3 ?: emptyList())
                        x.latestEvent3?.let { t3 -> navigateToPostOrSubreddit(R.id.postFragment, t3) }
                        x.latestEvent5?.let { t5 -> navigateToPostOrSubreddit(R.id.subredditFragment, t5) }
                        if (x.effect != null){
                           when (x.effect) {
                               EffectType.DELETE_OR_SAVE -> {
                                                             popTopViewerElement()
                                                             subredditAdapter.clearSelected()
                              }
                               EffectType.SNACKBAR -> Snackbar.make(binding.root,
                                   "Already in Stack. Press back to find it...", Snackbar.LENGTH_SHORT)
                                   .show()
                    }
                        //Clear the effect in case process is recreated so we don't repeat it
                        subsAndPostsVM.processInput(MyEvent.ClearEffectEvent)
                    }
                },
                { Timber.e("error fetching vs: ${it.localizedMessage}") })
                .addTo(disposables)
    }

    private fun popTopViewerElement() {
        if (navHostFragment.childFragmentManager.primaryNavigationFragment is PostFragment) {

            navHostFragment.navController.popBackStack(R.id.subredditFragment, false)

        } else if (navHostFragment.childFragmentManager.primaryNavigationFragment is SubredditFragment) {

            navHostFragment.navController.popBackStack(R.id.subredditFragment, true)
            navHostFragment.navController.popBackStack(R.id.subredditFragment, false)
        }
        //after popping the stack, its either a subreddit....
        if (navHostFragment.navController.backQueue.size > 2) enableButtons(onlyBack = false)
        else disableButtons(true)       //...or a blank fragment
    }

    private fun getSubNameOrNull(): String? {
        val t = navHostFragment.childFragmentManager.primaryNavigationFragment
        var name: String?
        t.let { name = (t as ContentFragment).getName() }
        if (name == "BlankFragment") return null
        return name
    }

    private fun navigateToPostOrSubreddit(@IdRes resId: Int, t3OrT5: PartialViewState) {
        //TODO right now it is giving error if add again but bring up the posts
        //ANOTHER GOOD OPTION IS TO JUST MOVE IT TO THE FRONT OF THE queue
        val inBackStack = navHostFragment.navController.backQueue
            .any { t3OrT5.name == (it.arguments?.get("key") ?: "NOMATCH") }

        if (inBackStack && (t3OrT5 is PartialViewState.T5ForViewing)) {
            subsAndPostsVM.processInput(MyEvent.MakeSnackBarEffect)
            return
        }

        navHostFragment.navController.navigate(resId, bundleOf("key" to t3OrT5.name))
        if (t3OrT5 is PartialViewState.T3ForViewing) disableButtons(includingBack = false)
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
        saveEnabled?.let { if (it)enableButtons(onlyBack = false)
                                    else if (backEnabled != null && backEnabled as Boolean)
                                        enableButtons(onlyBack = true)
                                 else disableButtons(true)
        }
    }

    override fun onDestroyView() {
        fragmentSelectionBinding = null
        disposables.clear()
        super.onDestroyView()
    }
}

