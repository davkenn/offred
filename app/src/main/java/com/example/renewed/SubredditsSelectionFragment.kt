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
import com.example.renewed.models.MyViewState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import timber.log.Timber


@AndroidEntryPoint
class SubredditsSelectionFragment : Fragment(R.layout.fragment_subreddits_selection) {


    private lateinit var subredditAdapter: SubredditsAdapter
    private lateinit var postAdapter: PostsAdapter
    private val disposables = CompositeDisposable()
    private var disposable: Disposable? = null

    private lateinit var subRV: RecyclerView
    private lateinit var postRV: RecyclerView
    private val subsAndPostsVM: SubredditsAndPostsVM by viewModels()
    private var fragmentSelectionBinding: FragmentSubredditsSelectionBinding? = null


    private var selectedSubreddit: String? by atomicNullable(null)
    private lateinit var saveButton1: Button
    private lateinit var deleteButton1: Button
    private lateinit var navHostFragment: NavHostFragment
//TODO oncreate is called on rotation but only start when you click a frag in the menu

    override fun onCreate(savedInstanceState: Bundle?) {
        selectedSubreddit = savedInstanceState?.getString("key1")
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.run {
            putString("key1", selectedSubreddit)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("onViewCreated in home Fragment")

        navHostFragment = childFragmentManager
            .findFragmentById(R.id.subscreen_nav_container) as NavHostFragment

        postAdapter = PostsAdapter { x ->
            subsAndPostsVM.processInput(MyEvent.ClickOnT3ViewEvent(x.name))
        }

        subredditAdapter = SubredditsAdapter { x ->
            selectedSubreddit = x.name
            subsAndPostsVM.processInput(MyEvent.ClickOnT5ViewEvent(x.name))
        }

        val binding = FragmentSubredditsSelectionBinding.bind(view)

        fragmentSelectionBinding = binding.apply {

            postsRv.layoutManager = LinearLayoutManager(requireContext())
            postsRv.adapter = postAdapter
            subredditsRv.layoutManager = LinearLayoutManager(requireContext())
            subredditsRv.adapter = subredditAdapter
            subRV=subredditsRv
            postRV=postsRv


            refreshButton.setOnClickListener {
                selectedSubreddit = null//?? need this line?
         //       subredditAdapter.clearSelected()
                subsAndPostsVM.processInput(
                    MyEvent.RemoveAllSubreddits(subredditAdapter.currentList.map { it.name to it.displayName })
                )
            }
            backButton.setOnClickListener {
                subsAndPostsVM.processInput(MyEvent.UpdateViewingState(getSubredditNameOrNull()))
            }

            saveButton.setOnClickListener {
                subsAndPostsVM.processInput(MyEvent.UpdateViewingState(getSubredditNameOrNull()))
                subsAndPostsVM.processInput(MyEvent.SaveOrDeleteEvent(getSubredditNameOrNull(), false))
            }


            deleteButton.setOnClickListener {
                    subsAndPostsVM.processInput(MyEvent.UpdateViewingState(getSubredditNameOrNull()))
                    subsAndPostsVM.processInput(MyEvent.SaveOrDeleteEvent(getSubredditNameOrNull(), true))
            }

            saveButton1 = saveButton
            deleteButton1 = deleteButton

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



                if (x.eventProcessed) {//navHostFragment.navController.navigateUp()

                    val navController = navHostFragment.navController

                    val n = getSubredditNameOrNull()
                    if ( navHostFragment.childFragmentManager.primaryNavigationFragment is PostFragment) {
                        navController.popBackStack(R.id.subredditFragment, false)
                    }

                    else if ( navHostFragment.childFragmentManager.primaryNavigationFragment
                                                                     is SubredditFragment) {
                        navController.popBackStack(R.id.subredditFragment, true)
                        navController.popBackStack(R.id.subredditFragment, false)
                    }
                    if (navController.backQueue.size > 2) enableButtons()
                    else disableButtons()

                    subredditAdapter.submitList( subredditAdapter.currentList.filter { it.name != n })
                    subredditAdapter.notifyDataSetChanged()
                    }
            },

            { Timber.e("error fetching vs: ${it.localizedMessage}") })
            .addTo(disposables)
    }




    private fun getSubredditNameOrNull(): String? {
        var name: String? = null
        val t = navHostFragment.childFragmentManager.primaryNavigationFragment
        t.let { name = (t as ContentFragment).getName() }
        return name
    }

    private fun navigateToPostOrSubreddit(
        @IdRes resId: Int,
        t3OrT5: MyViewState, binding: FragmentSubredditsSelectionBinding,
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

            if (t3OrT5 is MyViewState.T3ForViewing) disableButtons() else enableButtons()
        }
    }

    private fun disableButtons() {
        deleteButton1.visibility = INVISIBLE
        deleteButton1.isClickable = false
        saveButton1.visibility = INVISIBLE
        saveButton1.isClickable = false
    }

    private fun enableButtons() {
        deleteButton1.isClickable = true
        deleteButton1.visibility = VISIBLE
        saveButton1.visibility = VISIBLE
        saveButton1.isClickable = true
    }


    override fun onStart() {
        super.onStart()

        Timber.d("onStart in home Fragment")
        disposable = subsAndPostsVM.prefetch()
                                    .concatWith { subsAndPostsVM.processInput(
                                                            MyEvent.ScreenLoadEvent(selectedSubreddit))
                                    }


                                    .subscribe({ Timber.d("----done fetching both ") },
                                    {
                                        Timber.e("----error fetching is ${it.localizedMessage}")
                                    })
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









