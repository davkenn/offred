package com.example.renewed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.renewed.databinding.RvPostElemBinding

import com.example.renewed.models.ViewStateT3


class PostsAdapter(private val onClick: (ViewStateT3) -> Unit) :
    ListAdapter<ViewStateT3, PostsAdapter.PostViewHolder>(PostDiffCallback) {

    class PostViewHolder(private val postsBinding: RvPostElemBinding) :
        RecyclerView.ViewHolder(postsBinding.root) {

        fun bind(sr: ViewStateT3, onClickFunction: (ViewStateT3) -> Unit) {
            postsBinding.postTitle.text =sr.displayName
            //TODO change this and the other to the virewgroup
            postsBinding.postTitle.setOnClickListener { onClickFunction.invoke(sr) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val postBinding = RvPostElemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(postBinding)

    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position),onClick )

    }
}

object PostDiffCallback : DiffUtil.ItemCallback<ViewStateT3>() {
    override fun areItemsTheSame(oldItem: ViewStateT3, newItem: ViewStateT3): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: ViewStateT3, newItem: ViewStateT3): Boolean {
        //TODO what to do here
        //  return oldItem.timeLastAccessed == newItem.timeLastAccessed
        return oldItem.displayName == newItem.displayName
    }
    }
