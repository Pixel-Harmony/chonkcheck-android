package com.chonkcheck.android.data.repository

import com.chonkcheck.android.data.api.WeightApi
import com.chonkcheck.android.data.db.dao.WeightDao
import com.chonkcheck.android.data.mappers.toDomain
import com.chonkcheck.android.data.mappers.toEntity
import com.chonkcheck.android.data.mappers.toRequest
import com.chonkcheck.android.domain.model.CreateWeightParams
import com.chonkcheck.android.domain.model.WeightEntry
import com.chonkcheck.android.domain.repository.AuthRepository
import com.chonkcheck.android.domain.repository.WeightRepository
import io.sentry.Sentry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightRepositoryImpl @Inject constructor(
    private val weightApi: WeightApi,
    private val weightDao: WeightDao,
    private val authRepository: AuthRepository
) : WeightRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getWeightEntries(limit: Int?): Flow<List<WeightEntry>> {
        return authRepository.currentUser.flatMapLatest { user ->
            val userId = user?.id
            if (userId != null) {
                weightDao.getEntriesForUser(userId).map { entities ->
                    val entries = entities.map { it.toDomain() }
                    if (limit != null) {
                        entries.take(limit)
                    } else {
                        entries
                    }
                }
            } else {
                flowOf(emptyList())
            }
        }
    }

    override suspend fun createEntry(params: CreateWeightParams): Result<WeightEntry> {
        return try {
            val currentUser = authRepository.currentUser.first()
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            // Check if entry already exists for this date
            val existingEntry = weightDao.getEntryForDate(currentUser.id, params.date.toString())
            if (existingEntry != null) {
                // Update existing entry
                val updatedEntity = existingEntry.copy(
                    weight = params.weight,
                    notes = params.notes,
                    updatedAt = System.currentTimeMillis(),
                    syncedAt = null
                )
                weightDao.update(updatedEntity)

                // Try to sync with API
                try {
                    val response = weightApi.createWeightEntry(params.toRequest())
                    val syncedEntity = response.toEntity()
                    weightDao.update(syncedEntity)
                    Result.success(syncedEntity.toDomain())
                } catch (apiError: Exception) {
                    Sentry.captureException(apiError)
                    Result.success(updatedEntity.toDomain())
                }
            } else {
                // Create new entry
                val tempId = "temp_${UUID.randomUUID()}"
                val localEntity = params.toEntity(tempId, currentUser.id)
                weightDao.insert(localEntity)

                // Try to sync with API
                try {
                    val response = weightApi.createWeightEntry(params.toRequest())
                    val syncedEntity = response.toEntity()

                    // Replace temp entity with synced entity
                    weightDao.delete(tempId)
                    weightDao.insert(syncedEntity)

                    Result.success(syncedEntity.toDomain())
                } catch (apiError: Exception) {
                    Sentry.captureException(apiError)
                    Result.success(localEntity.toDomain())
                }
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun deleteEntry(date: LocalDate): Result<Unit> {
        return try {
            val currentUser = authRepository.currentUser.first()
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            val entry = weightDao.getEntryForDate(currentUser.id, date.toString())
                ?: return Result.failure(IllegalArgumentException("Entry not found"))

            // Soft delete locally first
            weightDao.softDelete(entry.id)

            // Try to sync with API
            try {
                weightApi.deleteWeightEntry(date.toString())
            } catch (apiError: Exception) {
                Sentry.captureException(apiError)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun refresh() {
        try {
            val currentUser = authRepository.currentUser.first() ?: return

            val response = weightApi.getWeightEntries()
            val entities = response.entries.map { it.toEntity() }

            // Update local database with fresh data
            weightDao.insertAll(entities)
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }
}
