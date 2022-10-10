package com.example.renewed

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
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


 var selected = -1
 var previousSelected :View? = null
 val stack = ArrayDeque<String>(listOf())


class SubredditsAdapter(private val onClick: (ViewStateT5) -> Unit) :
    ListAdapter<ViewStateT5, SubredditsAdapter.SubredditViewHolder>(SubredditDiffCallback) {

    fun clearSelected() {
        selected=-1
        previousSelected=null
    }




    class SubredditViewHolder(private val elementBinding: RvSubredditElemBinding) :
        RecyclerView.ViewHolder(elementBinding.root){

        fun bind(sr: ViewStateT5, fragmentContextClosure: (ViewStateT5) -> Unit,
                                                        adapterContextClosure: (Int) -> Unit,){

            elementBinding.displayName.text = sr.displayName
            elementBinding.root.setOnClickListener {
                adapterContextClosure.invoke(selected)
                selected = layoutPosition
                adapterContextClosure.invoke(selected)
                fragmentContextClosure.invoke(sr)
            }

            if (sr.displayName.length > 18) {

                elementBinding.detailImage.visibility=GONE
                return
            }
            if (sr.thumbnail.isBlank()){
                elementBinding.detailImage.visibility= VISIBLE
                  elementBinding.detailImage.setImageResource(R.color.purple_500)
                  return
            }

            elementBinding.detailImage.visibility=VISIBLE
            Glide.with(this.itemView.context).load(sr.thumbnail)
                .apply(
                    RequestOptions().override(50, 50))
                        .placeholder(ColorDrawable(Color.BLACK))
                        .error(ColorDrawable(Color.RED))
                        .fallback(ColorDrawable(Color.YELLOW))
                        .into(elementBinding.detailImage)
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubredditViewHolder {

        val elementBinding = RvSubredditElemBinding.inflate(LayoutInflater.from(parent.context),
                                                                    parent, false)
        return SubredditViewHolder(elementBinding)
    }

    override fun onViewRecycled(holder: SubredditViewHolder) {
        if (selected == holder.adapterPosition) selected = -1
        super.onViewRecycled(holder)
    }


    override fun onBindViewHolder(holder: SubredditViewHolder, position: Int) {

        val closure = { x:Int ->
            notifyItemChanged(x)
            selected = holder.adapterPosition
            notifyItemChanged(x) }

        holder.bind(getItem(position),onClick,closure)

        if (position == selected){
            previousSelected?.let{it.isSelected=false }
            holder.itemView.isSelected=true
            previousSelected = holder.itemView
        }
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
        return oldItem.timeLastAccessed == newItem.timeLastAccessed
    }
}
