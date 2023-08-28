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
    private lateinit var vpPagesAdapter : FavoritesListAdapter
    private var savedPos: Int by atomic(0)

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

                //The following sends events to implement an infinite list. When swiping forward
                // onto one specified view pager position, an event to delete the first few pages
                //is sent to the view model. When this returns, an event is sent to the viewmodel
                //to load more pages and add them to the end of the viewpager. Also, an event is
                //immediately sent to the view model which will clear the effect state so that
                //this reload is not processed again if the screen is rotated.
                fullViewState.effect?.let {
                    favoritesVM.processInput(Screen2Event.ClearEffectEvent)
                    when (it) {
                        //Upon getting the delete effect, the first few view pager pages were
                        // deleted succesfully so send an event that adds the number of posts that
                        // were deleted to the end of the viewpager
                        Screen2Effect.DELETE -> {
                            favoritesVM.processInput(
                                Screen2Event.AddSubredditsEvent(VP_PAGES_PER_LOAD)
                            )
                        }
                        //Once the load is complete, the new infinite list is ready to view so
                        //hide loading image
                        Screen2Effect.LOAD -> {
                            hideLoading()
                        }
                    }
                }
            }
            .addTo(disposables)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("pos",savedPos)
    }

    override fun onDestroyView() {
        Timber.d("onDestroyView in FavoritesListFragment")
        disposables.clear()
        super.onDestroyView()
    }

    override fun onResume() {
        Timber.d("onResume in FavoritesListFragment")
        super.onResume()
        vp.pageSelections().subscribe { position -> Timber.d("THELIISPOS $position")
            //update position if loading new posts for new pages in infinite list
            if (position == vpPagesAdapter.postIds.size - 4 &&
                vpPagesAdapter.postIds.size == VIEWPAGER_PAGES_TOTAL) {

                showLoading()
                favoritesVM.processInput(
                    Screen2Event.UpdatePositionEvent(position - VP_PAGES_PER_LOAD))
                //when DeleteSubredditEvent returns, SaveSubredditEvent will be called
                favoritesVM.processInput(
                    Screen2Event.DeleteSubredditEvent(vpPagesAdapter.postIds
                        .take(VP_PAGES_PER_LOAD)))
            }
            //update position if not reloading infinite list
            else {
                favoritesVM.processInput(Screen2Event.UpdatePositionEvent(position))
            }
        }
        //update position on rotation
        if (savedPos != 0) favoritesVM.processInput(Screen2Event.UpdatePositionEvent(savedPos))
    }

    /**
     *
     */
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

