package com.example.renewed

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.renewed.databinding.RvSubredditElemBinding
import com.example.renewed.models.ViewStateT5
import timber.log.Timber
//because these are used across config changes, they must be global
private var selected = -1
private var lastSelected = -1

class SubredditsAdapter(private val onClick: (ViewStateT5) -> Unit) :
        ListAdapter<ViewStateT5, SubredditsAdapter.SubredditViewHolder>(SubredditDiffCallback) {

    var previousSelected :RecyclerView.ViewHolder? = null

    fun clearSelected() {
        previousSelected?.bindingAdapter?.notifyItemChanged(selected)
        selected=-1
    //    previousSelected = null is this line good or bad
    }

    fun setSelected() {
        selected = lastSelected
        previousSelected?.bindingAdapter?.notifyItemChanged(selected)

    }

    inner class SubredditViewHolder(private val elementBinding: RvSubredditElemBinding) :
        RecyclerView.ViewHolder(elementBinding.root){

        fun bind(sr: ViewStateT5, fragmentContextClosure: (ViewStateT5) -> Unit){

            elementBinding.displayName.text = sr.displayName.chunked(18).joinToString("\n")

            elementBinding.root.setOnClickListener { lastSelected = selected
                                                    selected = layoutPosition
                                                     bindingAdapter?.notifyItemChanged(selected)
                                                     fragmentContextClosure.invoke(sr)
                                                    }

            Glide.with(this.itemView.context)
                .load(sr.thumbnail)
                .apply(
                        RequestOptions().override(50, 50))
                                        .placeholder(ColorDrawable(Color.BLACK))
                                        .error(ColorDrawable(Color.BLUE))
                                        .into(elementBinding.detailImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubredditViewHolder {

        val elementBinding = RvSubredditElemBinding.inflate(LayoutInflater.from(parent.context),
                                                                      parent, false)
        return SubredditViewHolder(elementBinding)
    }

    override fun onBindViewHolder(holder: SubredditViewHolder, position: Int) {
        Timber.d("onBindViewHolder called")
        holder.bind(getItem(position), onClick)
        //This branch will set the selected subreddit and highlight it when the screen reloads.
        if (position == selected){
            previousSelected?.let{it.itemView.isSelected =false }
            holder.itemView.isSelected=true
            previousSelected = holder
        }
        //This branch will set the rest of the subreddits to not highlighted when screen is loaded
        else{
            holder.itemView.isSelected=false
        }
    }
}

object SubredditDiffCallback : DiffUtil.ItemCallback<ViewStateT5>() {
    override fun areItemsTheSame(oldItem: ViewStateT5, newItem: ViewStateT5): Boolean {
        return oldItem.name == newItem.name
    }
    override fun areContentsTheSame(oldItem: ViewStateT5, newItem: ViewStateT5): Boolean {
        return oldItem == newItem
    }
}
