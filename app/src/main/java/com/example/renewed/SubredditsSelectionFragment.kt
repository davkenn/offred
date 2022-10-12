@file:Suppress("SpellCheckingInspection")

package com.example.renewed

import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.renewed.databinding.FragmentSubredditsSelectionBinding

import com.example.renewed.models.MyEvent
import com.example.renewed.models.PartialViewState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import timber.log.Timber


@AndroidEntryPoint
class SubredditsSelectionFragment : Fragment(R.layout.fragment_subreddits_selection) {



    private lateinit var backButton1: Button
    private lateinit var subredditAdapter: SubredditsAdapter
    private lateinit var postAdapter: PostsAdapter
    private val disposables = CompositeDisposable()
    private var disposable: Disposable? = null

    private lateinit var subRV: RecyclerView
    private lateinit var postRV: RecyclerView
    private val subsAndPostsVM: SubredditsAndPostsVM by viewModels()
    private var fragmentSelectionBinding: FragmentSubredditsSelectionBinding? = null

    private var selectPos:Int by atomic(-1)
    private var buttonStatus:Boolean? by atomicNullable(null)

    private lateinit var saveButton1: Button
    private lateinit var deleteButton1: Button
    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buttonStatus = savedInstanceState?.getBoolean("button_enabled")
        selectPos= savedInstanceState?.getInt("selected_pos")?:-1
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
           //TODO do i need to fix button status too like selectedpos
            buttonStatus?.let { putBoolean("button_enabled", it) }
            putInt("selected_pos",selectPos )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated in home Fragment")

        navHostFragment = childFragmentManager
            .findFragmentById(R.id.subscreen_nav_container) as NavHostFragment

        val binding = FragmentSubredditsSelectionBinding.bind(view)
        postAdapter =  PostsAdapter { x ->
            subsAndPostsVM.processInput(MyEvent.ClickOnT3ViewEvent(x.name))
        }
        subredditAdapter= SubredditsAdapter { x ->

            selectPos = subredditAdapter._selected
            subsAndPostsVM.processInput(MyEvent.ClickOnT5ViewEvent(x.name))
        }

            fragmentSelectionBinding = binding.apply {
                postsRv.layoutManager = LinearLayoutManager(requireContext())
                postsRv.adapter = postAdapter
                subredditsRv.layoutManager = LinearLayoutManager(requireContext())
                subredditsRv.adapter = subredditAdapter
                subRV=subredditsRv
                postRV=postsRv

            refreshButton.setOnClickListener {
                subredditAdapter.clearSelected()
                selectPos=-1
                subsAndPostsVM.processInput(
                    MyEvent.RemoveAllSubreddits(subredditAdapter.currentList.map { it.displayName })
                )
            }

            backButton.setOnClickListener {
                subsAndPostsVM.processInput(MyEvent.UpdateViewingState(getSubredditNameOrNull()))
            }

            saveButton.setOnClickListener {
                //TODO be sure there are no timing connections between these two events
                //seems like maybe there is bc i couldnt delete before I update viewing state
                subsAndPostsVM.processInput(MyEvent.UpdateViewingState(getSubredditNameOrNull()))
                subsAndPostsVM.processInput(MyEvent.SaveOrDeleteEvent(getSubredditNameOrNull(), false))
            }

            deleteButton.setOnClickListener {
                subsAndPostsVM.processInput(MyEvent.UpdateViewingState(getSubredditNameOrNull()))
                subsAndPostsVM.processInput(MyEvent.SaveOrDeleteEvent(getSubredditNameOrNull(), true))
            }


            saveButton1 = saveButton
            deleteButton1 = deleteButton
                backButton1 = backButton
                if (buttonStatus==true)enableButtons()
            }


        subsAndPostsVM.vs.observeOn(AndroidSchedulers.mainThread()).subscribe(
            { x ->

                x.t5ListForRV?.let {
                    subredditAdapter.submitList(it.vsT5)
                }

                postAdapter.submitList(x.t3ListForRV?.vsT3 ?: emptyList())

                x.latestEvent3?.let { t3 ->
                    navigateToPostOrSubreddit(R.id.postFragment, t3, binding)

                }

                x.latestEvent5?.let { t5 ->
                    navigateToPostOrSubreddit(R.id.subredditFragment, t5, binding)
                }

                if (x.isEffect) {

                    val n = removeNavTop()
                    subredditAdapter.submitList( subredditAdapter.currentList.filter { it.name != n })

                    //Clear the effect in case process is recreated so we don't repeat it
                    subsAndPostsVM.processInput(MyEvent.ClearEffectEvent)
                    subredditAdapter.clearSelected()
                    selectPos=-1
                }
            },

            { Timber.e("error fetching vs: ${it.localizedMessage}") })
            .addTo(disposables)
    }

    private fun removeNavTop(): String? {
        val previousTop = getSubredditNameOrNull()

        if (navHostFragment.childFragmentManager.primaryNavigationFragment is PostFragment) {

            navHostFragment.navController.popBackStack(R.id.subredditFragment, false)

        } else if (navHostFragment.childFragmentManager.primaryNavigationFragment is SubredditFragment) {

            navHostFragment.navController.popBackStack(R.id.subredditFragment, true)
            navHostFragment.navController.popBackStack(R.id.subredditFragment, false)
        }

        if (navHostFragment.navController.backQueue.size > 2) enableButtons()

        else disableButtons(true)

        return previousTop
    }


    private fun getSubredditNameOrNull(): String? {
        var name: String? = null
        val t = navHostFragment.childFragmentManager.primaryNavigationFragment
        t.let { name = (t as ContentFragment).getName() }
        if (name == "BlankFragment") return null
        return name
    }

    private fun navigateToPostOrSubreddit(
        @IdRes resId: Int,
        t3OrT5: PartialViewState, binding: FragmentSubredditsSelectionBinding,
    ) {
        val b = navHostFragment.navController.backQueue
            .any { t3OrT5.name == (it.arguments?.get("key") ?: "NOMATCH") }
        if (b){
            Snackbar.make(
                binding.root, "Already in Stack. Press back to find it...",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        else{
            navHostFragment.navController.navigate(resId, bundleOf("key" to t3OrT5.name))

            if (t3OrT5 is PartialViewState.T3ForViewing) disableButtons(false) else enableButtons()
        }
    }

    private fun disableButtons(includingBack:Boolean) {
        if (includingBack){
            backButton1.visibility= INVISIBLE
            backButton1.isClickable=false
        }
        deleteButton1.visibility = INVISIBLE
        deleteButton1.isClickable = false
        saveButton1.visibility = INVISIBLE
        saveButton1.isClickable = false
        //TODO is this not working anymore im using this in a messy way
        buttonStatus=false
    }

    private fun enableButtons() {
        deleteButton1.isClickable = true
        deleteButton1.visibility = VISIBLE
        backButton1.visibility= VISIBLE
        backButton1.isClickable=true
        saveButton1.visibility = VISIBLE
        saveButton1.isClickable = true
        buttonStatus=true
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart in home Fragment")
//this is to check if its the first time being loaded and only loads it then
        if (buttonStatus!=null)  {
            subredditAdapter.setSelect(selectPos,subRV.findViewHolderForAdapterPosition(selectPos))
            return
        }

        disposable = subsAndPostsVM.prefetch()
            .concatWith {
                subsAndPostsVM.processInput(
                    MyEvent.ScreenLoadEvent(""))
            }
            .subscribe({ Timber.d("----done fetching both ") },
                {
                    Timber.e("----error fetching is ${it.localizedMessage}")
                })
    }
//TODO its fucked up that im not pausing the disposable here I think FIX THISSS

    override fun onPause() {
        Timber.d("onResume in home Fragment")
        super.onPause()
    }

    override fun onResume() {
        Timber.d("onResume in home Fragment")
        super.onResume()
    }

    override fun onDestroyView() {
        fragmentSelectionBinding = null
        disposables.clear()
        super.onDestroyView()
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }
}









