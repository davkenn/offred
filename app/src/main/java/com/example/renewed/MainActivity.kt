package com.example.renewed

import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {


    @Inject lateinit var auth:AuthAPI
    @Inject lateinit var sm:SessionManager
    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate called")
        getAuthToken().subscribeBy{ sm.saveAuthToken(it) }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                                                            as NavHostFragment
        navController = navHostFragment.navController
        bottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            toggleFullscreen(destination.id) }
    }

    private fun getAuthToken(): Single<String> {
        val credentials = "u3MaMah0dOe1IA:"
        val encodedCredentials: String = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        return auth.installedClient("Basic $encodedCredentials",
            "https://oauth.reddit.com/grants/installed_client",
            "DO_NOT_TRACK_THIS_DEViCE")
            .map {  it.getOrDefault("access_token","") }
    }
    private fun toggleFullscreen(id:Int) {
        if (id == R.id.feed) {
            bottomNavigationView.visibility = View.GONE

            val ctrl = WindowCompat.getInsetsController(window,
                                                       window.decorView.findViewById(R.id.favorites))
            ctrl.hide(WindowInsetsCompat.Type.statusBars())
            ctrl.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            ctrl.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            bottomNavigationView.visibility = View.VISIBLE
            val ctrl = WindowCompat.getInsetsController(window,
                                                      window.decorView.findViewById(R.id.selection))

           ctrl.show(WindowInsetsCompat.Type.navigationBars())
         }
    }


}