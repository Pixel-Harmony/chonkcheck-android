package com.chonkcheck.android.domain.repository

import com.chonkcheck.android.domain.model.CreateWeightParams
import com.chonkcheck.android.domain.model.WeightEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface WeightRepository {
    fun getWeightEntries(limit: Int? = null): Flow<List<WeightEntry>>
    suspend fun createEntry(params: CreateWeightParams): Result<WeightEntry>
    suspend fun deleteEntry(date: LocalDate): Result<Unit>
    suspend fun refresh()
}
