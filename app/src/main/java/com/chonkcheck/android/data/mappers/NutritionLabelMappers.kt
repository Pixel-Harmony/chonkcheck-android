package com.chonkcheck.android.data.mappers

import com.chonkcheck.android.data.api.dto.NutritionDataDto
import com.chonkcheck.android.domain.model.NutritionLabelData

fun NutritionDataDto.toDomain(): NutritionLabelData = NutritionLabelData(
    name = name,
    brand = brand,
    servingSize = servingSize,
    servingUnit = servingUnit,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    fiber = fiber,
    sugar = sugar,
    sodium = sodium,
    saturatedFat = saturatedFat,
    cholesterol = cholesterol
)
