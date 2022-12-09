package com.example.renewed.Screen1.Subscreen


import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.renewed.R
import com.example.renewed.Screen1.SubredditsSelectionFragment
import com.example.renewed.Screen2.FavoritesListFragment
import com.example.renewed.databinding.PostViewBinding
import com.example.renewed.models.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.Listener
import com.google.android.exoplayer2.ui.PlayerView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class PostFragment : ContentFragment() {


    @Inject
    lateinit var exo: ExoPlayer
    var playerView: PlayerView? = null
    var exoPosition: Long = 0
    private var name:String?= null
    private var isSubScreen:Boolean = false
    private val postsVM: PostVM by viewModels()
     var postBinding: PostViewBinding? = null

    var state: ViewStateT3? = null
    override fun getName() : String = postsVM.name


    override fun onSaveInstanceState(outState: Bundle) {

    //TODO am i shooting myself in the foot here by only saving instance state from fragmentadapter?
        super.onSaveInstanceState(outState)
        outState.run {
        putLong("player_pos", exo.currentPosition)

               putString("key",name)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

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

    }
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        isSubScreen = arguments?.getBoolean("isSubscreen")?: false


        name = arguments?.getString("key") ?: "NONE"
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

                    if (t3ViewState.isGalleryPost()){
                        postBinding!!.fullImg.setOnClickListener(object : View.OnClickListener{
                                private var dex: Int = 1
                              override fun onClick(v: View?) {
                                    Glide.with(this@PostFragment).load(
                                        t3ViewState.galleryUrls!![dex % t3ViewState.galleryUrls.size])
                                        .into(postBinding!!.fullImg)
                                    dex += 1
                                }})

                            Glide.with(this@PostFragment).load(t3ViewState.galleryUrls!![0])
                                .into(postBinding!!.fullImg)
                            postBinding!!.fullImg.visibility = VISIBLE
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
                    if (!t3ViewState.hasNoThumbnail()) {
                        loadThumbNail(t3ViewState)
                        postBinding!!.thumb.visibility = VISIBLE
                    }
                    //FOr now get rid of all state
                    if (t3ViewState.isVideoPost()){
                        //todo not working on first
                        postBinding!!.postName.setTextColor(Color.parseColor("yellow"))
                        view?.setBackgroundColor(Color.parseColor("black"))
                        postBinding!!.timeCreated.visibility= GONE
                        postBinding!!.bodyText.visibility=GONE
                        postBinding!!.exoplayer.visibility=VISIBLE

                        //TODO how to have vids on main screen
                        if (isSubScreen){
                            loadVideo()
                        }
            //               loadVideo()

     }
                    //should I also do title or just make it neon?
                }, { Timber.e("Error in binding ${it.localizedMessage}")})

    }

    override fun onPause() {
        Timber.d("onPause in Post Fragment")
        super.onPause()
//        stopVideo()
   //     exo.pause()


       // postBinding= null
   //     playerView?.player=null

     //   exo.clearMediaItems()



    }

    override fun onResume() {
        Timber.d("onResume in Post Fragment")
        super.onResume()
        //exo.playWhenReady=true

    }

    override fun onDestroy() {

        Timber.d("onDestroy in Post Fragment")
        super.onDestroy()

    }

    override fun onStop() {
        stopVideo()
        Timber.d("onStop in Post Fragment")
        super.onStop()
     //is this ok? can onstop and onstart in the next fragment get mixed up? should I do this in onpause?


    }

    private fun loadUrlClickListener(t3ViewState: ViewStateT3) =
        postBinding!!.url.setOnClickListener {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(t3ViewState.url))
                    startActivity(browserIntent)
        }

    private fun loadImage(t3ViewState: ViewStateT3) {

        //TODO this is where the error is triggered on the rotate
        Glide.with(this@PostFragment).load(t3ViewState.url)
            .into(postBinding!!.fullImg)
    }

    fun loadVideo() {

        if (state?.let{!it.isVideoPost()} == true)  return

        playerView = postBinding?.exoplayer
        playerView?.player=exo

        val vid = MediaItem.fromUri(state?.url?: "")
        exo.setMediaItem(vid)
        playerView?.useController = false

        exo.repeatMode = Player.REPEAT_MODE_ALL
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


    //    exo.playWhenReady       =false
        playerView?.player?.stop()



    }
}

