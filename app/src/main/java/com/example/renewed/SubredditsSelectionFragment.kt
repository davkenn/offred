package com.example.renewed

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.renewed.*
import com.example.renewed.models.MyEvent

import com.example.renewed.databinding.FragmentSubredditsSelectionBinding
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.addTo
import timber.log.Timber


@AndroidEntryPoint
class SubredditsSelectionFragment : Fragment(R.layout.fragment_subreddits_selection) {
    //    private val postClick: PublishSubject<String> = PublishSubject.create()
    private val disposables = CompositeDisposable()
    private var disposable: Disposable? = null
    private val subsAndPostsVM: SubredditsAndPostsVM by viewModels()
    private var fragmentSelectionBinding: FragmentSubredditsSelectionBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        disposable = subsAndPostsVM.go(1).subscribe(
            { Timber.d("----done fetching both ") },
            { Timber.e("----error fetching is ${it.localizedMessage}") })



        super.onCreate(savedInstanceState)

        //here or in onresume?


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        Timber.d("onViewCreated in home Fragment")


        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.subscreen_nav_container) as NavHostFragment

//TODO no need for this here right bc cascading delete?
        val adapter2 = PostsAdapter { x ->
            subsAndPostsVM.processInput(MyEvent.ClickOnT3ViewEvent(x.name))
        }

        val adapter = SubredditsAdapter { x ->
            subsAndPostsVM.processInput(MyEvent.ClickOnT5ViewEvent(x.name))

        }
        val binding = FragmentSubredditsSelectionBinding.bind(view)

        fragmentSelectionBinding = binding.apply {
            subredditsRv2.layoutManager = LinearLayoutManager(requireContext())
            subredditsRv2.adapter = adapter2
            subredditsRv.layoutManager = LinearLayoutManager(requireContext())
            subredditsRv.adapter = adapter

            button1.setOnClickListener {
                subsAndPostsVM.processInput(
                    MyEvent.RemoveAllSubreddits(adapter.currentList.map { it.name })
                )
            }

            //TODO here i can remove it by marking it unviewed do i need to double up depends on
            //if it disappears and if i click again
            //i can also change the way the back arrow looks here
            button2.setOnClickListener {

                if (navHostFragment.childFragmentManager.fragments.reversed()[0] is SubredditFragment) {
                    Timber.e("ITS A SUB FRAG")
                    //TODO this is a cheat clean this up
                    (navHostFragment.childFragmentManager.fragments.reversed()[0]
                            as SubredditFragment).setNotDisplayed()
                }

                navHostFragment.navController.navigateUp()
            }
        }



            subsAndPostsVM.vs.observeOn(AndroidSchedulers.mainThread()).subscribe(

                { x ->

                    x.t5ListForRV?.let { adapter.submitList(it.vsT5) }

                    adapter2.submitList(x.t3ListForRV?.vsT3 ?: emptyList())

                    x.latestEvent3?.let {
                        navHostFragment.navController.navigate(
                            R.id.postFragment,
                            bundleOf("key" to it.t3.name)) }

                    x.latestEvent5?.let {
                        navHostFragment.navController.navigate(
                            R.id.subredditFragment,
                            bundleOf("key" to it.t5.name))}
                },

                { Timber.e("error fetching viewstate: ${it.localizedMessage}") })
                .addTo(disposables)
        }

            //TODO in here add test to see if i want that button
            //    subsAndPostsVM.viewState3.observeOn(AndroidSchedulers.mainThread()).subscribe{navHostFragment.navController.navigate(R.id.postFragment,
            //      bundleOf("key" to it.t3.name))}


            //subsAndPostsVM.viewState4.observeOn(AndroidSchedulers.mainThread())
            //  .subscribe{navHostFragment.navController.navigate(R.id.subredditFragment,
            // bundleOf("key" to it.t5.name))}}


            override fun onStart() {
                super.onStart()
//SHOULD THIS BE WITH ONCREATE FUNCTION? SOMEWHERE ELSE?

//TODO make sure you should be doing it here and if so where do I stop it so no bugs?
         //       disposable = subsAndPostsVM.go(1).subscribe(
           //         { Timber.d("----done fetching both ") },
             //       { Timber.e("----error fetching is ${it.localizedMessage}") })

                subsAndPostsVM.processInput(MyEvent.ScreenLoadEvent)

            }

            override fun onResume() {
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

