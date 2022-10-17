package com.example.renewed

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class FavoritesListAdapter(fragment:Fragment): FragmentStateAdapter(fragment){
    override fun getItemCount(): Int = 22


    override fun createFragment(position: Int): Fragment {
        val fragment= PostFragment()
        fragment.arguments = Bundle().apply{putString("key","t3_y2e2am")
                                            putString("pos",position.toString())}
        return fragment
    }
}