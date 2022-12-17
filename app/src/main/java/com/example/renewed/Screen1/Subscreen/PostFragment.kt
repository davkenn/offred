package com.example.renewed.Screen1.Subscreen


import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.core.view.postDelayed
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.renewed.R
import com.example.renewed.databinding.PostViewBinding
import com.example.renewed.models.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.jakewharton.rxbinding4.view.focusChanges
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class PostFragment : ContentFragment() {

    @Inject
    lateinit var exo: ExoPlayer
    var playerView: StyledPlayerView? = null
    var exoPosition: Long = 0
    private val postsVM: PostVM by viewModels()
    private var name:String?= null
    private var isSubScreen:Boolean = false
    var postBinding: PostViewBinding? = null
    var state: ViewStateT3? = null
    private val disposables = CompositeDisposable()
    override fun getName() : String = postsVM.name

    override fun onSaveInstanceState(outState: Bundle) {

    //TODO am i shooting myself in the foot here by only saving instance state from fragmentadapter?
        super.onSaveInstanceState(outState)
        outState.run {
                       putLong("player_pos", exo.currentPosition)
                       putString("key",name)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = PostViewBinding.inflate(inflater,container,false)
        postBinding = binding

    //    postBinding!!.fullImg.setOnClickListener(object:View.OnClickListener {
      //      override fun onClick(v: View?) {
        //        Timber.e("WOOOOODY")
          //  }})
        return binding.root
    }

    override fun onDestroyView() {
        //TODO need to not do this if I don't want crashes
        postBinding = null
        super.onDestroyView()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            exoPosition= savedInstanceState.getLong("player_pos")
            name=              savedInstanceState.getString("key")
            isSubScreen = savedInstanceState.getBoolean("isSubscreen")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.parseColor("black"))
        isSubScreen = arguments?.getBoolean("isSubscreen")?: false


        name = arguments?.getString("key") ?: "NONE"


   //     postBinding!!.nestedScroll
     //       .setOnFocusChangeListener { v, hasFocus -> Timber.e("FOCUS NEST CHANGED $hasFocus") ;if (hasFocus==false) v.requestFocus()}
        postBinding!!.fullImg
            .setOnFocusChangeListener { v, hasFocus -> Timber.e("FOCUS IMG CHANGED $hasFocus") ;if (hasFocus==false) v.requestFocus()}
        var dex: Int = 0
        postBinding!!.fullImg
            .setOnClickListener(object : View.OnClickListener {


                override fun onClick(v: View?) {
                    dex += 1

                    Timber.e("ONCLICK CALLED")
                    Glide.with(this@PostFragment).load(
                        state?.galleryUrls?.get(dex % state?.galleryUrls!!.size)
                    )
                        .into(postBinding!!.fullImg)





                }
            })

}

     //   postBinding!!.fullImg.setOnFocusChangeListener({ v, hasFocus ->if (hasFocus)
       // {Timber.e("GOOOOOOT FOCUS")
         //   v.performClick() }})
 /**   E/AndroidRuntime: FATAL EXCEPTION: main
    Process: com.example.offred, PID: 14675
    java.lang.NullPointerException
    at com.example.renewed.Screen1.Subscreen.PostFragment.onActivityCreated$lambda-3(PostFragment.kt:123)
    at com.example.renewed.Screen1.Subscreen.PostFragment.$r8$lambda$S1vzCuUV1WPgskrHPj82n0CADGg(Unknown Source:0)
    at com.example.renewed.Screen1.Subscreen.PostFragment$$ExternalSyntheticLambda2.accept(Unknown Source:4)
    at io.reactivex.rxjava3.internal.observers.ConsumerSingleObserver.onSuccess(ConsumerSingleObserver.java:62)
    at io.reactivex.rxjava3.internal.operators.single.SingleObserveOn$ObserveOnSingleObserver.run(SingleObserveOn.java:81)
    at io.reactivex.rxjava3.android.schedulers.HandlerScheduler$ScheduledRunnable.run(HandlerScheduler.java:123)
    at android.os.Handler.handleCallback(Handler.java:938)
    at android.os.Handler.dispatchMessage(Handler.java:99)
    at android.os.Looper.loop(Looper.java:223)
    at android.app.ActivityThread.main(ActivityThread.java:7656)
    at java.lang.reflect.Method.invoke(Native Method)
    at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:592)
    at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:947)
**/


    override fun onPause() {
        Timber.d("onPause in Post Fragment")
        super.onPause()
    }

    override fun onStart() {
        postsVM.setPost(name!!)
            .doOnEvent{x,_ -> state=x}
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { t3ViewState ->
                    state = t3ViewState
                    postBinding!!.postName.text = t3ViewState.displayName
                    val text = t3ViewState.created + ": "
                    postBinding!!.timeCreated.text = text
                    postBinding!!.bodyText.text = t3ViewState.selftext
                    Linkify.addLinks(postBinding!!.bodyText, Linkify.WEB_URLS)
                    postBinding!!.url.text = t3ViewState.url
//TODO there is a bug here where if you click on the imageview to go to 2nd in gallery it jumps back to top
                    if (t3ViewState.isGalleryPost()) {
                          if (t3ViewState.galleryUrls!=null){
                         //     postBinding!!.fullImg.focusable= NOT_FOCUSABLE
                              postBinding!!.fullImg.visibility = VISIBLE


                         //     postBinding!!.fullImg.onFocusChangeListener =  View.OnFocusChangeListener { view, hasFocus ->
                           //       if (hasFocus) {
                             //         view.performClick()
                               //   }
                             // }



                        Timber.d("RIGHT BEFORE ERROR: pf:$this  vs:$t3ViewState")

                        Glide.with(this@PostFragment).load(t3ViewState.galleryUrls!![0])
                            .into(postBinding!!.fullImg)
                    }

                            val end = "\nGALLERY, click to to open..."
                            postBinding!!.postName.text = "${postBinding!!.postName.text}$end"

                    }

                    if (t3ViewState.isUrlPost()) {
                        loadUrlClickListener(t3ViewState)
                        postBinding!!.url.visibility= VISIBLE
                    }
                    if (t3ViewState.isImagePost())  {
                        loadImage(t3ViewState)
                        postBinding!!.fullImg.visibility = VISIBLE
                    }
                    //TODO not handling great if its both a thumb and certain kinds of reddit urls
                    //Latina Teen Short Shorts from tiktokthots_2
                    if (!t3ViewState.hasNoThumbnail()) {
                        loadThumbNail(t3ViewState)
                        postBinding!!.thumb.visibility = VISIBLE
                    }
                    //FOr now get rid of all state

                    if (t3ViewState.isVideoPost()){

                        postBinding!!.timeCreated.visibility= GONE
                        postBinding!!.postName
                        postBinding!!.bodyText.visibility=GONE
                        postBinding!!.exoplayer.visibility=VISIBLE

                        if (isSubScreen){
                            loadVideo()
                        }
                    }
                }, { Timber.e("Error in binding ${it.localizedMessage}")}).addTo(disposables )

        super.onStart()
    }
    override fun onResume() {

        Timber.d("onResume in Post Fragment $this")
        super.onResume()


        postBinding!!.fullImg.isFocusable =false

        postBinding!!.fullImg.isFocusableInTouchMode =false

        postBinding!!.nestedScroll.isFocusableInTouchMode=false

        postBinding!!.nestedScroll.isFocusable=false


        Timber.e("ISFOCUSABLE ${postBinding!!.fullImg.isFocusable}")
        Timber.e("ISFOCUSABLE ${postBinding!!.fullImg.isFocusableInTouchMode}")
//        Timber.e("GAVE ME FOCUS? ${postBinding!!.fullImg.requestFocus()}")


    }

    override fun onDestroy() {
        Timber.d("onDestroy in Post Fragment")
        super.onDestroy()

    }

    override fun onStop() {
        //moved this to ondestroyview maybe thats better
        stopVideo()
        Timber.d("onStop in Post Fragment")
        super.onStop()
        disposables.clear()
     //is this ok? can onstop and onstart in the next fragment get mixed up? should I do this in onpause?
    }

    private fun loadUrlClickListener(t3ViewState: ViewStateT3) =
        postBinding!!.url.setOnClickListener {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(t3ViewState.url))
                    startActivity(browserIntent)
        }

    private fun loadImage(t3ViewState: ViewStateT3) {
//makes work for imgur
        if (t3ViewState.url.endsWith("gifv")) {
            val url = t3ViewState.url.substring(0,t3ViewState.url.length-1)
            Glide.with(this@PostFragment).asGif().load(url).into(postBinding!!.fullImg)
        }
        else{
            Glide.with(this@PostFragment).load(t3ViewState.url)
                .into(postBinding!!.fullImg)
        }
    }

    fun loadVideo() {
        playerView?.player = null
        playerView = postBinding?.exoplayer
        playerView?.player=exo
        exo.stop()

        if (state?.let{!it.isVideoPost()} == true)  return
        val vid = MediaItem.fromUri(state?.url?: "")
     //   val vid = MediaItem.fromUri(  "https://v.redd.it/5asdaux80k5a1/DASHPlaylist.mpd?a=1673596605%2CYTEzMDcyMjA4ZGY3ZDRlY2ViZmVmMmQ5ZGEyNTllNjVkOGVmMDIwOTE5NjBhYjc5MmRiYzk0YmUyNTA2MTM4Zg%3D%3D&amp;v=1&amp;f=sd"    )
        exo.setMediaItem(vid)
        exo.repeatMode = Player.REPEAT_MODE_ALL
        Timber.e("VOLUME${exo.deviceVolume}")
        exo.playWhenReady= true
        exo.prepare()
    }

    private fun loadThumbNail(viewState: ViewStateT3)     {
        postBinding!!.thumb.visibility = VISIBLE
        if (viewState.thumbnail == "spoiler"){ postBinding!!.thumb.setImageResource(R.drawable.ic_spoiler)
                                                return}
        if (viewState.thumbnail == "nsfw") {postBinding!!.thumb.setImageResource(R.drawable.ic_nsfw)
            return
        }
        Glide.with(this).load(viewState.thumbnail.replace("&amp;", ""))
             .apply( RequestOptions().override(150, 150))
             .placeholder(ColorDrawable(Color.BLACK))
             .error(ColorDrawable(Color.RED))
             .fallback(ColorDrawable(Color.YELLOW))
             .into(postBinding!!.thumb)
    }

    fun isPlaying() = playerView?.player?.isPlaying?:false

    fun stopVideo() {
        playerView?.player?.stop()
        playerView?.player = null
    }
}

