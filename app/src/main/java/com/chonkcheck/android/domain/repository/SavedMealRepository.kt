package com.chonkcheck.android.domain.repository

import com.chonkcheck.android.domain.model.AddMealToDiaryParams
import com.chonkcheck.android.domain.model.CreateSavedMealParams
import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.model.SavedMeal
import com.chonkcheck.android.domain.model.UpdateSavedMealParams
import kotlinx.coroutines.flow.Flow

interface SavedMealRepository {

    fun searchSavedMeals(query: String, limit: Int = 50): Flow<List<SavedMeal>>

    fun getSavedMealById(id: String): Flow<SavedMeal?>

    suspend fun createSavedMeal(params: CreateSavedMealParams): Result<SavedMeal>

    suspend fun updateSavedMeal(id: String, params: UpdateSavedMealParams): Result<SavedMeal>

    suspend fun deleteSavedMeal(id: String): Result<Unit>

    suspend fun addMealToDiary(params: AddMealToDiaryParams): Result<List<DiaryEntry>>

    suspend fun refreshSavedMeals(query: String)
}
