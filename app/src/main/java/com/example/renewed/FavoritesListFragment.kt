package com.example.renewed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

import com.example.renewed.databinding.FragmentFavoritesListBinding
import com.example.renewed.databinding.FragmentSubredditsSelectionBinding

class FavoritesListFragment : Fragment(R.layout.fragment_favorites_list) {
    private lateinit var adapter2 : FragmentStateAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter2 = FavoritesListAdapter(this)
        val binding = FragmentFavoritesListBinding.bind(view)
        binding.apply { pager.adapter = adapter2 }


    }
}