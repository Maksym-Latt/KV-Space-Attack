package com.chicken.spaceattack.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import com.chicken.spaceattack.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var menuPlayer: MediaPlayer? = null
    private var gamePlayer: MediaPlayer? = null
    private var sfxPlayer: MediaPlayer? = null

    var isMusicEnabled: Boolean = true
        private set
    var isSoundEnabled: Boolean = true
        private set

    fun toggleMusic() {
        isMusicEnabled = !isMusicEnabled
        if (isMusicEnabled) {
            resumeMusic()
        } else {
            stopMusic()
        }
    }

    fun toggleSound() {
        isSoundEnabled = !isSoundEnabled
    }

    fun playMenuMusic() {
        if (!isMusicEnabled) return
        if (menuPlayer == null) {
            menuPlayer = createLoopingPlayer(R.raw.music_menu)
        }
        gamePlayer?.pause()
        menuPlayer?.start()
    }

    fun playGameMusic() {
        if (!isMusicEnabled) return
        if (gamePlayer == null) {
            gamePlayer = createLoopingPlayer(R.raw.music_game)
        }
        menuPlayer?.pause()
        gamePlayer?.start()
    }

    fun playShot() = playSfx(R.raw.sfx_shot)
    fun playHit() = playSfx(R.raw.sfx_hit)
    fun playExplosion() = playSfx(R.raw.sfx_explosion)
    fun playLose() = playSfx(R.raw.sfx_lose)
    fun playWin() = playSfx(R.raw.sfx_win)

    fun onAppForeground() {
        resumeMusic()
    }

    fun onAppBackground() {
        pauseAll()
    }

    fun persistSettings() {
        // Placeholder for future DataStore persistence
    }

    private fun playSfx(@RawRes res: Int) {
        if (!isSoundEnabled) return
        sfxPlayer?.release()
        sfxPlayer = MediaPlayer.create(context, res)
        sfxPlayer?.setOnCompletionListener { player ->
            player.release()
        }
        sfxPlayer?.start()
    }

    private fun createLoopingPlayer(@RawRes res: Int): MediaPlayer {
        return MediaPlayer.create(context, res).apply {
            isLooping = true
        }
    }

    private fun resumeMusic() {
        if (!isMusicEnabled) return
        if (gamePlayer?.isPlaying == false) gamePlayer?.start()
        if (menuPlayer?.isPlaying == false) menuPlayer?.start()
    }

    private fun stopMusic() {
        gamePlayer?.pause()
        menuPlayer?.pause()
    }

    private fun pauseAll() {
        gamePlayer?.pause()
        menuPlayer?.pause()
    }
}
