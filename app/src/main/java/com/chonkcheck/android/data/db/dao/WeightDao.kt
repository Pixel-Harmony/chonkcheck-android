package com.chonkcheck.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chonkcheck.android.data.db.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {

    @Query("SELECT * FROM weight_entries WHERE id = :entryId AND deletedAt IS NULL")
    fun getEntryById(entryId: String): Flow<WeightEntryEntity?>

    @Query("SELECT * FROM weight_entries WHERE id = :entryId AND deletedAt IS NULL")
    suspend fun getEntryByIdOnce(entryId: String): WeightEntryEntity?

    @Query("""
        SELECT * FROM weight_entries
        WHERE userId = :userId
        AND deletedAt IS NULL
        ORDER BY date DESC
    """)
    fun getEntriesForUser(userId: String): Flow<List<WeightEntryEntity>>

    @Query("""
        SELECT * FROM weight_entries
        WHERE userId = :userId
        AND date BETWEEN :startDate AND :endDate
        AND deletedAt IS NULL
        ORDER BY date
    """)
    fun getEntriesInRange(userId: String, startDate: String, endDate: String): Flow<List<WeightEntryEntity>>

    @Query("""
        SELECT * FROM weight_entries
        WHERE userId = :userId
        AND deletedAt IS NULL
        ORDER BY date DESC
        LIMIT 1
    """)
    fun getLatestEntry(userId: String): Flow<WeightEntryEntity?>

    @Query("""
        SELECT * FROM weight_entries
        WHERE userId = :userId
        AND deletedAt IS NULL
        ORDER BY date DESC
        LIMIT 1
    """)
    suspend fun getLatestEntryOnce(userId: String): WeightEntryEntity?

    @Query("""
        SELECT * FROM weight_entries
        WHERE userId = :userId
        AND date = :date
        AND deletedAt IS NULL
    """)
    suspend fun getEntryForDate(userId: String, date: String): WeightEntryEntity?

    @Query("""
        SELECT AVG(weight) FROM weight_entries
        WHERE userId = :userId
        AND date BETWEEN :startDate AND :endDate
        AND deletedAt IS NULL
    """)
    suspend fun getAverageWeight(userId: String, startDate: String, endDate: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WeightEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<WeightEntryEntity>)

    @Update
    suspend fun update(entry: WeightEntryEntity)

    @Query("UPDATE weight_entries SET deletedAt = :deletedAt WHERE id = :entryId")
    suspend fun softDelete(entryId: String, deletedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM weight_entries WHERE id = :entryId")
    suspend fun delete(entryId: String)

    @Query("SELECT COUNT(*) FROM weight_entries WHERE userId = :userId AND deletedAt IS NULL")
    suspend fun getCountForUser(userId: String): Int
}
