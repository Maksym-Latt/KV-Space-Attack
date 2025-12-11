package com.chicken.spaceattack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.chicken.spaceattack.audio.AudioController
import com.chicken.spaceattack.ui.navigation.AppNavHost
import com.chicken.spaceattack.ui.theme.ChickenWarsTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var audioController: AudioController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChickenWarsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(audioController = audioController)
                }
                SideEffect { hideSystemUI() }
            }
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        audioController.onAppForeground()
    }

    override fun onPause() {
        super.onPause()
        audioController.onAppBackground()
    }

    override fun onDestroy() {
        audioController.onAppBackground()
        super.onDestroy()
    }
}
