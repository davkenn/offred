package com.example.renewed

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels

import androidx.fragment.app.activityViewModels

import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var navController: NavController





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate called")




        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController








    }
//
  //  override fun onRestart() {
    //    super.onRestart()
   // }

    override fun onResume() {
        super.onResume()

    }
    override fun onPause() {
        super.onPause()

    }

    override fun onDestroy() {

        super.onDestroy()

    }




}