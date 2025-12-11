package com.chicken.spaceattack.di

import com.chicken.spaceattack.domain.GameEngine
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
}
