package com.chonkcheck.android.presentation.ui.diary.addentry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.CreateDiaryEntryParams
import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.model.FoodFilter
import com.chonkcheck.android.domain.model.FoodFilterType
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.model.Recipe
import com.chonkcheck.android.domain.model.SavedMeal
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.domain.usecase.CreateDiaryEntryUseCase
import com.chonkcheck.android.domain.usecase.SearchFoodsUseCase
import com.chonkcheck.android.domain.usecase.SearchRecipesUseCase
import com.chonkcheck.android.domain.usecase.SearchSavedMealsUseCase
import com.chonkcheck.android.presentation.navigation.NavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

enum class AddEntryPhase {
    SEARCH,
    DETAILS,
    SAVING
}

data class AddDiaryEntryUiState(
    val date: LocalDate = LocalDate.now(),
    val mealType: MealType = MealType.BREAKFAST,
    val phase: AddEntryPhase = AddEntryPhase.SEARCH,

    // Search phase
    val searchQuery: String = "",
    val searchResults: List<Food> = emptyList(),
    val recipeResults: List<Recipe> = emptyList(),
    val savedMealResults: List<SavedMeal> = emptyList(),
    val isSearching: Boolean = false,
    val recentFoods: List<Food> = emptyList(),
    val recentRecipes: List<Recipe> = emptyList(),
    val recentMeals: List<SavedMeal> = emptyList(),

    // Details phase
    val selectedFood: Food? = null,
    val selectedRecipe: Recipe? = null,
    val servingSize: Double = 0.0,
    val servingSizeText: String = "",
    val servingUnit: ServingUnit = ServingUnit.GRAM,
    val numberOfServings: Double = 1.0,
    val numberOfServingsText: String = "1",

    // Calculated values
    val calculatedCalories: Double = 0.0,
    val calculatedProtein: Double = 0.0,
    val calculatedCarbs: Double = 0.0,
    val calculatedFat: Double = 0.0,

    // Error handling
    val error: String? = null,
    val isSaving: Boolean = false
)

sealed class AddDiaryEntryEvent {
    data object EntrySaved : AddDiaryEntryEvent()
    data object NavigateBack : AddDiaryEntryEvent()
    data class NavigateToMealPreview(val savedMealId: String, val date: String, val mealType: String) : AddDiaryEntryEvent()
    data class ShowError(val message: String) : AddDiaryEntryEvent()
}

@HiltViewModel
class AddDiaryEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val searchFoodsUseCase: SearchFoodsUseCase,
    private val searchRecipesUseCase: SearchRecipesUseCase,
    private val searchSavedMealsUseCase: SearchSavedMealsUseCase,
    private val createDiaryEntryUseCase: CreateDiaryEntryUseCase
) : ViewModel() {

    private val dateString: String = savedStateHandle.get<String>(NavArgs.DATE) ?: LocalDate.now().toString()
    private val mealTypeString: String = savedStateHandle.get<String>("mealType") ?: MealType.BREAKFAST.apiValue

    private val _uiState = MutableStateFlow(AddDiaryEntryUiState(
        date = LocalDate.parse(dateString),
        mealType = MealType.fromApiValue(mealTypeString)
    ))
    val uiState: StateFlow<AddDiaryEntryUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<AddDiaryEntryEvent?>(null)
    val events: StateFlow<AddDiaryEntryEvent?> = _events.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadRecentItems()
    }

    private fun loadRecentItems() {
        // Load recent foods
        searchFoodsUseCase(FoodFilter(query = "", type = FoodFilterType.ALL, limit = 10, includeRecipes = false))
            .onEach { foods ->
                _uiState.update { it.copy(recentFoods = foods) }
            }
            .catch { /* Ignore errors for recent foods */ }
            .launchIn(viewModelScope)

        // Load recent recipes
        searchRecipesUseCase("", limit = 10)
            .onEach { recipes ->
                _uiState.update { it.copy(recentRecipes = recipes) }
            }
            .catch { /* Ignore errors for recent recipes */ }
            .launchIn(viewModelScope)

        // Load recent saved meals
        searchSavedMealsUseCase("", limit = 5)
            .onEach { meals ->
                _uiState.update { it.copy(recentMeals = meals) }
            }
            .catch { /* Ignore errors for recent meals */ }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        // Debounce search
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.update { it.copy(searchResults = emptyList(), recipeResults = emptyList(), savedMealResults = emptyList(), isSearching = false) }
                return@launch
            }

            delay(300) // Debounce
            _uiState.update { it.copy(isSearching = true) }

            // Search foods
            searchFoodsUseCase(FoodFilter(query = query, type = FoodFilterType.ALL, limit = 50, includeRecipes = false))
                .onEach { foods ->
                    _uiState.update { it.copy(searchResults = foods, isSearching = false) }
                }
                .catch { error ->
                    _uiState.update { it.copy(isSearching = false, error = error.message) }
                }
                .launchIn(this)

            // Search recipes
            searchRecipesUseCase(query, limit = 20)
                .onEach { recipes ->
                    _uiState.update { it.copy(recipeResults = recipes) }
                }
                .catch { /* Ignore recipe search errors */ }
                .launchIn(this)

            // Search saved meals
            searchSavedMealsUseCase(query, limit = 20)
                .onEach { meals ->
                    _uiState.update { it.copy(savedMealResults = meals) }
                }
                .catch { /* Ignore meal search errors */ }
                .launchIn(this)
        }
    }

    fun onFoodSelected(food: Food) {
        _uiState.update {
            it.copy(
                phase = AddEntryPhase.DETAILS,
                selectedFood = food,
                selectedRecipe = null,
                servingSize = food.servingSize,
                servingSizeText = food.servingSize.formatForInput(),
                servingUnit = food.servingUnit,
                numberOfServings = 1.0,
                numberOfServingsText = "1"
            )
        }
        recalculateNutrition()
    }

    fun onRecipeSelected(recipe: Recipe) {
        _uiState.update {
            it.copy(
                phase = AddEntryPhase.DETAILS,
                selectedFood = null,
                selectedRecipe = recipe,
                servingSize = 1.0,
                servingSizeText = "1",
                servingUnit = ServingUnit.SERVING,
                numberOfServings = 1.0,
                numberOfServingsText = "1"
            )
        }
        recalculateNutrition()
    }

    private fun Double.formatForInput(): String {
        return if (this == this.toLong().toDouble()) {
            this.toLong().toString()
        } else {
            String.format("%.1f", this)
        }
    }

    fun onMealSelected(meal: SavedMeal) {
        val state = _uiState.value
        _events.value = AddDiaryEntryEvent.NavigateToMealPreview(
            savedMealId = meal.id,
            date = state.date.toString(),
            mealType = state.mealType.apiValue
        )
    }

    fun onServingSizeChange(size: String) {
        val sizeValue = size.toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(servingSizeText = size, servingSize = sizeValue) }
        recalculateNutrition()
    }

    fun onServingUnitChange(unit: ServingUnit) {
        _uiState.update { it.copy(servingUnit = unit) }
    }

    fun onNumberOfServingsChange(servings: String) {
        val servingsValue = servings.toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(numberOfServingsText = servings, numberOfServings = servingsValue) }
        recalculateNutrition()
    }

    fun onMealTypeChange(mealType: MealType) {
        _uiState.update { it.copy(mealType = mealType) }
    }

    private fun recalculateNutrition() {
        val state = _uiState.value

        if (state.selectedFood != null) {
            val food = state.selectedFood
            val multiplier = (state.servingSize / food.servingSize) * state.numberOfServings

            _uiState.update {
                it.copy(
                    calculatedCalories = food.calories * multiplier,
                    calculatedProtein = food.protein * multiplier,
                    calculatedCarbs = food.carbs * multiplier,
                    calculatedFat = food.fat * multiplier
                )
            }
        } else if (state.selectedRecipe != null) {
            val recipe = state.selectedRecipe
            // For recipes, servingSize represents number of servings
            val multiplier = state.servingSize * state.numberOfServings

            _uiState.update {
                it.copy(
                    calculatedCalories = recipe.caloriesPerServing * multiplier,
                    calculatedProtein = recipe.proteinPerServing * multiplier,
                    calculatedCarbs = recipe.carbsPerServing * multiplier,
                    calculatedFat = recipe.fatPerServing * multiplier
                )
            }
        }
    }

    fun onBackToSearch() {
        _uiState.update {
            it.copy(
                phase = AddEntryPhase.SEARCH,
                selectedFood = null,
                selectedRecipe = null
            )
        }
    }

    fun onSave() {
        val state = _uiState.value

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val params = if (state.selectedFood != null) {
                val food = state.selectedFood
                CreateDiaryEntryParams(
                    date = state.date,
                    mealType = state.mealType,
                    foodId = food.id,
                    recipeId = null,
                    servingSize = state.servingSize,
                    servingUnit = state.servingUnit,
                    numberOfServings = state.numberOfServings,
                    foodServingSize = food.servingSize
                )
            } else if (state.selectedRecipe != null) {
                val recipe = state.selectedRecipe
                CreateDiaryEntryParams(
                    date = state.date,
                    mealType = state.mealType,
                    foodId = null,
                    recipeId = recipe.id,
                    servingSize = state.servingSize,
                    servingUnit = ServingUnit.SERVING,
                    numberOfServings = state.numberOfServings,
                    foodServingSize = 1.0 // Recipe serving size is always 1 serving
                )
            } else {
                _uiState.update { it.copy(isSaving = false) }
                return@launch
            }

            createDiaryEntryUseCase(params)
                .onSuccess {
                    _events.value = AddDiaryEntryEvent.EntrySaved
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false) }
                    _events.value = AddDiaryEntryEvent.ShowError(
                        error.message ?: "Failed to save entry"
                    )
                }
        }
    }

    fun onCancel() {
        _events.value = AddDiaryEntryEvent.NavigateBack
    }

    fun onEventConsumed() {
        _events.value = null
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
