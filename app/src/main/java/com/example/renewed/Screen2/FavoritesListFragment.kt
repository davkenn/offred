package com.example.renewed.Screen2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.renewed.R

import com.example.renewed.databinding.FragmentFavoritesListBinding
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import timber.log.Timber

@AndroidEntryPoint
class FavoritesListFragment : Fragment(R.layout.fragment_favorites_list) {
    private lateinit var adapter2 : FragmentStateAdapter
    private val favoritesVM: FavoritesListVM by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate in FavoritesListFragment")
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("onViewCreated in FavoritesListFragment")

        super.onViewCreated(view, savedInstanceState)

        adapter2 = FavoritesListAdapter(this)
        val binding = FragmentFavoritesListBinding.bind(view)
        binding.apply { pager.adapter = adapter2
                        pager.orientation=ViewPager2.ORIENTATION_VERTICAL
        }

        favoritesVM.vs.observeOn(AndroidSchedulers.mainThread())
            .subscribe({ Timber.d("FavoritesListVM::",it.toString())},{
                Timber.e("FAVLISTERROR",it.stackTrace)})


    }

    override fun onDestroy() {
        Timber.d("onDestroy in FavoritesListFragment")
        super.onDestroy()
    }

    override fun onDestroyView() {
        Timber.d("onDestroyView in FavoritesListFragment")
        super.onDestroyView()
    }

    override fun onPause() {
        Timber.d("onPause in FavoritesListFragment")
        super.onPause()
    }

    override fun onResume() {
        Timber.d("onResume in FavoritesListFragment")
        super.onResume()
    }
}