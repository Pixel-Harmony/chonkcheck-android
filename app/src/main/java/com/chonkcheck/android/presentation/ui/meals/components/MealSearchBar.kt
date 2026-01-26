package com.chonkcheck.android.presentation.ui.meals.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.chonkcheck.android.presentation.ui.components.DebouncedSearchBar
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.Purple

@Composable
fun MealSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search meals...",
    debounceMillis: Long = 300L
) {
    DebouncedSearchBar(
        query = query,
        onQueryChange = onQueryChange,
        placeholder = placeholder,
        accentColor = Purple,
        modifier = modifier,
        debounceMillis = debounceMillis
    )
}

@Preview(showBackground = true)
@Composable
private fun MealSearchBarPreview() {
    ChonkCheckTheme {
        MealSearchBar(
            query = "",
            onQueryChange = {}
        )
    }
}
