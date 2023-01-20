package com.example.renewed.Screen2

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.renewed.Screen1.PostsAdapter
import com.example.renewed.Screen1.Subscreen.BlankFragment
import com.example.renewed.Screen1.Subscreen.PostFragment
import com.example.renewed.models.PartialViewState
import com.example.renewed.models.ViewStateT3
import com.example.renewed.models.isVideoPost
import com.google.android.exoplayer2.Player
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

class FavoritesListAdapter(private val fragment: FavoritesListFragment): FragmentStateAdapter(fragment) {
    var postIds: MutableList<String> = mutableListOf<String>()
    var fragList: MutableList<PostFragment?> = arrayOfNulls<PostFragment>(12).toMutableList()
    var a = arrayOfNulls<PostFragment>(12).toMutableList()

    override fun getItemCount(): Int = postIds.size
    override fun getItemId(position: Int): Long = postIds[position].hashCode().toLong()
    override fun containsItem(itemId: Long): Boolean = postIds.any { it.hashCode().toLong() == itemId }

    fun replaceList(idList:List<String>){
        postIds.clear()
        postIds.addAll(idList)
        notifyDataSetChanged()
    }

    fun removeFirst(){
        Timber.d("WELLSBEFORE $postIds")
        Timber.d("WELLSBEFORE2 ${fragList.map{it?.t3Name}}")

 //IMPORTANT DON'T SWAP THE ORDER OF THESE TWO I FIXED THE MIDLIST NULL BUG BY
        //SWAPPING THE ORDER TO THIS
        var copy2 = fragList.toMutableList()
        copy2.removeAt(0)
        copy2.add(null)


        fragList=copy2

        var copy = postIds.toMutableList()
        copy.removeAt(0)
        replaceList(copy)


        Timber.d("WELLSAFTER $postIds")
        Timber.d("WELLSAFTER2  ${fragList.map{it?.t3Name}}")
    }

    override fun createFragment(position: Int): Fragment {
        val name = postIds[position]
        val fragment = PostFragment()
        fragment.arguments = Bundle().apply {
            putString("key", name)
        }
        fragList[position] = fragment
        return fragment

    }
    fun addFragment(t3: ViewStateT3){
        var copy = postIds.toMutableList()
        t3.name?.let { copy.add(it) }

        replaceList(copy)
        notifyDataSetChanged()

    }
    fun startVideoAtPosition(position: Int) {
        if (position <0|| position >=  fragList.size) return
      //  fragList[position]?.loadVideo()
    }
}