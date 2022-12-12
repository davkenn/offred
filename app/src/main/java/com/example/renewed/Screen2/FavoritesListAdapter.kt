package com.example.renewed.Screen2

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.renewed.PostsAdapter
import com.example.renewed.Screen1.Subscreen.BlankFragment
import com.example.renewed.Screen1.Subscreen.PostFragment
import com.example.renewed.models.isVideoPost
import com.google.android.exoplayer2.Player
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

class FavoritesListAdapter(private val fragment: FavoritesListFragment): FragmentStateAdapter(fragment) {
    var postIds: MutableList<String> = mutableListOf<String>()
    var fragList: MutableList<PostFragment> = mutableListOf<PostFragment>()
    override fun getItemCount(): Int = postIds.size
    override fun getItemId(position: Int): Long = postIds[position].hashCode().toLong()
    override fun containsItem(itemId: Long): Boolean = postIds.any { it.hashCode().toLong() == itemId }



    fun replaceList(idList:List<String>){
    //    fragList.clear()
        postIds.clear()
        postIds.addAll(idList)
      //  fragList.clear()
        notifyDataSetChanged()

    }

    fun removeFirst(){

        Timber.d("WELLSBEFORE $postIds")
        Timber.d("WELLSBEFORE2 ${fragList.map{it.state?.name}}")
        var copy = postIds.toMutableList()
        copy.removeAt(0)
        replaceList(copy)
        var copy2 = fragList.toMutableList()
        copy2.removeAt(0)
        fragList=copy2
        notifyDataSetChanged()

        Timber.d("WELLSAFTER $postIds")
        Timber.d("WELLSAFTER2  ${fragList.map{it.state?.name}}")
        notifyItemRemoved(0)

    }



    //TODO BUGSS
    //on the last fragment when you rotate it doesn't reload the video, maybe the position im using is incremented?
    //after the first few fragments rotate doesn't work on a video, related to the pages initially loaded in vp?
    //related to previous: it seems to start the first video and then come to the current and not load it
    override fun createFragment(position: Int): Fragment {
        val name = postIds[position]
        val fragment = PostFragment()
        fragment.arguments = Bundle().apply {
            putString("key", name)
            putBoolean("isSubscreen",false)
        }
        fragList.add(position,fragment)
        return fragment

    }
    fun startVideoAtPosition(position: Int) {
        //from stopvideoatposition do i need isplaying
        //        if (fragList[position].isPlaying()){
        //            fragList[position].stopVideo()
        if (position <0|| position >=  fragList.size) return
        fragList[position].loadVideo()
    }

}