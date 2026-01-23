package com.chonkcheck.android.presentation.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.Recipe
import com.chonkcheck.android.domain.usecase.DeleteRecipeUseCase
import com.chonkcheck.android.domain.usecase.SearchRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipesUiState(
    val recipes: List<Recipe> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val deleteConfirmation: RecipeDeleteConfirmation? = null
)

data class RecipeDeleteConfirmation(
    val recipeId: String,
    val recipeName: String
)

sealed class RecipesEvent {
    data class NavigateToEditRecipe(val recipeId: String) : RecipesEvent()
    data object NavigateToCreateRecipe : RecipesEvent()
    data class ShowError(val message: String) : RecipesEvent()
    data object RecipeDeleted : RecipesEvent()
}

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val searchRecipesUseCase: SearchRecipesUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipesUiState())
    val uiState: StateFlow<RecipesUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<RecipesEvent?>(null)
    val events: StateFlow<RecipesEvent?> = _events.asStateFlow()

    init {
        loadRecipes()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadRecipes()
    }

    fun onRecipeClick(recipeId: String) {
        _events.value = RecipesEvent.NavigateToEditRecipe(recipeId)
    }

    fun onAddRecipeClick() {
        _events.value = RecipesEvent.NavigateToCreateRecipe
    }

    fun onDeleteClick(recipe: Recipe) {
        _uiState.update {
            it.copy(
                deleteConfirmation = RecipeDeleteConfirmation(
                    recipeId = recipe.id,
                    recipeName = recipe.name
                )
            )
        }
    }

    fun onDeleteConfirm() {
        val confirmation = _uiState.value.deleteConfirmation ?: return
        _uiState.update { it.copy(deleteConfirmation = null) }

        viewModelScope.launch {
            deleteRecipeUseCase(confirmation.recipeId)
                .onSuccess {
                    _events.value = RecipesEvent.RecipeDeleted
                    loadRecipes()
                }
                .onFailure { error ->
                    _events.value = RecipesEvent.ShowError(error.message ?: "Failed to delete recipe")
                }
        }
    }

    fun onDeleteCancel() {
        _uiState.update { it.copy(deleteConfirmation = null) }
    }

    fun onEventConsumed() {
        _events.value = null
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadRecipes()
    }

    private fun loadRecipes() {
        searchRecipesUseCase(_uiState.value.searchQuery, limit = 100)
            .onEach { recipes ->
                _uiState.update {
                    it.copy(
                        recipes = recipes,
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = error.message ?: "Failed to load recipes"
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
