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
import com.example.renewed.models.MyFavsEvent
import com.example.renewed.models.PartialViewState
import com.google.android.exoplayer2.ExoPlayer
import com.jakewharton.rxbinding4.viewpager2.*
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import timber.log.Timber
import java.lang.Math.abs
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
    //-1 as a test its correctly loading position state
    private var selectPos: Int by atomic(0)
    var p :Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate in FavoritesListFragment")
        super.onCreate(savedInstanceState)
        p = savedInstanceState?.getInt("pos") ?: 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("onViewCreated in FavoritesListFragment")

        adapter2 = FavoritesListAdapter(this)

        binding = FragmentFavoritesListBinding.bind(view)
        binding.apply {
            vp = pager
            pager.adapter = adapter2
            //need to keep this as least as high as the number of pages
            pager.offscreenPageLimit = 6
            pager.orientation = ViewPager2.ORIENTATION_VERTICAL
            favorites.setBackgroundColor(Color.parseColor("black"))
            Glide.with(this@FavoritesListFragment).load(R.drawable.ic_loading).into(loading)
        }

        favoritesVM.currentlyDisplayedPosts.filter{it.size== VIEWPAGER_PAGES_TOTAL }
                                           .observeOn(AndroidSchedulers.mainThread())
                                           .subscribe({adapter2.replaceList(it) },
                                               { Timber.e("FAVLISTERROR", it.stackTrace) })
                                           .addTo(disposables)

        favoritesVM.currentPosition.observeOn(AndroidSchedulers.mainThread())
                         .subscribe({
                                        selectPos = it
                                      vp.post{vp.setCurrentItem( selectPos,true)} },
                                    { Timber.d("ERROR IN POS") })
                        .addTo(disposables)

        favoritesVM.deletePostsComplete.observeOn(AndroidSchedulers.mainThread())
                       .filter{ it is PartialViewState.SnackbarEffect }
                       .subscribe({ favoritesVM.processInput(
                                                MyFavsEvent.AddSubredditsEvent(VP_PAGES_PER_LOAD))},
                                  { Timber.e("FAVLISTERROR", it.stackTrace) })
                       .addTo(disposables)

        favoritesVM.addPostsComplete.observeOn(AndroidSchedulers.mainThread())
                       .filter{ it is PartialViewState.T3ForViewing }
                       .subscribe({ vp.post{}},{})
                       .addTo(disposables)
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

        vp.pageScrollStateChanges().subscribe(){if (vp.scrollState==ViewPager2.SCROLL_STATE_IDLE) {
            binding.loading.visibility=View.INVISIBLE;vp.visibility=View.VISIBLE;vp.isUserInputEnabled=true;
        }}
        vp.pageSelections().subscribe { position -> Timber.d("THELIISPOS $position")

            //also called when rotate but it doesn't matter because already set to visibile vp.visibility=View.VISIBLE
            if (position == adapter2.postIds.size - 4
                                                && adapter2.postIds.size == VIEWPAGER_PAGES_TOTAL) {
                vp.visibility=View.INVISIBLE
                binding.loading.visibility= View.VISIBLE
                vp.isUserInputEnabled=false

                favoritesVM.processInput(MyFavsEvent.UpdatePositionEvent(
                    position - VP_PAGES_PER_LOAD))

                //when DeleteSubredditEvent returns t, SaveSubredditEvent will be called
                favoritesVM.processInput(MyFavsEvent.DeleteSubredditEvent(
                                                         adapter2.postIds.take(VP_PAGES_PER_LOAD)))
            }
            else {
                favoritesVM.processInput(MyFavsEvent.UpdatePositionEvent(position))
            }
        }

        favoritesVM.processInput(MyFavsEvent.UpdatePositionEvent(p?:0))
    }

    override fun onStop() {
        super.onStop()
        p = selectPos
    }
}

