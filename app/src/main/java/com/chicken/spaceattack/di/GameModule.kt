package com.chicken.spaceattack.di

import com.chicken.spaceattack.domain.GameEngine
import com.chicken.spaceattack.domain.UpgradeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun provideUpgradeRepository(): UpgradeRepository = UpgradeRepository()
}
