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
import com.example.renewed.models.PartialViewState
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber

@AndroidEntryPoint
class PostFragment : ContentFragment() {

    private val postsVM: PostVM by viewModels()
     var postBinding: PostViewBinding? = null
    private var name:String?= null
    override fun getName() : String = postsVM.name

    //TODO am i shooting myself in the foot here by only saving instance state from fragmentadapter?
   // override fun onSaveInstanceState(outState: Bundle) {

     //   super.onSaveInstanceState(outState)
       // outState.run {

         //   putString("key",name)
        //}
    //}
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
   //     postBinding = null
        super.onDestroyView()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        name = arguments?.getString("key") ?: "NONE"
        postsVM.setPost(name!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( { t3ViewState -> postBinding!!.fullImg.visibility= GONE
                postBinding!!.postName.text = t3ViewState.t3.displayName
                val text = t3ViewState.t3.created + ": "
                postBinding!!.timeCreated.text = text
                postBinding!!.bodyText.text = t3ViewState.t3.selftext
                Linkify.addLinks(postBinding!!.bodyText, Linkify.WEB_URLS)
                postBinding!!.url.text = t3ViewState.t3.url
                if (isUrlPost(t3ViewState)) {
                    loadUrlClickListener(t3ViewState)
                }
                else {
                    postBinding!!.url.visibility = GONE
                }
                if (isImagePost(t3ViewState))  loadImage(t3ViewState)
                //could it also be a text post that this is signalling?
                if (hasNoThumbnail(t3ViewState)) {
                    postBinding!!.thumb.visibility = GONE
                }  else loadThumbNail(t3ViewState)
            },{ Timber.e("Error in binding ${it.localizedMessage}")})
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    private fun hasNoThumbnail(t3ViewState: PartialViewState.T3ForViewing) =
        t3ViewState.t3.thumbnail.isBlank() || t3ViewState.t3.thumbnail == "self" ||
                t3ViewState.t3.thumbnail == "default"  || isImagePost(t3ViewState)
          //TODO I also need to implement this spoiler somewhere
                || t3ViewState.t3.thumbnail == "spoiler" //|| thumbnail == nsfw
    private fun loadUrlClickListener(t3ViewState: PartialViewState.T3ForViewing) =
        postBinding!!.url.setOnClickListener {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(t3ViewState.t3.url))
                    startActivity(browserIntent)
        }

    private fun loadImage(t3ViewState: PartialViewState.T3ForViewing) {
        postBinding!!.thumb.visibility = GONE
        postBinding!!.fullImg.visibility = VISIBLE
        //TODO this is where the error is triggered on the rotate
        Glide.with(this).load(t3ViewState.t3.url)
            .into(postBinding!!.fullImg)
    }

    private fun isUrlPost(t3ViewState: PartialViewState.T3ForViewing):Boolean =
        t3ViewState.t3.url.startsWith("http")// && "com" in x.t3.url
                && (("reddit" !in t3ViewState.t3.url  && "redd.it" !in t3ViewState.t3.url) ||
        (("reddit" in t3ViewState.t3.url) && ("gallery" in t3ViewState.t3.url)))

    private fun isImagePost(t3ViewState: PartialViewState.T3ForViewing):Boolean =
                                                            "i.redd.it" in t3ViewState.t3.url

    private fun loadThumbNail(viewState: PartialViewState.T3ForViewing)     {
        postBinding!!.thumb.visibility = VISIBLE
        if (viewState.t3.thumbnail == "spoiler") postBinding!!.thumb.setImageResource(R.drawable.ic_spoiler)
        if (viewState.t3.thumbnail == "nsfw") postBinding!!.thumb.setImageResource(R.drawable.ic_nsfw)
        Glide.with(this).load(viewState.t3.thumbnail)
            .apply( RequestOptions().override(100, 100))
            .placeholder(ColorDrawable(Color.BLACK))
            .error(ColorDrawable(Color.RED))
            .fallback(ColorDrawable(Color.YELLOW))
            .into(postBinding!!.thumb)
    }
}

