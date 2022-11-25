package com.example.renewed.Screen1.Subscreen


import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.example.renewed.databinding.PostViewBinding
import com.example.renewed.models.ViewStateT3
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
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


    private val postsVM: PostVM by viewModels()
     var postBinding: PostViewBinding? = null
    private var name:String?= null
    override fun getName() : String = postsVM.name


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong("player_pos", exo.contentPosition)


    //TODO am i shooting myself in the foot here by only saving instance state from fragmentadapter?
     //   super.onSaveInstanceState(outState)
       // outState.run {

    //   putString("key",name)
    //}
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
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        name = arguments?.getString("key") ?: "NONE"
        postsVM.setPost(name!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { t3ViewState ->
                    postBinding!!.postName.text = t3ViewState.displayName
                    val text = t3ViewState.created + ": "
                    postBinding!!.timeCreated.text = text
                    postBinding!!.bodyText.text = t3ViewState.selftext
                    Linkify.addLinks(postBinding!!.bodyText, Linkify.WEB_URLS)
                    postBinding!!.url.text = t3ViewState.url


                    if (isGalleryPost(t3ViewState)){
                        postBinding!!.fullImg.setOnClickListener {
                            Timber.d("AAAAAAAAAAAAAAAAAAAAAAAAAA")
                            Glide.with(this@PostFragment)
                                .load(
                                    t3ViewState.galleryUrls!![(0 until t3ViewState.galleryUrls.size)
                                        .shuffled().first()]
                                )
                                .into(postBinding!!.fullImg)
                            Glide.with(this).load(t3ViewState.galleryUrls!![0])
                                .into(postBinding!!.fullImg)
                            postBinding!!.fullImg.visibility = VISIBLE
                        }
                //        postBinding!!.fullImg.setOnClickListener {   class GalleryClick : View.OnClickListener {
                  //              override fun onClick(v: View) {
                    //                Timber.d("AAAAAAAAAAAAAAAAAAAAAAAAAA")
                      //          }}}
                                /**  var index:Int
                                init{index= 0}
                                override fun onClick(v: View) {

                                Glide.with(this@PostFragment).load(t3ViewState
                                .galleryUrls!![0.rangeTo(t3ViewState.galleryUrls.size).shuffled().first()])
                                .into(postBinding!!.fullImg)
                                //    index= index+1
                                Timber.d("AAAAAAAAAAAAAAAAAAAAAAAAAA$index")
                                }
                                }
                                var index = 0;**/
                         //   }

                        //TODO this is where the error is triggered on the rotate
                  //      TImber.d("this is")
                        Glide.with(this).load(t3ViewState.galleryUrls!![1])
                            .into(postBinding!!.fullImg)
                                postBinding!!.fullImg.visibility = VISIBLE

                    }
                    if (isUrlPost(t3ViewState)) {
                        loadUrlClickListener(t3ViewState)
                        postBinding!!.url.visibility= VISIBLE
                    }
                    if (isImagePost(t3ViewState))  {
                        loadImage(t3ViewState)
                        postBinding!!.fullImg.visibility = VISIBLE
                    }
                    if (!hasNoThumbnail(t3ViewState)) {
                        loadThumbNail(t3ViewState)
                        postBinding!!.thumb.visibility = VISIBLE
                    }
                    //FOr now get rid of all state
                    if (isVideoPost(t3ViewState)){
                        postBinding!!.timeCreated.visibility= GONE
                        postBinding!!.bodyText.visibility=GONE
                        postBinding!!.exoplayer.visibility=VISIBLE
                        loadVideo(t3ViewState)
     }
                    //should I also do title or just make it neon?
                }, { Timber.e("Error in binding ${it.localizedMessage}")})

    }

    override fun onPause() {
        Timber.d("onPause in Post Fragment")
        super.onPause()
    }

    override fun onResume() {
        Timber.d("onResume in Post Fragment")
        super.onResume()
    }

    override fun onDestroy() {
        Timber.d("onDestroy in Post Fragment")
        super.onDestroy()
    }

    override fun onStop() {
     //is this ok? can onstop and onstart in the next fragment get mixed up? should I do this in onpause?
        exo.pause()
        super.onStop()
    }


    private fun hasNoThumbnail(t3ViewState: ViewStateT3) =
        t3ViewState.thumbnail.isBlank() || t3ViewState.thumbnail == "self" ||
                t3ViewState.thumbnail == "default"  || isImagePost(t3ViewState)
                || isVideoPost(t3ViewState) || t3ViewState.thumbnail == "spoiler" //|| thumbnail == nsfw

    private fun loadUrlClickListener(t3ViewState: ViewStateT3) =
        postBinding!!.url.setOnClickListener {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(t3ViewState.url))
                    startActivity(browserIntent)
        }

    private fun loadImage(t3ViewState: ViewStateT3) {

        //TODO this is where the error is triggered on the rotate
        Glide.with(this).load(t3ViewState.url)
            .into(postBinding!!.fullImg)
    }

    private fun loadVideo(t3ViewState: ViewStateT3) {
        playerView = postBinding!!.exoplayer
        playerView?.player=exo
        val vid = MediaItem.fromUri(t3ViewState.url)
        exo.setMediaItem(vid)
        exo.repeatMode = Player.REPEAT_MODE_ALL
        playerView?.useController = false

        exo.playWhenReady       =true
     //TODO not working
        exo.seekTo(exoPosition)


       exo.prepare()

    }

//TODO this is a mess I have to unselect all the image posts then check if image post in image one
    private fun isUrlPost(t3ViewState: ViewStateT3):Boolean =
        t3ViewState.url.startsWith("http")// && "com" in x.t3.url
                //todo this is better but doesnt capture the text posts that no need url
             //   && !isImagePost(t3ViewState) && !isVideoPost(t3ViewState)
                && ("reddit" !in t3ViewState.url  && "redd.it" !in t3ViewState.url
                                                     && "imgur" !in t3ViewState.url)
   //                 || ("reddit" in t3ViewState.url) && ("gallery" in t3ViewState.url)


    private fun isGalleryPost(t3ViewState: ViewStateT3):Boolean =
        ("reddit" in t3ViewState.url) && ("gallery" in t3ViewState.url)

    private fun isImagePost(t3ViewState: ViewStateT3):Boolean =
                            "i.redd.it" in t3ViewState.url || "imgur" in t3ViewState.url

    private fun isVideoPost(t3ViewState: ViewStateT3):Boolean =
                "v.redd.it" in t3ViewState.url


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
}

