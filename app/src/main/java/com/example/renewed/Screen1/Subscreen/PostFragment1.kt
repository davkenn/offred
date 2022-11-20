package com.example.renewed.Screen1.Subscreen


import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
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

@AndroidEntryPoint
class PostFragment1 : ContentFragment() {
    private var page:String?=null
    private val postsVM: PostVM by viewModels()
    private var postBinding: PostViewBinding? = null

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
        postBinding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val name = arguments?.getString("key") ?: "NONE"
        page = arguments?.getString("pos")?: "NO PAGE"

        postsVM.setPost(name)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { t3ViewState -> //postBinding!!.fullImg.visibility= GONE
                postBinding!!.postName.text = t3ViewState.t3.displayName
                val text = t3ViewState.t3.created + ": "
                postBinding!!.timeCreated.text = text
                postBinding!!.bodyText.text = t3ViewState.t3.selftext
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
            }
    }

    private fun hasNoThumbnail(t3ViewState: PartialViewState.T3ForViewing) =
        t3ViewState.t3.thumbnail.isBlank() || t3ViewState.t3.thumbnail == "self"
                || isImagePost(t3ViewState)

    private fun loadUrlClickListener(t3ViewState: PartialViewState.T3ForViewing) =
        postBinding!!.url.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(t3ViewState.t3.url))
            startActivity(browserIntent)
        }

    private fun loadImage(t3ViewState: PartialViewState.T3ForViewing) {
        postBinding!!.thumb.visibility = GONE
        postBinding!!.fullImg.visibility = VISIBLE
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
