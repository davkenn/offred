package com.example.offred.Screen2

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.bumptech.glide.Glide
import com.example.offred.R
import com.example.offred.VP_PAGES_PER_LOAD
import com.example.offred.databinding.FragmentFavoritesListBinding
import com.example.offred.models.*
import com.google.android.exoplayer2.ExoPlayer
import com.jakewharton.rxbinding4.viewpager2.pageScrollStateChanges
import com.jakewharton.rxbinding4.viewpager2.pageSelections
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class FavoritesListFragment : Fragment(
    R.layout.fragment_favorites_list

) {
    private lateinit var binding: FragmentFavoritesListBinding
    @Inject
    lateinit var exo: ExoPlayer
    private val favoritesVM: FavoritesListVM by viewModels()
    private val viewDisposables = CompositeDisposable()
    private val infiniteListDisposables = CompositeDisposable()

    private lateinit var vp: ViewPager2
    private lateinit var vpPagesAdapter : FavoritesListAdapter
    private var savedPos: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate in FavoritesListFragment")
        super.onCreate(savedInstanceState)
        savedPos = savedInstanceState?.getInt("pos") ?: 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated in FavoritesListFragment")
        vpPagesAdapter = FavoritesListAdapter(this)
        binding = FragmentFavoritesListBinding.bind(view)
        binding.apply {
            vp = pager
            pager.adapter = vpPagesAdapter
            pager.offscreenPageLimit = 6
            pager.orientation = ViewPager2.ORIENTATION_VERTICAL
            favorites.setBackgroundColor(Color.parseColor("black"))
            Glide.with(this@FavoritesListFragment)
                .load(R.drawable.ic_loading).into(loading)
        }

        favoritesVM.vs.observeOn(AndroidSchedulers.mainThread())
            .subscribe { fullViewState ->
                //upon receiving a position update view state, set the position on the viewpager
                fullViewState.position?.let {
                    savedPos = it.position
                    vp.post { vp.setCurrentItem(savedPos, true) }
                }

                //upon receiving a new list view state, update the viewpager to contain the
                //new list
                fullViewState.currentlyDisplayedList?.let { vpPagesAdapter.replaceList(it.posts) }

            }
            .addTo(viewDisposables)



    }

    override fun onResume() {
        super.onResume()

        vp.pageScrollStateChanges().subscribe(){state->if(state==SCROLL_STATE_IDLE) hideLoading() }
     //   vp.pageScrollStateChanges().subscribe(){state->if(state== SCROLL_STATE_DRAGGING) showLoading() else hideLoading()}
        vp.pageSelections().subscribe { position -> Timber.d("THELIISPOS $position")
            //update position if loading new posts for new pages in infinite list
            if (position == VP_PAGES_PER_LOAD+2) {
                showLoading()
                favoritesVM.processInput(Screen2Event.UpdatePositionEvent(2))
                favoritesVM.processInput(
                    Screen2Event.LoadMoreEvent(vpPagesAdapter.postIds.take(VP_PAGES_PER_LOAD)))


            }
            //update position if not reloading infinite list
            else {
                favoritesVM.processInput(Screen2Event.UpdatePositionEvent(position))
            }
        }.addTo(infiniteListDisposables)

        //update position on rotation
        if (savedPos != 0) favoritesVM.processInput(Screen2Event.UpdatePositionEvent(savedPos))
    }

    override fun onPause() {
        super.onPause()
        infiniteListDisposables.clear()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("pos",savedPos)
    }

    override fun onDestroyView() {
        Timber.d("onDestroyView in FavoritesListFragment")
        super.onDestroyView()
        viewDisposables.clear()
    }


    private fun showLoading() {
        vp.visibility = View.INVISIBLE
        binding.loading.visibility = View.VISIBLE
        vp.isUserInputEnabled = false
    }

    private fun hideLoading() {
        binding.loading.visibility = View.INVISIBLE;
        vp.visibility = View.VISIBLE
        vp.isUserInputEnabled = true;
    }
}

