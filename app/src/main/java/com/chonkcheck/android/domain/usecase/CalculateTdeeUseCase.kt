package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.ActivityLevel
import com.chonkcheck.android.domain.model.Sex
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject

data class TdeeResult(
    val bmr: Int,
    val tdee: Int,
    val maintenanceCalories: Int
)

class CalculateTdeeUseCase @Inject constructor() {

    operator fun invoke(
        weightKg: Double,
        heightCm: Double,
        birthDate: LocalDate,
        sex: Sex,
        activityLevel: ActivityLevel
    ): TdeeResult {
        val age = Period.between(birthDate, LocalDate.now()).years

        val bmr = calculateBmr(weightKg, heightCm, age, sex)
        val tdee = (bmr * activityLevel.factor).toInt()

        return TdeeResult(
            bmr = bmr,
            tdee = tdee,
            maintenanceCalories = tdee
        )
    }

    private fun calculateBmr(
        weightKg: Double,
        heightCm: Double,
        age: Int,
        sex: Sex
    ): Int {
        val baseBmr = (10 * weightKg) + (6.25 * heightCm) - (5 * age)

        return when (sex) {
            Sex.MALE -> (baseBmr + 5).toInt()
            Sex.FEMALE -> (baseBmr - 161).toInt()
            Sex.OTHER -> baseBmr.toInt()
        }
    }
}
