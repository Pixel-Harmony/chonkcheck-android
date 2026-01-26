package com.chonkcheck.android.di

import com.chonkcheck.android.data.api.MilestoneApi
import com.chonkcheck.android.data.repository.MilestoneRepositoryImpl
import com.chonkcheck.android.domain.repository.MilestoneRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MilestoneModule {

    @Binds
    @Singleton
    abstract fun bindMilestoneRepository(impl: MilestoneRepositoryImpl): MilestoneRepository

    companion object {
        @Provides
        @Singleton
        fun provideMilestoneApi(retrofit: Retrofit): MilestoneApi =
            retrofit.create(MilestoneApi::class.java)
    }
}
