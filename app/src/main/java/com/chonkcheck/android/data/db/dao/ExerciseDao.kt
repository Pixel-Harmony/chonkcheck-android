package com.chonkcheck.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chonkcheck.android.data.db.entity.ExerciseEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercise_entries WHERE id = :entryId AND deletedAt IS NULL")
    fun getEntryById(entryId: String): Flow<ExerciseEntryEntity?>

    @Query("SELECT * FROM exercise_entries WHERE id = :entryId AND deletedAt IS NULL")
    suspend fun getEntryByIdOnce(entryId: String): ExerciseEntryEntity?

    @Query("""
        SELECT * FROM exercise_entries
        WHERE userId = :userId
        AND date = :date
        AND deletedAt IS NULL
        ORDER BY createdAt
    """)
    fun getEntriesForDate(userId: String, date: String): Flow<List<ExerciseEntryEntity>>

    @Query("""
        SELECT COALESCE(SUM(caloriesBurned), 0.0)
        FROM exercise_entries
        WHERE userId = :userId
        AND date = :date
        AND deletedAt IS NULL
    """)
    fun getTotalCaloriesBurnedForDate(userId: String, date: String): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ExerciseEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<ExerciseEntryEntity>)

    @Update
    suspend fun update(entry: ExerciseEntryEntity)

    @Query("UPDATE exercise_entries SET deletedAt = :deletedAt WHERE id = :entryId")
    suspend fun softDelete(entryId: String, deletedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM exercise_entries WHERE id = :entryId")
    suspend fun delete(entryId: String)
}
