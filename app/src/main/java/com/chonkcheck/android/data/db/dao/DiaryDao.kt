package com.chonkcheck.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chonkcheck.android.data.db.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary_entries WHERE id = :entryId AND deletedAt IS NULL")
    fun getEntryById(entryId: String): Flow<DiaryEntryEntity?>

    @Query("SELECT * FROM diary_entries WHERE id = :entryId AND deletedAt IS NULL")
    suspend fun getEntryByIdOnce(entryId: String): DiaryEntryEntity?

    @Query("""
        SELECT * FROM diary_entries
        WHERE userId = :userId
        AND date = :date
        AND deletedAt IS NULL
        ORDER BY
            CASE mealType
                WHEN 'breakfast' THEN 0
                WHEN 'lunch' THEN 1
                WHEN 'dinner' THEN 2
                WHEN 'snacks' THEN 3
                ELSE 4
            END,
            createdAt
    """)
    fun getEntriesForDate(userId: String, date: String): Flow<List<DiaryEntryEntity>>

    @Query("""
        SELECT * FROM diary_entries
        WHERE userId = :userId
        AND date = :date
        AND mealType = :mealType
        AND deletedAt IS NULL
        ORDER BY createdAt
    """)
    fun getEntriesForMeal(userId: String, date: String, mealType: String): Flow<List<DiaryEntryEntity>>

    @Query("""
        SELECT
            SUM(calories) as totalCalories,
            SUM(protein) as totalProtein,
            SUM(carbs) as totalCarbs,
            SUM(fat) as totalFat
        FROM diary_entries
        WHERE userId = :userId
        AND date = :date
        AND deletedAt IS NULL
    """)
    fun getDailySummary(userId: String, date: String): Flow<DailySummaryResult?>

    @Query("""
        SELECT date, SUM(calories) as totalCalories
        FROM diary_entries
        WHERE userId = :userId
        AND date BETWEEN :startDate AND :endDate
        AND deletedAt IS NULL
        GROUP BY date
        ORDER BY date
    """)
    fun getCalorieHistory(userId: String, startDate: String, endDate: String): Flow<List<DateCaloriesResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DiaryEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DiaryEntryEntity>)

    @Update
    suspend fun update(entry: DiaryEntryEntity)

    @Query("UPDATE diary_entries SET deletedAt = :deletedAt WHERE id = :entryId")
    suspend fun softDelete(entryId: String, deletedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM diary_entries WHERE id = :entryId")
    suspend fun delete(entryId: String)

    @Query("DELETE FROM diary_entries WHERE userId = :userId AND date = :date")
    suspend fun deleteAllForDate(userId: String, date: String)
}

data class DailySummaryResult(
    val totalCalories: Double?,
    val totalProtein: Double?,
    val totalCarbs: Double?,
    val totalFat: Double?
)

data class DateCaloriesResult(
    val date: String,
    val totalCalories: Double
)
