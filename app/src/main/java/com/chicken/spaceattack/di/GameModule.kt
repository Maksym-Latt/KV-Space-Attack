package com.chicken.spaceattack.di

import android.content.Context
import com.chicken.spaceattack.domain.GameEngine
import com.chicken.spaceattack.domain.UpgradeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GameModule {
    @Provides
    @Singleton
    fun provideGameEngine(): GameEngine = GameEngine()

    @Provides
    @Singleton
    fun provideUpgradeRepository(@ApplicationContext context: Context): UpgradeRepository =
            UpgradeRepository(context)
}
