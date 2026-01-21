package com.chonkcheck.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chonkcheck.android.data.db.entity.SavedMealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedMealDao {

    @Query("SELECT * FROM saved_meals WHERE id = :savedMealId AND deletedAt IS NULL")
    fun getSavedMealById(savedMealId: String): Flow<SavedMealEntity?>

    @Query("SELECT * FROM saved_meals WHERE id = :savedMealId AND deletedAt IS NULL")
    suspend fun getSavedMealByIdOnce(savedMealId: String): SavedMealEntity?

    @Query("""
        SELECT * FROM saved_meals
        WHERE userId = :userId
        AND deletedAt IS NULL
        ORDER BY usageCount DESC, updatedAt DESC
    """)
    fun getSavedMealsForUser(userId: String): Flow<List<SavedMealEntity>>

    @Query("""
        SELECT * FROM saved_meals
        WHERE userId = :userId
        AND deletedAt IS NULL
        AND name LIKE '%' || :query || '%'
        ORDER BY name
        LIMIT :limit
    """)
    fun searchSavedMeals(userId: String, query: String, limit: Int = 50): Flow<List<SavedMealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(savedMeal: SavedMealEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(savedMeals: List<SavedMealEntity>)

    @Update
    suspend fun update(savedMeal: SavedMealEntity)

    @Query("""
        UPDATE saved_meals
        SET usageCount = usageCount + 1, lastUsedAt = :lastUsedAt
        WHERE id = :savedMealId
    """)
    suspend fun incrementUsage(savedMealId: String, lastUsedAt: Long = System.currentTimeMillis())

    @Query("UPDATE saved_meals SET deletedAt = :deletedAt WHERE id = :savedMealId")
    suspend fun softDelete(savedMealId: String, deletedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM saved_meals WHERE id = :savedMealId")
    suspend fun delete(savedMealId: String)

    @Query("SELECT COUNT(*) FROM saved_meals WHERE userId = :userId AND deletedAt IS NULL")
    suspend fun getCountForUser(userId: String): Int
}
