package com.chonkcheck.android.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.chonkcheck.android.ui.theme.Amber
import com.chonkcheck.android.ui.theme.AmberLight
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.ChonkGreenLight
import com.chonkcheck.android.ui.theme.Coral
import com.chonkcheck.android.ui.theme.CoralLight
import com.chonkcheck.android.ui.theme.Purple
import com.chonkcheck.android.ui.theme.PurpleLight
import com.chonkcheck.android.ui.theme.Teal
import com.chonkcheck.android.ui.theme.TealLight

sealed class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val activeColorLight: Color,
    val activeColorDark: Color
) {
    data object Dashboard : BottomNavItem(
        screen = Screen.Dashboard,
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        activeColorLight = Amber,
        activeColorDark = AmberLight
    )

    data object Diary : BottomNavItem(
        screen = Screen.Diary,
        label = "Diary",
        selectedIcon = Icons.Filled.Checklist,
        unselectedIcon = Icons.Outlined.Checklist,
        activeColorLight = ChonkGreen,
        activeColorDark = ChonkGreenLight
    )

    data object Weight : BottomNavItem(
        screen = Screen.Weight,
        label = "Weight",
        selectedIcon = Icons.Filled.Scale,
        unselectedIcon = Icons.Outlined.Scale,
        activeColorLight = Purple,
        activeColorDark = PurpleLight
    )

    data object Foods : BottomNavItem(
        screen = Screen.Foods,
        label = "Foods",
        selectedIcon = Icons.Filled.Inventory2,
        unselectedIcon = Icons.Outlined.Inventory2,
        activeColorLight = Coral,
        activeColorDark = CoralLight
    )

    data object Settings : BottomNavItem(
        screen = Screen.Settings,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        activeColorLight = Teal,
        activeColorDark = TealLight
    )
}
