package com.chonkcheck.android.domain.repository

import com.chonkcheck.android.domain.model.CreateFoodParams
import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.model.FoodFilter
import com.chonkcheck.android.domain.model.UpdateFoodParams
import kotlinx.coroutines.flow.Flow

interface FoodRepository {

    fun searchFoods(filter: FoodFilter): Flow<List<Food>>

    fun getFoodById(id: String): Flow<Food?>

    suspend fun getFoodByBarcode(barcode: String): Food?

    suspend fun createFood(params: CreateFoodParams): Result<Food>

    suspend fun updateFood(id: String, params: UpdateFoodParams): Result<Food>

    suspend fun deleteFood(id: String): Result<Unit>

    suspend fun promoteFood(id: String): Result<Unit>

    suspend fun refreshFoods(filter: FoodFilter)

    fun getRecentFoods(limit: Int = 20): Flow<List<Food>>

    fun getUserFoods(limit: Int = 100): Flow<List<Food>>
}
