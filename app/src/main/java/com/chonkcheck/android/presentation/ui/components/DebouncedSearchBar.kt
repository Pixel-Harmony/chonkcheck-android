package com.chonkcheck.android.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Coral
import com.chonkcheck.android.ui.theme.Purple
import kotlinx.coroutines.delay

/**
 * A reusable search bar component with built-in debouncing.
 *
 * @param query The current search query string
 * @param onQueryChange Callback when the query changes (after debounce)
 * @param placeholder Placeholder text shown when the field is empty
 * @param accentColor Color used for the focused border and search icon when active
 * @param modifier Modifier for the search bar
 * @param debounceMillis Debounce delay in milliseconds (default 300ms)
 */
@Composable
fun DebouncedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    debounceMillis: Long = 300L
) {
    val focusManager = LocalFocusManager.current
    var localQuery by remember { mutableStateOf(query) }

    // Debounce the query changes
    LaunchedEffect(localQuery) {
        if (localQuery != query) {
            if (localQuery.isEmpty()) {
                // Immediate callback for clearing
                onQueryChange(localQuery)
            } else {
                delay(debounceMillis)
                onQueryChange(localQuery)
            }
        }
    }

    // Sync external changes
    LaunchedEffect(query) {
        if (query != localQuery) {
            localQuery = query
        }
    }

    OutlinedTextField(
        value = localQuery,
        onValueChange = { localQuery = it },
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = if (localQuery.isNotEmpty()) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (localQuery.isNotEmpty()) {
                IconButton(
                    onClick = {
                        localQuery = ""
                        onQueryChange("")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                focusManager.clearFocus()
            }
        ),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun DebouncedSearchBarEmptyPreview() {
    ChonkCheckTheme {
        DebouncedSearchBar(
            query = "",
            onQueryChange = {},
            placeholder = "Search foods...",
            accentColor = Coral
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DebouncedSearchBarWithQueryPreview() {
    ChonkCheckTheme {
        DebouncedSearchBar(
            query = "Chicken",
            onQueryChange = {},
            placeholder = "Search foods...",
            accentColor = Coral
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DebouncedSearchBarRecipePreview() {
    ChonkCheckTheme {
        DebouncedSearchBar(
            query = "",
            onQueryChange = {},
            placeholder = "Search recipes...",
            accentColor = ChonkGreen
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DebouncedSearchBarMealPreview() {
    ChonkCheckTheme {
        DebouncedSearchBar(
            query = "",
            onQueryChange = {},
            placeholder = "Search meals...",
            accentColor = Purple
        )
    }
}
