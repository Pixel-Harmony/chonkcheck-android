package com.chonkcheck.android.presentation.ui.recipes.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.chonkcheck.android.presentation.ui.components.DebouncedSearchBar
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen

@Composable
fun RecipeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search recipes...",
    debounceMillis: Long = 300L
) {
    DebouncedSearchBar(
        query = query,
        onQueryChange = onQueryChange,
        placeholder = placeholder,
        accentColor = ChonkGreen,
        modifier = modifier,
        debounceMillis = debounceMillis
    )
}

@Preview(showBackground = true)
@Composable
private fun RecipeSearchBarEmptyPreview() {
    ChonkCheckTheme {
        RecipeSearchBar(
            query = "",
            onQueryChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecipeSearchBarWithQueryPreview() {
    ChonkCheckTheme {
        RecipeSearchBar(
            query = "Chicken",
            onQueryChange = {}
        )
    }
}
