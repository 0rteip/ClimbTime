package com.example.climbtime.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.climbtime.R
import com.example.climbtime.ui.ClimbTimeRoute

sealed class BottomNavItem(
    val titleResourceId: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val route: String
) {

    data object Map :
        BottomNavItem(
            R.string.map,
            Icons.Outlined.Map,
            Icons.Filled.Map,
            ClimbTimeRoute.Home.route
        )

    data object Saved :
        BottomNavItem(
            R.string.saved,
            Icons.Outlined.StarOutline,
            Icons.Filled.Star,
            ClimbTimeRoute.Saved.route
        )

    data object Profile :
        BottomNavItem(
            R.string.profile,
            Icons.Outlined.Person,
            Icons.Filled.Person,
            ClimbTimeRoute.Profile.route
        )

    companion object {
        val items = listOf(Map, Saved, Profile)
    }
}