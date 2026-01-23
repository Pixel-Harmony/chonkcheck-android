package com.chonkcheck.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chonkcheck.android.data.db.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUserOnce(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Query("UPDATE users SET onboardingCompleted = :completed, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updateOnboardingStatus(userId: String, completed: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE users SET
            dailyCalorieTarget = :calories,
            proteinTarget = :protein,
            carbsTarget = :carbs,
            fatTarget = :fat,
            bmr = :bmr,
            tdee = :tdee,
            updatedAt = :updatedAt
        WHERE id = :userId
    """)
    suspend fun updateGoals(
        userId: String,
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
        bmr: Int,
        tdee: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE users SET
            weightUnit = :weightUnit,
            heightUnit = :heightUnit,
            height = :height,
            birthDate = :birthDate,
            sex = :sex,
            activityLevel = :activityLevel,
            weightGoal = :weightGoal,
            targetWeight = :targetWeight,
            weeklyGoal = :weeklyGoal,
            dailyCalorieTarget = :calories,
            proteinTarget = :protein,
            carbsTarget = :carbs,
            fatTarget = :fat,
            bmr = :bmr,
            tdee = :tdee,
            onboardingCompleted = 1,
            updatedAt = :updatedAt
        WHERE id = :userId
    """)
    suspend fun completeOnboarding(
        userId: String,
        weightUnit: String,
        heightUnit: String,
        height: Double,
        birthDate: String,
        sex: String,
        activityLevel: String,
        weightGoal: String,
        targetWeight: Double?,
        weeklyGoal: Double?,
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
        bmr: Int,
        tdee: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun delete(userId: String)

    @Query("DELETE FROM users")
    suspend fun deleteAll()

    @Query("""
        UPDATE users SET
            weightUnit = :weightUnit,
            heightUnit = :heightUnit,
            height = :height,
            birthDate = :birthDate,
            sex = :sex,
            activityLevel = :activityLevel,
            updatedAt = :updatedAt
        WHERE id = :userId
    """)
    suspend fun updateProfile(
        userId: String,
        weightUnit: String,
        heightUnit: String,
        height: Double?,
        birthDate: String?,
        sex: String?,
        activityLevel: String?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE users SET
            weightGoal = :weightGoal,
            targetWeight = :targetWeight,
            weeklyGoal = :weeklyGoal,
            dailyCalorieTarget = :calories,
            proteinTarget = :protein,
            carbsTarget = :carbs,
            fatTarget = :fat,
            bmr = :bmr,
            tdee = :tdee,
            updatedAt = :updatedAt
        WHERE id = :userId
    """)
    suspend fun updateFullGoals(
        userId: String,
        weightGoal: String?,
        targetWeight: Double?,
        weeklyGoal: Double?,
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
        bmr: Int?,
        tdee: Int?,
        updatedAt: Long = System.currentTimeMillis()
    )
}
