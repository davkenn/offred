package com.example.renewed.Screen2

import android.graphics.Color
import android.os.Bundle

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.renewed.R
import com.example.renewed.atomic
import com.example.renewed.databinding.FragmentFavoritesListBinding
import com.example.renewed.models.MyFavsEvent
import com.example.renewed.models.PartialViewState
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.jakewharton.rxbinding4.viewpager2.*
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class FavoritesListFragment : Fragment(R.layout.fragment_favorites_list) {


    @Inject
    lateinit var exo: ExoPlayer
    private val favoritesVM: FavoritesListVM by viewModels()
    private val disposables = CompositeDisposable()
    private lateinit var vp: ViewPager2
    private lateinit var adapter2 : FavoritesListAdapter
    //-1 as a test its correctly loading position state
    private var selectPos: Int by atomic(-1)
    var p :Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate in FavoritesListFragment")
        super.onCreate(savedInstanceState)
        exo.addListener(readyToPlayListener)
        p = savedInstanceState?.getInt("pos") ?: 0
    }

    private val readyToPlayListener = object : Player.Listener { // player listener
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) { // check player play back state
                Player.STATE_READY -> {
                    exo.playWhenReady= true
                }
                Player.STATE_ENDED -> {}
                Player.STATE_BUFFERING -> {}
                Player.STATE_IDLE -> {}
                else -> {}
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("onViewCreated in FavoritesListFragment")

        adapter2 = FavoritesListAdapter(this)

        val binding = FragmentFavoritesListBinding.bind(view)
        binding.apply {
            vp = pager
            pager.adapter = adapter2
            //need to keep this as least as high as the number of pages
            pager.offscreenPageLimit = 10
            pager.orientation = ViewPager2.ORIENTATION_VERTICAL
            pager.setBackgroundColor(Color.parseColor("black"))
        }
//this filter is so I don't get adapter bugs for createfragment
        favoritesVM.vs.filter{it.size>7}.observeOn(AndroidSchedulers.mainThread())
            .subscribe({ Timber.d("FavoritesListVM::$it"); adapter2.replaceList(it) },
                { Timber.e("FAVLISTERROR", it.stackTrace) })
            .addTo(disposables)

        favoritesVM.vsPos.observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.d("THELIISEVENTS $it"); selectPos = it
                vp.post{vp.setCurrentItem( selectPos,false)}
           },
                { Timber.d("ERROR IN POS") })
            .addTo(disposables)

    favoritesVM.vs3.observeOn(AndroidSchedulers.mainThread())
        //can call adapter2.removeFirst here it works so even is coming back
    .filter{ it is PartialViewState.SnackbarEffect }.subscribe({favoritesVM.processInput(MyFavsEvent.AddSubredditsEvent)},
    { Timber.e("FAVLISTERROR", it.stackTrace) })
    .addTo(disposables)

    favoritesVM.vs4.observeOn(AndroidSchedulers.mainThread())
        .filter{ it is PartialViewState.T3ForViewing }.subscribe({},{}).addTo(disposables)


    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("pos",selectPos)
    }

    override fun onDestroy() {
        Timber.d("onDestroy in FavoritesListFragment")
        super.onDestroy()
    }

    override fun onDestroyView() {
        Timber.d("onDestroyView in FavoritesListFragment")
    //here or in ondestroy?
        disposables.clear()
        super.onDestroyView()
    }

    override fun onPause() {
        Timber.d("onPause in FavoritesListFragment")
        super.onPause()
    }

    override fun onResume() {
        Timber.d("onResume in FavoritesListFragment")
        super.onResume()

        vp.pageSelections().subscribe { position -> Timber.d("THELIISPOS $position")

            //I think here is where the bug is. If the size isn't 12 it doesn't advance. but if size
            //isn't 12 still will have a null sneak in there so thats prob where the bug is
            if (position == adapter2.postIds.size - 4 && adapter2.postIds.size == 12) {

                favoritesVM.processInput(MyFavsEvent.DeleteSubredditEvent(adapter2.postIds.take(6)))
                vp.post { repeat(6) { adapter2.removeFirst() } }
                favoritesVM.processInput(MyFavsEvent.UpdatePositionEvent(position - 6))
            } else {
                favoritesVM.processInput(MyFavsEvent.UpdatePositionEvent(position))
            }

            vp.post {
                var a = selectPos
                Timber.d("THELIIS $a")
            }

            adapter2.startVideoAtPosition(position)
        }

        favoritesVM.processInput(MyFavsEvent.UpdatePositionEvent(p?:0))
    }}

