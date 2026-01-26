package com.chonkcheck.android.domain.model

data class SavedMeal(
    val id: String,
    val userId: String,
    val name: String,
    val items: List<SavedMealItem>,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val usageCount: Int = 0,
    val lastUsedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)

data class SavedMealItem(
    val itemId: String,
    val itemType: SavedMealItemType,
    val itemName: String,
    val brand: String?,
    val servingSize: Double,
    val servingUnit: ServingUnit,
    val quantity: Double,
    val enteredAmount: Double?,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

enum class SavedMealItemType {
    FOOD,
    RECIPE;

    fun toApiValue(): String = when (this) {
        FOOD -> "food"
        RECIPE -> "recipe"
    }

    companion object {
        fun fromApiValue(value: String): SavedMealItemType = when (value.lowercase()) {
            "food" -> FOOD
            "recipe" -> RECIPE
            else -> FOOD
        }
    }
}

data class CreateSavedMealParams(
    val name: String,
    val items: List<SavedMealItemParams>
)

data class UpdateSavedMealParams(
    val name: String?,
    val items: List<SavedMealItemParams>?
)

data class SavedMealItemParams(
    val itemId: String,
    val itemType: SavedMealItemType,
    val quantity: Double,
    val enteredAmount: Double?
)

data class AddMealToDiaryParams(
    val savedMealId: String,
    val date: String,
    val mealType: MealType,
    val items: List<AddMealToDiaryItemParams>
)

data class AddMealToDiaryItemParams(
    val itemId: String,
    val itemType: SavedMealItemType,
    val quantity: Double,
    val enteredAmount: Double?
)
