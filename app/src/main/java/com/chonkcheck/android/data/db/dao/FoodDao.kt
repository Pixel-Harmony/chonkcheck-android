package com.chonkcheck.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chonkcheck.android.data.db.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Query("SELECT * FROM foods WHERE id = :foodId AND archivedAt IS NULL")
    fun getFoodById(foodId: String): Flow<FoodEntity?>

    @Query("SELECT * FROM foods WHERE id = :foodId AND archivedAt IS NULL")
    suspend fun getFoodByIdOnce(foodId: String): FoodEntity?

    @Query("SELECT * FROM foods WHERE barcode = :barcode AND archivedAt IS NULL LIMIT 1")
    suspend fun getFoodByBarcode(barcode: String): FoodEntity?

    @Query("""
        SELECT * FROM foods
        WHERE archivedAt IS NULL
        AND (name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%')
        ORDER BY
            CASE WHEN name LIKE :query || '%' THEN 0 ELSE 1 END,
            name
        LIMIT :limit
    """)
    fun searchFoods(query: String, limit: Int = 50): Flow<List<FoodEntity>>

    @Query("""
        SELECT * FROM foods
        WHERE archivedAt IS NULL
        AND type = :type
        AND (name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%')
        ORDER BY name
        LIMIT :limit
    """)
    fun searchFoodsByType(query: String, type: String, limit: Int = 50): Flow<List<FoodEntity>>

    @Query("""
        SELECT * FROM foods
        WHERE archivedAt IS NULL
        AND userId = :userId
        ORDER BY updatedAt DESC
        LIMIT :limit
    """)
    fun getUserFoods(userId: String, limit: Int = 100): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE archivedAt IS NULL ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentFoods(limit: Int = 20): Flow<List<FoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(food: FoodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foods: List<FoodEntity>)

    @Update
    suspend fun update(food: FoodEntity)

    @Query("UPDATE foods SET archivedAt = :archivedAt WHERE id = :foodId")
    suspend fun softDelete(foodId: String, archivedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM foods WHERE id = :foodId")
    suspend fun delete(foodId: String)

    @Query("DELETE FROM foods WHERE syncedAt < :threshold AND type = 'platform'")
    suspend fun deleteStale(threshold: Long)

    @Query("SELECT COUNT(*) FROM foods WHERE archivedAt IS NULL")
    suspend fun getCount(): Int
}
