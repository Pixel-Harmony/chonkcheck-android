package com.chonkcheck.android.di

import com.chonkcheck.android.data.repository.WeightRepositoryImpl
import com.chonkcheck.android.domain.repository.WeightRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WeightModule {

    @Binds
    @Singleton
    abstract fun bindWeightRepository(impl: WeightRepositoryImpl): WeightRepository
}
