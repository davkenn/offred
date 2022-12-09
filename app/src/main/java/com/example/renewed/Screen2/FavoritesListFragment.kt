package com.example.renewed.Screen2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.renewed.R

import com.example.renewed.databinding.FragmentFavoritesListBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.jakewharton.rxbinding4.view.changeEvents
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
    private val disposables = CompositeDisposable()
    private lateinit var vp: ViewPager2
    private lateinit var adapter2 : FavoritesListAdapter

    private val favoritesVM: FavoritesListVM by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate in FavoritesListFragment")
        super.onCreate(savedInstanceState)
        exo.addListener(readyToPlayListener)
    }

    private val stopPlayerCompleteListener = object : Player.Listener { // player listener

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) { // check player play back state
                Player.STATE_READY -> {}
                Player.STATE_ENDED -> {}
                Player.STATE_BUFFERING ->{}
                Player.STATE_IDLE -> {
                    adapter2.startVideoAtPosition(vp.currentItem)
                    exo.removeListener(this)
                    //your logic
                }
                else -> {}
            }
        }
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
        Timber.d("onViewCreated in FavoritesListFragment")

        super.onViewCreated(view, savedInstanceState)

        adapter2 = FavoritesListAdapter(this)
        val binding = FragmentFavoritesListBinding.bind(view)
        binding.apply {

            //TODO bug is now that first position doesnt start
                        pager.adapter = adapter2
                        pager.offscreenPageLimit=6
                        pager.orientation=ViewPager2.ORIENTATION_VERTICAL

                        vp = pager
        }





        favoritesVM.vs.observeOn(AndroidSchedulers.mainThread())
            .subscribe({ Timber.d("FavoritesListVM::$it")
                         adapter2.replaceList(it) },
                       { Timber.e("FAVLISTERROR",it.stackTrace)}).addTo(disposables)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("pos",vp.currentItem)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        vp.pageSelections().subscribe { position ->
            var red = savedInstanceState?.getInt("pos") ?: 0
            if (position==0 && red != 0) return@subscribe
            exo.addListener(stopPlayerCompleteListener)

                //check to see if we need to call startvideo here or in the adapter
                if (!adapter2.stopVideoAtPosition(position - 1) &&
                    !adapter2.stopVideoAtPosition(position + 1)
                ) {
                    adapter2.startVideoAtPosition(position)

                    exo.removeListener(stopPlayerCompleteListener)
                }

        }.addTo(disposables)

        vp.post{
            vp.currentItem = savedInstanceState?.getInt("pos") ?: 0
        }

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
    }
}