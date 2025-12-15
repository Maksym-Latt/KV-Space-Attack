package com.chicken.spaceattack.audio

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.annotation.RawRes
import com.chicken.spaceattack.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioEngine @Inject constructor(@ApplicationContext private val context: Context) {
    private var menuPlayer: MediaPlayer? = null
    private var gamePlayer: MediaPlayer? = null

    private var currentMusicContext: MusicContext = MusicContext.NONE

    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<Int, Int>()
    private var soundsLoaded = false

    private val prefs: SharedPreferences =
            context.getSharedPreferences("audio_prefs", Context.MODE_PRIVATE)

    var isMusicEnabled: Boolean = prefs.getBoolean(KEY_MUSIC, true)
        private set
    var isSoundEnabled: Boolean = prefs.getBoolean(KEY_SOUND, true)
        private set

    init {
        initSoundPool()
    }

    private fun initSoundPool() {
        val audioAttributes =
                AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()

        soundPool =
                SoundPool.Builder()
                        .setMaxStreams(5) // Max 5 simultaneous sounds
                        .setAudioAttributes(audioAttributes)
                        .build()

        soundPool?.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                soundsLoaded = true
            }
        }

        // Preload all sound effects
        try {
            soundIds[R.raw.sfx_shot] = soundPool?.load(context, R.raw.sfx_shot, 1) ?: 0
            soundIds[R.raw.sfx_drop] = soundPool?.load(context, R.raw.sfx_drop, 1) ?: 0
            soundIds[R.raw.sfx_hit] = soundPool?.load(context, R.raw.sfx_hit, 1) ?: 0
            soundIds[R.raw.sfx_hit] = soundPool?.load(context, R.raw.sfx_hit, 1) ?: 0
            soundIds[R.raw.sfx_explosion] = soundPool?.load(context, R.raw.sfx_explosion, 1) ?: 0
            soundIds[R.raw.sfx_get_damage] = soundPool?.load(context, R.raw.sfx_get_damage, 1) ?: 0
            soundIds[R.raw.sfx_boss_income] =
                    soundPool?.load(context, R.raw.sfx_boss_income, 1) ?: 0
            soundIds[R.raw.sfx_lose] = soundPool?.load(context, R.raw.sfx_lose, 1) ?: 0
            soundIds[R.raw.sfx_win] = soundPool?.load(context, R.raw.sfx_win, 1) ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleMusic() {
        isMusicEnabled = !isMusicEnabled
        prefs.edit().putBoolean(KEY_MUSIC, isMusicEnabled).apply()

        if (isMusicEnabled) {
            // Resume the current context music
            when (currentMusicContext) {
                MusicContext.MENU -> playMenuMusic()
                MusicContext.GAME -> playGameMusic()
                MusicContext.NONE -> Unit
            }
        } else {
            // Stop all music
            stopAllMusic()
        }
    }

    fun toggleSound() {
        isSoundEnabled = !isSoundEnabled
        prefs.edit().putBoolean(KEY_SOUND, isSoundEnabled).apply()
    }

    fun playMenuMusic() {
        currentMusicContext = MusicContext.MENU
        if (!isMusicEnabled) return

        try {
            // Stop game music completely
            gamePlayer?.pause()

            // Create or resume menu music
            if (menuPlayer == null) {
                menuPlayer = createLoopingPlayer(R.raw.music_menu)
            }

            if (menuPlayer?.isPlaying == false) {
                menuPlayer?.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playGameMusic() {
        currentMusicContext = MusicContext.GAME
        if (!isMusicEnabled) return

        try {
            // Stop menu music completely
            menuPlayer?.pause()

            // Create or resume game music
            if (gamePlayer == null) {
                gamePlayer = createLoopingPlayer(R.raw.music_game)
            }

            if (gamePlayer?.isPlaying == false) {
                gamePlayer?.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playShot() = playSfx(R.raw.sfx_shot)
    fun playDrop() = playSfx(R.raw.sfx_drop)
    fun playHit() = playSfx(R.raw.sfx_hit)
    fun playExplosion() = playSfx(R.raw.sfx_explosion)
    fun playGetDamage() = playSfx(R.raw.sfx_get_damage)
    fun playBossIncome() = playSfx(R.raw.sfx_boss_income)
    fun playLose() = playSfx(R.raw.sfx_lose)
    fun playWin() = playSfx(R.raw.sfx_win)

    fun onAppForeground() {
        // Resume only the current context music
        if (!isMusicEnabled) return

        try {
            when (currentMusicContext) {
                MusicContext.MENU -> {
                    if (menuPlayer?.isPlaying == false) menuPlayer?.start()
                }
                MusicContext.GAME -> {
                    if (gamePlayer?.isPlaying == false) gamePlayer?.start()
                }
                MusicContext.NONE -> Unit
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onAppBackground() {
        pauseAll()
    }

    fun release() {
        try {
            menuPlayer?.release()
            menuPlayer = null
            gamePlayer?.release()
            gamePlayer = null
            soundPool?.release()
            soundPool = null
            soundIds.clear()
            soundsLoaded = false
            currentMusicContext = MusicContext.NONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playSfx(@RawRes res: Int) {
        if (!isSoundEnabled || !soundsLoaded) return
        try {
            val soundId = soundIds[res] ?: return
            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createLoopingPlayer(@RawRes res: Int): MediaPlayer? {
        return try {
            MediaPlayer.create(context, res)?.apply {
                isLooping = true
                setOnErrorListener { _, _, _ ->
                    // Handle error gracefully
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun stopAllMusic() {
        try {
            gamePlayer?.pause()
            menuPlayer?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun pauseAll() {
        try {
            gamePlayer?.pause()
            menuPlayer?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private enum class MusicContext {
        NONE,
        MENU,
        GAME
    }

    private companion object {
        const val KEY_MUSIC = "music_enabled"
        const val KEY_SOUND = "sound_enabled"
    }
}
