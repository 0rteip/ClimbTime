package com.example.climbtime.ui.component

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.example.climbtime.data.model.BottomNavItem
import com.example.climbtime.ui.ClimbTimeRoute


@Composable
fun ClimbTimeNavBar(navController: NavHostController, currentRoute: ClimbTimeRoute) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = BottomNavItem.items

    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            is ClimbTimeRoute.Home -> {
                selectedItem = 0
            }

            is ClimbTimeRoute.Saved -> {
                selectedItem = 1
            }

            is ClimbTimeRoute.Profile -> {
                selectedItem = 2
            }

            else -> {}
        }
    }

    NavigationBar(
        modifier = Modifier.padding()
    ) {
        items.forEachIndexed { index, item ->
            val icon = if (selectedItem == index) item.selectedIcon else item.icon
            NavigationBarItem(icon = {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(id = item.titleResourceId)
                )
            },
                label = {
                    Text(text = stringResource(id = item.titleResourceId))
                },
                selected = selectedItem == index,
                onClick = {
                    navController.navigate(item.route)
                })
        }
    }
}
