package com.chonkcheck.android.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.chonkcheck.android.R
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
    @DrawableRes val iconRes: Int,
    val activeColorLight: Color,
    val activeColorDark: Color
) {
    data object Dashboard : BottomNavItem(
        screen = Screen.Dashboard,
        label = "Home",
        iconRes = R.drawable.ic_nav_home,
        activeColorLight = Amber,
        activeColorDark = AmberLight
    )

    data object Diary : BottomNavItem(
        screen = Screen.Diary,
        label = "Diary",
        iconRes = R.drawable.ic_nav_diary,
        activeColorLight = ChonkGreen,
        activeColorDark = ChonkGreenLight
    )

    data object Weight : BottomNavItem(
        screen = Screen.Weight,
        label = "Weight",
        iconRes = R.drawable.ic_nav_weight,
        activeColorLight = Purple,
        activeColorDark = PurpleLight
    )

    data object Foods : BottomNavItem(
        screen = Screen.Foods,
        label = "Foods",
        iconRes = R.drawable.ic_nav_foods,
        activeColorLight = Coral,
        activeColorDark = CoralLight
    )

    data object Settings : BottomNavItem(
        screen = Screen.Settings,
        label = "Settings",
        iconRes = R.drawable.ic_nav_settings,
        activeColorLight = Teal,
        activeColorDark = TealLight
    )
}
