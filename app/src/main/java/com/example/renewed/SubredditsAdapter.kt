package com.example.renewed

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import com.example.renewed.models.ViewStateT5
import com.example.renewed.databinding.RvSubredditElemBinding


//https://stackoverflow.com/questions/60423596/how-to-use-viewbinding-in-a-recyclerview-adapter
class SubredditsAdapter(private val onClick: (ViewStateT5) -> Unit) :
//    RecyclerView.Adapter<ViewStateT5, SubredditsAdapter.SubredditViewHolder>(SubredditDiffCallback) {
    ListAdapter<ViewStateT5, SubredditsAdapter.SubredditViewHolder>(SubredditDiffCallback) {

    class SubredditViewHolder(private val elementBinding: RvSubredditElemBinding) :
        RecyclerView.ViewHolder(elementBinding.root) {

        fun bind(sr: ViewStateT5, onClickFunction: (ViewStateT5) -> Unit){


            elementBinding.displayName.text = sr.displayName
            elementBinding.root.setOnClickListener {



                onClickFunction.invoke(sr); //elementBinding.root.setBackgroundColor(Color.BLUE)



            }

            if (sr.displayName.length > 18) {
            //TODO now that ive taken out center crop fixed some bad looks but messed up the ones that are too small

                elementBinding.detailImage.visibility=GONE
                return
            }
            if (sr.thumbnail.isNullOrBlank()){
                elementBinding.detailImage.visibility= VISIBLE
                  elementBinding.detailImage.setImageResource(R.color.purple_500)
                  return
            }

            elementBinding.detailImage.visibility=VISIBLE
            Glide.with(this.itemView.context).load(sr.thumbnail)
                .apply(
                    RequestOptions().override(50, 50))
                   //     .centerCrop()
                        .placeholder(ColorDrawable(Color.BLACK))
                        .error(ColorDrawable(Color.RED))
                  .fallback(ColorDrawable(Color.YELLOW))
                        .into(elementBinding.detailImage)

            }


        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubredditViewHolder {
        val elementBinding = RvSubredditElemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubredditViewHolder(elementBinding)
    }

    override fun onBindViewHolder(holder: SubredditViewHolder, position: Int) {

        holder.bind(getItem(position),onClick)


        //TODO this is a dead end
//        if (position == 0) holder.itemView.performClick()


    }

}

object SubredditDiffCallback : DiffUtil.ItemCallback<ViewStateT5>() {
    override fun areItemsTheSame(oldItem: ViewStateT5, newItem: ViewStateT5): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: ViewStateT5, newItem: ViewStateT5): Boolean {
        //TODO what to do here
      //  return oldItem.timeLastAccessed == newItem.timeLastAccessed
        return oldItem.subscribers == newItem.subscribers




















    }
}
