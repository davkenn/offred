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

class FavoritesListAdapter(private val fragment: FavoritesListFragment): FragmentStateAdapter(fragment) {
    var postIds: MutableList<String> = mutableListOf<String>()
    var fragList: MutableList<PostFragment> = mutableListOf<PostFragment>()
    override fun getItemCount(): Int = postIds.size
 //    override fun getItemId(position: Int): Long = items.itemId(position)
    //override fun containsItem(itemId: Long): Boolean = items.contains(itemId)


    //YAY
    fun replaceList(idList:List<String>){
    //    fragList.clear()
        postIds.clear()
        postIds.addAll(idList)
      //  fragList.clear()
        notifyDataSetChanged()

    }

    fun stopVideoAtPosition(position: Int): Boolean {
        if (position <0|| position >=  fragList.size) return false
        if (fragList[position].state?.isVideoPost() == false) return false
        if (fragList[position].isPlaying()){
            fragList[position].stopVideo()
            return true}
            return false
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
     //       putInt("pos",position)
            //TODO do i need to readd pos
            //  putString("pos",position.toString())

        }
        fragList.add(position,fragment)
        return fragment

    }

    fun startVideoAtPosition(position: Int) {
        if (position <0|| position >=  fragList.size) return
        fragList[position].loadVideo()
    }

}