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
import com.jakewharton.rxbinding4.material.select
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


    private var selectedSubreddit: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

//        disposable = subsAndPostsVM.go(1).subscribe(
  //          { Timber.d("----done fetching both ") },
   //         { Timber.e("----error fetching is ${it.localizedMessage}") })

        selectedSubreddit= savedInstanceState?.getString("SELECTED_SUB")
 //   if (savedInstanceState==null) {
   //      Timber.d("----called on first creation")
     // subsAndPostsVM.processInput(MyEvent.ScreenLoadEvent(selectedSubreddit))
    //}
        super.onCreate(savedInstanceState)

        //here or in onresume?


    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            putString("SELECTED_SUB", selectedSubreddit)
        }
        super.onSaveInstanceState(outState)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("onViewCreated in home Fragment")

        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.subscreen_nav_container) as NavHostFragment

        val adapter2 = PostsAdapter { x ->
            subsAndPostsVM.processInput(MyEvent.ClickOnT3ViewEvent(x.name))
        }

        val adapter = SubredditsAdapter { x ->
           //TODO fix this weird side effecty way
            selectedSubreddit =x.name
            subsAndPostsVM.processInput(MyEvent.ClickOnT5ViewEvent(x.name))

        }
        val binding = FragmentSubredditsSelectionBinding.bind(view)

        fragmentSelectionBinding = binding.apply {
            subredditsRv2.layoutManager = LinearLayoutManager(requireContext())
            subredditsRv2.adapter = adapter2
            subredditsRv.layoutManager = LinearLayoutManager(requireContext())
            subredditsRv.adapter = adapter

            button1.setOnClickListener {
                selectedSubreddit=null
                subsAndPostsVM.processInput(
                    MyEvent.RemoveAllSubreddits(adapter.currentList.map { it.name })
                )
            }

            button2.setOnClickListener {

                if (navHostFragment.childFragmentManager.fragments.reversed()[0] is SubredditFragment) {
                    Timber.e("ITS A SUB FRAG")
                    (navHostFragment.childFragmentManager.fragments.reversed()[0]
                            as SubredditFragment).setNotDisplayed()
                }

                navHostFragment.navController.navigateUp()
            }
        }


        subsAndPostsVM.vs.observeOn(AndroidSchedulers.mainThread()).subscribe(
            { x ->
                    x.t5ListForRV?.let { adapter.submitList(it.vsT5) }

                //    x.t3ListForRV?.let { adapter2.submitList(it.vsT3) }

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

                { Timber.e("error fetching vs: ${it.localizedMessage}") }).addTo(disposables)
        }


            override fun onStart() {
                super.onStart()
                Timber.d("onStart in home Fragment")

                disposable = subsAndPostsVM.go(1)
                    .concatWith{subsAndPostsVM.processInput(MyEvent.ScreenLoadEvent(
                    selectedSubreddit))}.subscribe(
                    { Timber.d("----done fetching both ") },
                    { Timber.e("----error fetching is ${it.localizedMessage}") })


         //       subsAndPostsVM.processInput(MyEvent.ScreenLoadEvent(selectedSubreddit))

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

