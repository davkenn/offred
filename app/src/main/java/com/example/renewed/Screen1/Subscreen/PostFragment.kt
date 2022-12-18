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
    private val postsVM: PostVM by viewModels()
    private var name:String?= null
    private var isSubScreen:Boolean = false
    var postBinding: PostViewBinding? = null
    var state: ViewStateT3? = null
    private val disposables = CompositeDisposable()
    override fun getName() : String = postsVM.name



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = PostViewBinding.inflate(inflater,container,false)
        postBinding = binding


        return binding.root
    }

    override fun onDestroyView() {
        //TODO need to not do this if I don't want crashes
        postBinding = null
        super.onDestroyView()
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.parseColor("black"))
        isSubScreen = arguments?.getBoolean("isSubscreen")?: false


        name = arguments?.getString("key") ?: "NONE"




}

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

                        //makes gallery image clickable but still focusable on other post types
                        postBinding!!.fullImg.isFocusable =false
                        postBinding!!.fullImg.isFocusableInTouchMode =false

                        postBinding!!.fullImg
                            .setOnClickListener(object : View.OnClickListener {
                                var dex: Int = 0
                                override fun onClick(v: View?) {
                                    dex += 1

                                    Timber.e("ONCLICK CALLED")
                                    Glide.with(this@PostFragment).load(
                                        state?.galleryUrls?.get(dex % state?.galleryUrls!!.size)
                                    )
                                        .into(postBinding!!.fullImg)

                                }
                            })
                          if (t3ViewState.galleryUrls!=null){
                         //     postBinding!!.fullImg.focusable= NOT_FOCUSABLE
                              postBinding!!.fullImg.visibility = VISIBLE


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
        if (t3ViewState.url.endsWith("gifv")) {     //makes work for imgur
            val url = t3ViewState.url.substring(0,t3ViewState.url.length-1)
            Glide.with(this@PostFragment).asGif().load(url)

                .into(postBinding!!.fullImg)
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
        if (state?.let{!it.isVideoPost()} == true)  { return}
        val vid = MediaItem.fromUri(state?.url?: "")
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
            .placeholder(R.drawable.ic_loading)
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

