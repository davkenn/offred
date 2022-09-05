package com.example.renewed

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.renewed.databinding.PostViewBinding


import dagger.Binds
import dagger.Module
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Singleton


@AndroidEntryPoint
class PostFragment : Fragment() {
    private val postsVM: PostVM by viewModels()
    private var postBinding: PostViewBinding? = null

    fun getName() : String = postsVM.name

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        postsVM.setPost(name)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe { x ->
                postBinding!!.fullImg.visibility= GONE
                postBinding!!.postName.text = x.t3.displayName
                postBinding!!.timeCreated.text = x.t3.created + ": "
                postBinding!!.bodyText.text = x.t3.selftext
                postBinding!!.url.text = x.t3.url

                if (x.t3.thumbnail.isNullOrBlank() || x.t3.thumbnail == "self")
                    postBinding!!.thumb.visibility = GONE
                else if (("i.redd.it" in x.t3.url)){
                 postBinding!!.thumb.visibility = GONE
                    postBinding!!.fullImg.visibility=VISIBLE
                    Glide.with(this).load(x.t3.url)
                        .into(postBinding!!.fullImg)
                }
                else{
                    postBinding!!.thumb.visibility = VISIBLE
                    if (x.t3.thumbnail.equals("spoiler")) //rpg_gamers Expeditions
                        postBinding!!.thumb.setImageResource(R.drawable.ic_spoiler)
                    if (x.t3.thumbnail.equals("nsfw"))
                        postBinding!!.thumb.setImageResource(R.drawable.ic_nsfw)
                    Glide.with(this).load(x.t3.thumbnail)
                        .apply( RequestOptions().override(100, 100))
                        .placeholder(ColorDrawable(Color.BLACK))
                        .error(ColorDrawable(Color.RED))
                        .fallback(ColorDrawable(Color.YELLOW))
                        .into(postBinding!!.thumb)
                    }
                    if ((x.t3.url.startsWith("http")// && "com" in x.t3.url
                                && "reddit" !in x.t3.url  && "redd.it" !in x.t3.url) ||
                                   "reddit" in x.t3.url && "gallery" in x.t3.url ){
                                postBinding!!.url.setOnClickListener {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(x.t3.url))
                                    startActivity(browserIntent)}
                    }
            }
        }
}
