package com.example.renewed.Screen2

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.renewed.Screen1.PostsAdapter
import com.example.renewed.Screen1.Subscreen.BlankFragment
import com.example.renewed.Screen1.Subscreen.PostFragment
import com.example.renewed.VIEWPAGER_PAGES_TOTAL
import com.example.renewed.models.PartialViewState
import com.example.renewed.models.ViewStateT3
import com.example.renewed.models.isVideoPost
import com.google.android.exoplayer2.Player
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

class FavoritesListAdapter(private val fragment: FavoritesListFragment): FragmentStateAdapter(fragment) {
    var postIds: MutableList<String> = mutableListOf<String>()
    var fragList: MutableList<PostFragment?> = arrayOfNulls<PostFragment>(VIEWPAGER_PAGES_TOTAL).toMutableList()
    var a = arrayOfNulls<PostFragment>(VIEWPAGER_PAGES_TOTAL).toMutableList()

    override fun getItemCount(): Int = postIds.size
    override fun getItemId(position: Int): Long = postIds[position].hashCode().toLong()
    override fun containsItem(itemId: Long): Boolean = postIds.any { it.hashCode().toLong() == itemId }

    fun replaceList(idList:List<String>){
        postIds.clear()
        postIds.addAll(idList)
        notifyDataSetChanged()
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
}