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
    var t3Name:String?= null
    var postBinding: PostViewBinding? = null
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

    override fun onCreate(savedInstanceState: Bundle?) {
    //    retainInstance=true
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        //TODO need to not do this if I don't want crashes
        postBinding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.parseColor("black"))
        t3Name = arguments?.getString("key") ?: "NONE"
    }

    override fun onPause() {
        stopVideo()
        Timber.d("onPause in Post Fragment")
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        Timber.d("onResume in Post Fragment ${this.t3Name}")
        super.onResume()
        stopVideo()
        postsVM.setPost(t3Name!!)
               .subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe({ t3ViewState -> postBinding!!.postName.text = t3ViewState.displayName
                                           val text = t3ViewState.created + ": "
                                           postBinding!!.timeCreated.text = text
                                           postBinding!!.bodyText.text = t3ViewState.selftext
                                           Linkify.addLinks(postBinding!!.bodyText, Linkify.WEB_URLS)
                                           postBinding!!.url.text = t3ViewState.url
                        if (t3ViewState.isGalleryPost()) {

                            //makes gallery image clickable but still focusable on other post types
                                           postBinding!!.fullImg.isFocusable =false
                                           postBinding!!.fullImg.isFocusableInTouchMode =false
                                           postBinding!!.fullImg.setOnClickListener {
                                               this@PostFragment.postsVM.pos+=1
                                               Glide.with(this@PostFragment).load(
                                                                    t3ViewState.galleryUrls
                                                         ?.get(this@PostFragment.postsVM.pos % t3ViewState.galleryUrls.size)
                                                        ).into(postBinding!!.fullImg)
                            }
                        if (t3ViewState.galleryUrls!=null){
                            postBinding!!.fullImg.visibility = VISIBLE
                            Glide.with(this@PostFragment).load(t3ViewState.galleryUrls[postsVM.pos% t3ViewState.galleryUrls.size])
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
                    if (!t3ViewState.hasNoThumbnail()) {
                        loadThumbNail(t3ViewState)
                        postBinding!!.thumb.visibility = VISIBLE
                    }
                    if (t3ViewState.isVideoPost()){

                        postBinding!!.timeCreated.visibility= GONE
                        postBinding!!.bodyText.visibility=GONE
                        postBinding!!.exoplayer.visibility=VISIBLE
                        loadVideo(t3ViewState)
                    }
                }, { Timber.e("Error in binding ${it.localizedMessage}")}).addTo(disposables )
    }

    override fun onDestroy() {
        Timber.d("onDestroy in Post Fragment")
        super.onDestroy()
    }

    override fun onStop() {
        //moved this to ondestroyview maybe thats better
        Timber.d("onStop in Post Fragment")
        super.onStop()
        disposables.clear()
     //is this ok? can onstop and onstart in the next fragment get mixed up? should I do this in onpause?
    }

    private fun loadUrlClickListener(t3ViewState: ViewStateT3) =
        postBinding!!.url.setOnClickListener {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(t3ViewState.url))
                    startActivity(browserIntent) }

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

    fun loadVideo(state:ViewStateT3?) {
        playerView?.player = null
        playerView = postBinding?.exoplayer
        playerView?.player=exo
        exo.stop()
        if (state?.let{!it.isVideoPost()} == true)  { return}
        exo.repeatMode = Player.REPEAT_MODE_ALL
        Timber.e("VOLUME${exo.deviceVolume}")
        exo.playWhenReady= true
        val vid = MediaItem.fromUri(state?.url?: "")
        exo.setMediaItem(vid)
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

    fun stopVideo() {
        playerView?.player?.stop()
        playerView?.player = null
    }
}

