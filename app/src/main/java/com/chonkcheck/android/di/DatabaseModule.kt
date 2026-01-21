package com.chonkcheck.android.di

import android.content.Context
import androidx.room.Room
import com.chonkcheck.android.data.db.ChonkCheckDatabase
import com.chonkcheck.android.data.db.dao.DiaryDao
import com.chonkcheck.android.data.db.dao.ExerciseDao
import com.chonkcheck.android.data.db.dao.FoodDao
import com.chonkcheck.android.data.db.dao.RecipeDao
import com.chonkcheck.android.data.db.dao.SavedMealDao
import com.chonkcheck.android.data.db.dao.SyncQueueDao
import com.chonkcheck.android.data.db.dao.UserDao
import com.chonkcheck.android.data.db.dao.WeightDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ChonkCheckDatabase =
        Room.databaseBuilder(
            context,
            ChonkCheckDatabase::class.java,
            "chonkcheck.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideUserDao(database: ChonkCheckDatabase): UserDao = database.userDao()

    @Provides
    fun provideFoodDao(database: ChonkCheckDatabase): FoodDao = database.foodDao()

    @Provides
    fun provideDiaryDao(database: ChonkCheckDatabase): DiaryDao = database.diaryDao()

    @Provides
    fun provideRecipeDao(database: ChonkCheckDatabase): RecipeDao = database.recipeDao()

    @Provides
    fun provideSavedMealDao(database: ChonkCheckDatabase): SavedMealDao = database.savedMealDao()

    @Provides
    fun provideWeightDao(database: ChonkCheckDatabase): WeightDao = database.weightDao()

    @Provides
    fun provideExerciseDao(database: ChonkCheckDatabase): ExerciseDao = database.exerciseDao()

    @Provides
    fun provideSyncQueueDao(database: ChonkCheckDatabase): SyncQueueDao = database.syncQueueDao()
}
