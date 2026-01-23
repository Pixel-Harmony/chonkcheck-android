package com.chonkcheck.android.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.chonkcheck.android.domain.model.HeightUnit
import com.chonkcheck.android.domain.model.WeightUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.unitsDataStore: DataStore<Preferences> by preferencesDataStore(name = "units")

@Singleton
class UnitsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val weightUnitKey = stringPreferencesKey("weight_unit")
    private val heightUnitKey = stringPreferencesKey("height_unit")

    val weightUnit: Flow<WeightUnit?> = context.unitsDataStore.data.map { preferences ->
        preferences[weightUnitKey]?.let { unitName ->
            try {
                WeightUnit.valueOf(unitName)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    val heightUnit: Flow<HeightUnit?> = context.unitsDataStore.data.map { preferences ->
        preferences[heightUnitKey]?.let { unitName ->
            try {
                HeightUnit.valueOf(unitName)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        context.unitsDataStore.edit { preferences ->
            preferences[weightUnitKey] = unit.name
        }
    }

    suspend fun setHeightUnit(unit: HeightUnit) {
        context.unitsDataStore.edit { preferences ->
            preferences[heightUnitKey] = unit.name
        }
    }
}
