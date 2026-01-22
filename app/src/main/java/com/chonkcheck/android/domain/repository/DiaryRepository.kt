package com.chonkcheck.android.domain.repository

import com.chonkcheck.android.domain.model.CreateDiaryEntryParams
import com.chonkcheck.android.domain.model.DiaryDay
import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.model.UpdateDiaryEntryParams
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DiaryRepository {
    fun getDiaryDay(date: LocalDate): Flow<DiaryDay>
    fun getDiaryEntryById(id: String): Flow<DiaryEntry?>
    suspend fun createEntry(params: CreateDiaryEntryParams): Result<DiaryEntry>
    suspend fun updateEntry(id: String, params: UpdateDiaryEntryParams): Result<DiaryEntry>
    suspend fun deleteEntry(id: String): Result<Unit>
    suspend fun completeDay(date: LocalDate): Result<Unit>
    suspend fun uncompleteDay(date: LocalDate): Result<Unit>
    suspend fun refresh(date: LocalDate)
}
