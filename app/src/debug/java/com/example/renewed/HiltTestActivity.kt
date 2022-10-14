package com.example.renewed

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HiltTestActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        val themeRes = intent.getIntExtra(THEME_EXTRAS_BUNDLE_KEY, 0)
   //     require(themeRes != 0) { "No theme configured for ${this.javaClass}" }
        setTheme(themeRes)
        super.onCreate(savedInstanceState)
    }
    companion object {

        private const val THEME_EXTRAS_BUNDLE_KEY = "theme-extra-bundle-key"

        fun createIntent(context: Context, @StyleRes themeResId: Int): Intent {
            val componentName = ComponentName(context, HiltTestActivity::class.java)
            return Intent.makeMainActivity(componentName)
                .putExtra(THEME_EXTRAS_BUNDLE_KEY, themeResId)
        }
    }
}

