package com.chonkcheck.android.presentation.ui.foods

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.CreateFoodParams
import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.model.FoodType
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.domain.model.UpdateFoodParams
import com.chonkcheck.android.domain.repository.FoodRepository
import com.chonkcheck.android.domain.usecase.BarcodeResult
import com.chonkcheck.android.domain.usecase.CreateUserFoodUseCase
import com.chonkcheck.android.domain.usecase.GetFoodByIdUseCase
import com.chonkcheck.android.domain.usecase.LookupBarcodeUseCase
import com.chonkcheck.android.domain.usecase.UpdateUserFoodUseCase
import com.chonkcheck.android.presentation.navigation.NavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodFormState(
    val name: String = "",
    val brand: String = "",
    val barcode: String = "",
    val servingSize: String = "100",
    val servingUnit: ServingUnit = ServingUnit.GRAM,
    val calories: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val fiber: String = "",
    val sugar: String = "",
    val sodium: String = ""
)

data class FoodFormUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isRequestingPromotion: Boolean = false,
    val isLookingUpBarcode: Boolean = false,
    val scannedFromOpenFoodFacts: Boolean = false,
    val mode: FoodFormMode = FoodFormMode.Create,
    val food: Food? = null,
    val formState: FoodFormState = FoodFormState(),
    val originalFormState: FoodFormState? = null,
    val error: String? = null,
    val showUnsavedChangesDialog: Boolean = false
)

sealed class FoodFormMode {
    data object Create : FoodFormMode()
    data class Edit(val foodId: String, val isReadOnly: Boolean = false) : FoodFormMode()
}

sealed class FoodFormEvent {
    data object NavigateBack : FoodFormEvent()
    data object FoodSaved : FoodFormEvent()
    data class ShowError(val message: String) : FoodFormEvent()
    data class NavigateToCreateOverride(val foodId: String) : FoodFormEvent()
    data object NavigateToBarcodeScanner : FoodFormEvent()
    data object NavigateToLabelScanner : FoodFormEvent()
}

@HiltViewModel
class FoodFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getFoodByIdUseCase: GetFoodByIdUseCase,
    private val createUserFoodUseCase: CreateUserFoodUseCase,
    private val updateUserFoodUseCase: UpdateUserFoodUseCase,
    private val lookupBarcodeUseCase: LookupBarcodeUseCase,
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val foodId: String? = savedStateHandle[NavArgs.FOOD_ID]

    private val _uiState = MutableStateFlow(FoodFormUiState())
    val uiState: StateFlow<FoodFormUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<FoodFormEvent?>(null)
    val events: StateFlow<FoodFormEvent?> = _events.asStateFlow()

    init {
        if (foodId != null) {
            loadFood(foodId)
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    mode = FoodFormMode.Create
                )
            }
        }
    }

    private fun loadFood(id: String) {
        viewModelScope.launch {
            val food = getFoodByIdUseCase(id).first()
            if (food == null) {
                _events.value = FoodFormEvent.ShowError("Food not found")
                _events.value = FoodFormEvent.NavigateBack
                return@launch
            }

            val formState = food.toFormState()
            val isReadOnly = food.type == FoodType.PLATFORM

            _uiState.update {
                it.copy(
                    isLoading = false,
                    mode = FoodFormMode.Edit(id, isReadOnly),
                    food = food,
                    formState = formState,
                    originalFormState = formState
                )
            }
        }
    }

    fun updateName(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(name = value)) }
    }

    fun updateBrand(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(brand = value)) }
    }

    fun updateBarcode(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(barcode = value)) }
    }

    fun updateServingSize(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(servingSize = value)) }
    }

    fun updateServingUnit(value: ServingUnit) {
        _uiState.update { it.copy(formState = it.formState.copy(servingUnit = value)) }
    }

    fun updateCalories(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(calories = value)) }
    }

    fun updateProtein(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(protein = value)) }
    }

    fun updateCarbs(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(carbs = value)) }
    }

    fun updateFat(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(fat = value)) }
    }

    fun updateFiber(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(fiber = value)) }
    }

    fun updateSugar(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(sugar = value)) }
    }

    fun updateSodium(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(sodium = value)) }
    }

    fun hasUnsavedChanges(): Boolean {
        val state = _uiState.value
        val mode = state.mode
        return when (mode) {
            is FoodFormMode.Create -> {
                state.formState != FoodFormState()
            }
            is FoodFormMode.Edit -> {
                !mode.isReadOnly && state.formState != state.originalFormState
            }
        }
    }

    fun onBackPressed() {
        if (hasUnsavedChanges()) {
            _uiState.update { it.copy(showUnsavedChangesDialog = true) }
        } else {
            _events.value = FoodFormEvent.NavigateBack
        }
    }

    fun dismissUnsavedChangesDialog() {
        _uiState.update { it.copy(showUnsavedChangesDialog = false) }
    }

    fun discardChanges() {
        _uiState.update { it.copy(showUnsavedChangesDialog = false) }
        _events.value = FoodFormEvent.NavigateBack
    }

    fun saveFood() {
        val state = _uiState.value
        val formState = state.formState

        // Validate required fields
        if (formState.name.isBlank()) {
            _uiState.update { it.copy(error = "Food name is required") }
            return
        }
        val servingSize = formState.servingSize.toDoubleOrNull()
        if (servingSize == null || servingSize <= 0) {
            _uiState.update { it.copy(error = "Serving size must be a positive number") }
            return
        }
        val calories = formState.calories.toDoubleOrNull()
        if (calories == null || calories < 0) {
            _uiState.update { it.copy(error = "Calories must be a non-negative number") }
            return
        }
        val protein = formState.protein.toDoubleOrNull()
        if (protein == null || protein < 0) {
            _uiState.update { it.copy(error = "Protein must be a non-negative number") }
            return
        }
        val carbs = formState.carbs.toDoubleOrNull()
        if (carbs == null || carbs < 0) {
            _uiState.update { it.copy(error = "Carbs must be a non-negative number") }
            return
        }
        val fat = formState.fat.toDoubleOrNull()
        if (fat == null || fat < 0) {
            _uiState.update { it.copy(error = "Fat must be a non-negative number") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val result = when (val mode = state.mode) {
                is FoodFormMode.Create -> {
                    createUserFoodUseCase(
                        CreateFoodParams(
                            name = formState.name.trim(),
                            brand = formState.brand.takeIf { it.isNotBlank() }?.trim(),
                            barcode = formState.barcode.takeIf { it.isNotBlank() }?.trim(),
                            servingSize = servingSize,
                            servingUnit = formState.servingUnit,
                            servingsPerContainer = null,
                            calories = calories,
                            protein = protein,
                            carbs = carbs,
                            fat = fat,
                            fiber = formState.fiber.toDoubleOrNull(),
                            sugar = formState.sugar.toDoubleOrNull(),
                            sodium = formState.sodium.toDoubleOrNull(),
                            saturatedFat = null,
                            transFat = null,
                            cholesterol = null,
                            addedSugar = null,
                            imageUrl = null
                        )
                    )
                }
                is FoodFormMode.Edit -> {
                    updateUserFoodUseCase(
                        mode.foodId,
                        UpdateFoodParams(
                            name = formState.name.trim(),
                            brand = formState.brand.takeIf { it.isNotBlank() }?.trim(),
                            barcode = formState.barcode.takeIf { it.isNotBlank() }?.trim(),
                            servingSize = servingSize,
                            servingUnit = formState.servingUnit,
                            servingsPerContainer = null,
                            calories = calories,
                            protein = protein,
                            carbs = carbs,
                            fat = fat,
                            fiber = formState.fiber.toDoubleOrNull(),
                            sugar = formState.sugar.toDoubleOrNull(),
                            sodium = formState.sodium.toDoubleOrNull(),
                            saturatedFat = null,
                            transFat = null,
                            cholesterol = null,
                            addedSugar = null,
                            imageUrl = null
                        )
                    )
                }
            }

            result.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                _events.value = FoodFormEvent.FoodSaved
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = error.message ?: "Failed to save food"
                    )
                }
            }
        }
    }

    fun onBarcodeScanned(barcode: String) {
        if (barcode.isBlank()) return

        _uiState.update { it.copy(isLookingUpBarcode = true, error = null, scannedFromOpenFoodFacts = false) }

        viewModelScope.launch {
            when (val result = lookupBarcodeUseCase(barcode)) {
                is BarcodeResult.Found -> {
                    val food = result.food
                    _uiState.update {
                        it.copy(
                            isLookingUpBarcode = false,
                            scannedFromOpenFoodFacts = true,
                            formState = food.toFormState()
                        )
                    }
                }
                is BarcodeResult.NotFound -> {
                    // Just populate the barcode field for manual entry
                    _uiState.update {
                        it.copy(
                            isLookingUpBarcode = false,
                            formState = it.formState.copy(barcode = barcode)
                        )
                    }
                }
                is BarcodeResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLookingUpBarcode = false,
                            error = "Failed to lookup barcode. Please enter details manually.",
                            formState = it.formState.copy(barcode = barcode)
                        )
                    }
                }
            }
        }
    }

    fun onNutritionLabelScanned(
        name: String?,
        brand: String?,
        servingSize: Double?,
        servingUnit: String?,
        calories: Double?,
        protein: Double?,
        carbs: Double?,
        fat: Double?,
        fiber: Double?,
        sugar: Double?,
        sodium: Double?
    ) {
        val currentFormState = _uiState.value.formState
        val unit = servingUnit?.let { unitStr ->
            ServingUnit.entries.find { it.name.equals(unitStr, ignoreCase = true) }
        }

        _uiState.update {
            it.copy(
                formState = currentFormState.copy(
                    name = name ?: currentFormState.name,
                    brand = brand ?: currentFormState.brand,
                    servingSize = servingSize?.formatForInput() ?: currentFormState.servingSize,
                    servingUnit = unit ?: currentFormState.servingUnit,
                    calories = calories?.formatForInput() ?: currentFormState.calories,
                    protein = protein?.formatForInput() ?: currentFormState.protein,
                    carbs = carbs?.formatForInput() ?: currentFormState.carbs,
                    fat = fat?.formatForInput() ?: currentFormState.fat,
                    fiber = fiber?.formatForInput() ?: "",
                    sugar = sugar?.formatForInput() ?: "",
                    sodium = sodium?.formatForInput() ?: ""
                )
            )
        }
    }

    fun requestPromotion() {
        val mode = _uiState.value.mode
        if (mode !is FoodFormMode.Edit) return

        _uiState.update { it.copy(isRequestingPromotion = true, error = null) }

        viewModelScope.launch {
            foodRepository.promoteFood(mode.foodId)
                .onSuccess {
                    // Reload the food to get updated state
                    loadFood(mode.foodId)
                    _uiState.update { it.copy(isRequestingPromotion = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isRequestingPromotion = false,
                            error = error.message ?: "Failed to request promotion"
                        )
                    }
                }
        }
    }

    fun createOverride() {
        val food = _uiState.value.food ?: return
        _events.value = FoodFormEvent.NavigateToCreateOverride(food.id)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onEventConsumed() {
        _events.value = null
    }

    private fun Food.toFormState(): FoodFormState {
        return FoodFormState(
            name = name,
            brand = brand ?: "",
            barcode = barcode ?: "",
            servingSize = servingSize.formatForInput(),
            servingUnit = servingUnit,
            calories = calories.formatForInput(),
            protein = protein.formatForInput(),
            carbs = carbs.formatForInput(),
            fat = fat.formatForInput(),
            fiber = fiber?.formatForInput() ?: "",
            sugar = sugar?.formatForInput() ?: "",
            sodium = sodium?.formatForInput() ?: ""
        )
    }

    private fun Double.formatForInput(): String {
        return if (this == this.toLong().toDouble()) {
            this.toLong().toString()
        } else {
            this.toString()
        }
    }
}
