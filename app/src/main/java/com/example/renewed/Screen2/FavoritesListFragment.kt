package com.example.renewed.Screen2

import android.graphics.Color
import android.os.Bundle

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.renewed.R
import com.example.renewed.VIEWPAGER_PAGES_TOTAL
import com.example.renewed.VP_PAGES_PER_LOAD
import com.example.renewed.atomic
import com.example.renewed.databinding.FragmentFavoritesListBinding
import com.example.renewed.models.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding4.viewpager2.*
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class FavoritesListFragment : Fragment(R.layout.fragment_favorites_list) {
    private lateinit var binding: FragmentFavoritesListBinding

    @Inject
    lateinit var exo: ExoPlayer
    private val favoritesVM: FavoritesListVM by viewModels()
    private val disposables = CompositeDisposable()
    private lateinit var vp: ViewPager2
    private lateinit var adapter2 : FavoritesListAdapter
    private var selectPos: Int by atomic(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate in FavoritesListFragment")
        super.onCreate(savedInstanceState)
        selectPos = savedInstanceState?.getInt("pos") ?: 0


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated in FavoritesListFragment")
        adapter2 = FavoritesListAdapter(this)

        binding = FragmentFavoritesListBinding.bind(view)
        binding.apply {
            vp = pager
            pager.adapter = adapter2
            pager.offscreenPageLimit = 6
            pager.orientation = ViewPager2.ORIENTATION_VERTICAL
            favorites.setBackgroundColor(Color.parseColor("black"))
            Glide.with(this@FavoritesListFragment).load(R.drawable.ic_loading).into(loading)
        }

        favoritesVM.vs.observeOn(AndroidSchedulers.mainThread())
            .subscribe { x ->
                x.position?.let {
                    selectPos = it.position
                    vp.post { vp.setCurrentItem(selectPos, true) }
                }
                x.currentlyDisplayedList?.let { adapter2.replaceList(it.posts) }
                x.effect?.let {
                    favoritesVM.processInput(Screen2Event.ClearEffectEvent)
                    when (x.effect) {
                        Screen2Effect.DELETE ->
                           favoritesVM.processInput(
                            Screen2Event.AddSubredditsEvent(
                                VP_PAGES_PER_LOAD
                            )
                        )
                        Screen2Effect.LOAD ->  vp.post{}
                    }
                }}
            .addTo(disposables)
    }



    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("pos",selectPos)
    }

    override fun onDestroyView() {
        Timber.d("onDestroyView in FavoritesListFragment")
        disposables.clear()
        super.onDestroyView()
    }

    override fun onResume() {
        Timber.d("onResume in FavoritesListFragment")
        super.onResume()
        vp.pageScrollStateChanges().subscribe(){if (vp.scrollState==ViewPager2.SCROLL_STATE_IDLE) {
            binding.loading.visibility=View.INVISIBLE;vp.visibility=View.VISIBLE;vp.isUserInputEnabled=true;
        }}
        vp.pageSelections().subscribe { position -> Timber.d("THELIISPOS $position")
            if (position == adapter2.postIds.size - 4 && adapter2.postIds.size == VIEWPAGER_PAGES_TOTAL) {
                vp.visibility=View.INVISIBLE
                binding.loading.visibility= View.VISIBLE
                vp.isUserInputEnabled=false
                favoritesVM.processInput(Screen2Event.UpdatePositionEvent(
                    position - VP_PAGES_PER_LOAD))

                //when DeleteSubredditEvent returns t, SaveSubredditEvent will be called
                favoritesVM.processInput(Screen2Event.DeleteSubredditEvent(
                                                         adapter2.postIds.take(VP_PAGES_PER_LOAD)))
            }
            else {
                favoritesVM.processInput(Screen2Event.UpdatePositionEvent(position))
            }
        }
        favoritesVM.processInput(Screen2Event.UpdatePositionEvent(selectPos))
    }
}

