package com.chonkcheck.android.presentation.ui.foods.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.chonkcheck.android.presentation.ui.components.DebouncedSearchBar
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.Coral

@Composable
fun FoodSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search foods...",
    accentColor: Color = Coral,
    modifier: Modifier = Modifier,
    debounceMillis: Long = 300L
) {
    DebouncedSearchBar(
        query = query,
        onQueryChange = onQueryChange,
        placeholder = placeholder,
        accentColor = accentColor,
        modifier = modifier,
        debounceMillis = debounceMillis
    )
}

@Preview(showBackground = true)
@Composable
private fun FoodSearchBarEmptyPreview() {
    ChonkCheckTheme {
        FoodSearchBar(
            query = "",
            onQueryChange = {},
            placeholder = "Search foods..."
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FoodSearchBarWithQueryPreview() {
    ChonkCheckTheme {
        FoodSearchBar(
            query = "Chicken",
            onQueryChange = {},
            placeholder = "Search foods..."
        )
    }
}
