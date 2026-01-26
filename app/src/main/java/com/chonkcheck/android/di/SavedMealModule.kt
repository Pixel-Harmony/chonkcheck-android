package com.chonkcheck.android.di

import com.chonkcheck.android.data.repository.SavedMealRepositoryImpl
import com.chonkcheck.android.domain.repository.SavedMealRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SavedMealModule {

    @Binds
    @Singleton
    abstract fun bindSavedMealRepository(impl: SavedMealRepositoryImpl): SavedMealRepository
}
