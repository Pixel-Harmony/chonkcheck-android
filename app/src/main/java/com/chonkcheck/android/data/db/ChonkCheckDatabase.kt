package com.chonkcheck.android.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chonkcheck.android.data.db.converters.Converters
import com.chonkcheck.android.data.db.dao.DiaryDao
import com.chonkcheck.android.data.db.dao.ExerciseDao
import com.chonkcheck.android.data.db.dao.FoodDao
import com.chonkcheck.android.data.db.dao.RecipeDao
import com.chonkcheck.android.data.db.dao.SavedMealDao
import com.chonkcheck.android.data.db.dao.SyncQueueDao
import com.chonkcheck.android.data.db.dao.UserDao
import com.chonkcheck.android.data.db.dao.WeightDao
import com.chonkcheck.android.data.db.entity.DiaryEntryEntity
import com.chonkcheck.android.data.db.entity.ExerciseEntryEntity
import com.chonkcheck.android.data.db.entity.FoodEntity
import com.chonkcheck.android.data.db.entity.RecipeEntity
import com.chonkcheck.android.data.db.entity.SavedMealEntity
import com.chonkcheck.android.data.db.entity.SyncQueueEntity
import com.chonkcheck.android.data.db.entity.UserEntity
import com.chonkcheck.android.data.db.entity.WeightEntryEntity

@Database(
    entities = [
        UserEntity::class,
        FoodEntity::class,
        DiaryEntryEntity::class,
        RecipeEntity::class,
        SavedMealEntity::class,
        WeightEntryEntity::class,
        ExerciseEntryEntity::class,
        SyncQueueEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ChonkCheckDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun foodDao(): FoodDao
    abstract fun diaryDao(): DiaryDao
    abstract fun recipeDao(): RecipeDao
    abstract fun savedMealDao(): SavedMealDao
    abstract fun weightDao(): WeightDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun syncQueueDao(): SyncQueueDao
}
