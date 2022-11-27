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

    fun replaceList(idList:List<String>){
        postIds.clear()
        postIds.addAll(idList)
        notifyDataSetChanged()
    }

    fun stopVideoAtPosition(position: Int): Boolean {
        if (position <0) return false
        if (fragList[position].state?.isVideoPost()==null ||
            fragList[position].state?.isVideoPost()==false ) return false
        else{
            fragList[position].stopVideo()
            return true
        }

    }

    override fun createFragment(position: Int): Fragment {
        val name = postIds[position]
        val fragment = PostFragment()
        fragment.arguments = Bundle().apply {

            putString("key", name)
     //       putInt("pos",position)
            //TODO do i need to readd pos
            //  putString("pos",position.toString())

        }
        fragList.add(position,fragment)
        return fragment
        //   val fragment = BlankFragment()
          //  return fragment
    }

    fun startVideoAtPosition(position: Int) {

        fragList[position].loadVideo()
    }

}