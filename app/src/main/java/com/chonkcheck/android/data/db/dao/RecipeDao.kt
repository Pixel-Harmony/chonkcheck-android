package com.chonkcheck.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chonkcheck.android.data.db.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes WHERE id = :recipeId AND deletedAt IS NULL")
    fun getRecipeById(recipeId: String): Flow<RecipeEntity?>

    @Query("SELECT * FROM recipes WHERE id = :recipeId AND deletedAt IS NULL")
    suspend fun getRecipeByIdOnce(recipeId: String): RecipeEntity?

    @Query("""
        SELECT * FROM recipes
        WHERE userId = :userId
        AND deletedAt IS NULL
        ORDER BY updatedAt DESC
    """)
    fun getRecipesForUser(userId: String): Flow<List<RecipeEntity>>

    @Query("""
        SELECT * FROM recipes
        WHERE userId = :userId
        AND deletedAt IS NULL
        AND name LIKE '%' || :query || '%'
        ORDER BY name
        LIMIT :limit
    """)
    fun searchRecipes(userId: String, query: String, limit: Int = 50): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeEntity>)

    @Update
    suspend fun update(recipe: RecipeEntity)

    @Query("UPDATE recipes SET deletedAt = :deletedAt WHERE id = :recipeId")
    suspend fun softDelete(recipeId: String, deletedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM recipes WHERE id = :recipeId")
    suspend fun delete(recipeId: String)

    @Query("SELECT COUNT(*) FROM recipes WHERE userId = :userId AND deletedAt IS NULL")
    suspend fun getCountForUser(userId: String): Int
}
