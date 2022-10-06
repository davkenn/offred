package com.example.renewed

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
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

    private val disposables = CompositeDisposable()
    private var disposable: Disposable? = null


    private val subsAndPostsVM: SubredditsAndPostsVM by viewModels()
    private var fragmentSelectionBinding: FragmentSubredditsSelectionBinding? = null


    private var selectedSubreddit: String? by atomicNullable(null)

    private lateinit var navHostFragment: NavHostFragment
//TODO oncreate is called on rotation but only start when you click a frag in the menu
    //maybe need to somehow move to on start
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

        val adapter2 = PostsAdapter { x ->
            subsAndPostsVM.processInput(MyEvent.ClickOnT3ViewEvent(x.name))
        }

        val adapter = SubredditsAdapter { x ->

            selectedSubreddit = x.name
            subsAndPostsVM.processInput(MyEvent.ClickOnT5ViewEvent(x.name))

        }
        val binding = FragmentSubredditsSelectionBinding.bind(view)

        fragmentSelectionBinding = binding.apply {

            subredditsRv2.layoutManager = LinearLayoutManager(requireContext())
            subredditsRv2.adapter = adapter2
            subredditsRv.layoutManager = LinearLayoutManager(requireContext())
            subredditsRv.adapter = adapter

            refreshButton.setOnClickListener {
                subsAndPostsVM.processInput(
                    //todo is displayname ok here
                    MyEvent.RemoveAllSubreddits(adapter.currentList.map { it.name to it.displayName})
                )
            }
            backButton.setOnClickListener {
                subsAndPostsVM.processInput(MyEvent.UpdateViewingState(getSubredditNameOrNull()))
            }
            saveButton.setOnClickListener {
                subsAndPostsVM.processInput(MyEvent.SaveOrDeleteEvent(getSubredditNameOrNull(),
                    false))
                subsAndPostsVM.processInput(MyEvent.UpdateViewingState(getSubredditNameOrNull()))

            }

            //TODO none of these work with t3 yet figure out how to do this here or in vm
            deleteButton.setOnClickListener {
                subsAndPostsVM.processInput(MyEvent.SaveOrDeleteEvent(
                    getSubredditNameOrNull(), true))
                subsAndPostsVM.processInput(MyEvent.UpdateViewingState(getSubredditNameOrNull() ))

            }
        }

        subsAndPostsVM.vs.observeOn(AndroidSchedulers.mainThread()).subscribe(
            { x ->

                x.t5ListForRV?.let { adapter.submitList(it.vsT5)
                }

                adapter2.submitList(x.t3ListForRV?.vsT3 ?: emptyList())

                x.latestEvent3?.let { t3 ->
                    navigateToPostOrSubreddit(R.id.postFragment, t3, binding)
                }

                x.latestEvent5?.let { t5 ->
                    navigateToPostOrSubreddit(R.id.subredditFragment, t5, binding)
                }

               /** x.eventProcessed?.let{
                    val currentDisplayedFragment = navHostFragment.childFragmentManager.primaryNavigationFragment
                    val navController = navHostFragment.navController
                    if ( currentDisplayedFragment is SubredditFragment) {
                        navController.popBackStack(R.id.subredditFragment, true)
                        navController.popBackStack(R.id.subredditFragment, false)
                    }
                    else{
                        navController.popBackStack(R.id.subredditFragment,false)
                    }
                    if (it==EventType.DELETE_OR_SAVE){

                    }


                }**/


                if (x.eventProcessed) {//navHostFragment.navController.navigateUp()
                    val currentDisplayedFragment = navHostFragment.childFragmentManager.primaryNavigationFragment
                    val navController = navHostFragment.navController
                    if ( currentDisplayedFragment is SubredditFragment) {
                        navController.popBackStack(R.id.subredditFragment, true)
                        navController.popBackStack(R.id.subredditFragment, false)
                    }
                    else{
                        navController.popBackStack(R.id.subredditFragment,false)
                    }
                    var n = ( currentDisplayedFragment as ContentFragment).getName()

                    var pt = adapter.currentList
                    var ls = adapter.currentList.filter { it.name != n }

                    adapter.submitList(ls)



                }},

            { Timber.e("error fetching vs: ${it.localizedMessage}") }   )
                                                                .addTo(disposables) }


    private fun getSubredditNameOrNull(): String? {
        var name: String? = null
        var t = navHostFragment.childFragmentManager.primaryNavigationFragment
        t.let { name = (t as ContentFragment).getName() }
        return name
    }

    private fun navigateToPostOrSubreddit(@IdRes resId: Int,
        t5: MyViewState, binding: FragmentSubredditsSelectionBinding, ) {

        var b = navHostFragment.navController.backQueue
            .any { t5.name == it.arguments?.get("key") ?: "NOMATCH" }
        if (!b) navHostFragment.navController.navigate(
                                    resId, bundleOf("key" to t5.name))
        else Snackbar.make(
            binding.root, "Already in Stack. Press back to find it...",
            Snackbar.LENGTH_SHORT
        ).show()
    }


    override fun onStart() {
        super.onStart()
        Timber.d("onStart in home Fragment")
        disposable = subsAndPostsVM.prefetch()
            .concatWith {
                subsAndPostsVM.processInput(
                    MyEvent.ScreenLoadEvent(
                        selectedSubreddit))
                    }.subscribe({ Timber.d("----done fetching both ") },
                { Timber.e("----error fetching is ${it.localizedMessage}")
                })
    }

    override fun onResume() {
        Timber.d("onResume in home Fragment")
        //subsAndPostsVM.processInput(MyEvent.ScreenLoadEvent)
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
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









