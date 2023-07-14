package com.example.renewed

import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.subscribeBy
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {


    @Inject lateinit var auth:AuthAPI
    @Inject lateinit var sm:SessionManager
    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView
    private var token:String=""
     fun login(): Completable {
        val credentials = "u3MaMah0dOe1IA:"

        val encodedCredentials: String = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        auth.installedClient(   "Basic $encodedCredentials",     "https://oauth.reddit.com/grants/installed_client",
            "DO_NOT_TRACK_THIS_DEViCE").subscribeBy { token=it.getOrDefault("access_token","");sm.saveAuthToken(token) }
        return Completable.complete()
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
//this destroys the app for now but maybe need it
//        sm.clearAuthToken()
        login().subscribe()
    //    sm.saveAuthToken(token)

        Timber.d("onCreate called")
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            toggleFullscreen(destination.id)
        }
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