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
import com.chicken.spaceattack.audio.AudioEngine
import com.chicken.spaceattack.ui.navigation.AppNavHost
import com.chicken.spaceattack.ui.theme.ChickenWarsTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var audioEngine: AudioEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChickenWarsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(audioEngine = audioEngine)
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
        audioEngine.onAppForeground()
    }

    override fun onPause() {
        super.onPause()
        audioEngine.onAppBackground()
    }

    override fun onDestroy() {
        audioEngine.onAppBackground()
        super.onDestroy()
    }
}
