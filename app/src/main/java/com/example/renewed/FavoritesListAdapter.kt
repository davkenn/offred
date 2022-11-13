package com.example.renewed

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class FavoritesListAdapter(private val fragment:FavoritesListFragment): FragmentStateAdapter(fragment){
    override fun getItemCount(): Int = 22
   // override fun getItemId(position: Int): Long = items.itemId(position)
    //override fun containsItem(itemId: Long): Boolean = items.contains(itemId)


    override fun createFragment(position: Int): Fragment {
        val fragment= PostFragment()
        fragment.arguments = Bundle().apply{putString("key","t3_y69uop")
                                            putString("pos",position.toString())}
        return fragment
    }
}