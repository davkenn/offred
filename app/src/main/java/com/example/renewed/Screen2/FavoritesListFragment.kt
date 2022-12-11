package com.example.renewed.Screen2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.renewed.R
import com.example.renewed.atomic

import com.example.renewed.databinding.FragmentFavoritesListBinding
import com.example.renewed.models.MyFavsEvent
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
//TODO maybe call this somewhere ive
fun ViewPager2.getDragSensitivity(): Int {
    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
    recyclerViewField.isAccessible = true
    val recyclerView = recyclerViewField.get(this) as RecyclerView

    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    return touchSlopField.get(recyclerView) as Int
//    touchSlopField.set(recyclerView, touchSlop*f)       // "8" was obtained experimentally
}
@AndroidEntryPoint
class FavoritesListFragment : Fragment(R.layout.fragment_favorites_list) {
    @Inject
    lateinit var exo: ExoPlayer
    private val disposables = CompositeDisposable()
    private lateinit var vp: ViewPager2
    private lateinit var adapter2 : FavoritesListAdapter
    //-1 as a test its correctly loading position state
    private var selectPos: Int by atomic(-1)




    private val favoritesVM: FavoritesListVM by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate in FavoritesListFragment")
        super.onCreate(savedInstanceState)
        exo.addListener(readyToPlayListener)
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
            //need to keep this as least as high as the number of pages
                        pager.offscreenPageLimit=10
                        pager.orientation=ViewPager2.ORIENTATION_VERTICAL
                        vp = pager
        }

        var r = vp.getDragSensitivity()


        favoritesVM.vsPos.observeOn(AndroidSchedulers.mainThread())
            .subscribe({   Timber.d("THELIISEVENTS $it")
                selectPos = it
            })
            .addTo(disposables)

        favoritesVM.vs.observeOn(AndroidSchedulers.mainThread())
            .subscribe({ Timber.d("FavoritesListVM::$it")
                         adapter2.replaceList(it) },
                       { Timber.e("FAVLISTERROR",it.stackTrace)}).addTo(disposables)



/**vp.pageScrollEvents().subscribe(){
  //  vp.currentItem = selectPos
   // adapter2.startVideoAtPosition(selectPos)


    if (it.positionOffset<=0.5 && it.positionOffsetPixels>=44) {
        Timber.d("THELIIUP ${it.positionOffset}")
        favoritesVM.processInput(MyFavsEvent.UpdatePositionEvent(vp.currentItem + 1))
    }
    if (it.positionOffset>0.5 && it.positionOffsetPixels>=44) {
        Timber.d("THELIIDOWN ${it.positionOffset}")

        favoritesVM.processInput(MyFavsEvent.UpdatePositionEvent
                (vp.currentItem -1))
        }

    }
**/
        vp.pageSelections().subscribe { position ->
            //probably need this first

//todo this isnt going to work maybe because I have to wait for a response

            Timber.d("THELIISPOS $position")
            favoritesVM.processInput(MyFavsEvent.UpdatePositionEvent(position))


// dont reverse these or it wont work timing wise. need to start current vid before moving pos
    //        adapter2.startVideoAtPosition(selectPos)
            adapter2.startVideoAtPosition(position)
            vp.post{   var a = selectPos
                Timber.d("THELIIS $a")}

         //   var a = selectPos
     //       Timber.d("THELIIS $a")

   //         vp.currentItem = selectPos


        }.addTo(disposables)
    }




    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)


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