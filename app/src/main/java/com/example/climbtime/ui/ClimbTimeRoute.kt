package com.example.climbtime.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.climbtime.data.model.FilterItem
import com.example.climbtime.data.model.place.Crag
import com.example.climbtime.data.model.theme.Theme
import com.example.climbtime.data.model.utils.LocationService
import com.example.climbtime.ui.screen.HomeScreen
import com.example.climbtime.ui.screen.LocationScreen
import com.example.climbtime.ui.screen.ProfileScreen
import com.example.climbtime.ui.screen.SavedLocationsScreen
import com.example.climbtime.ui.screen.location.RouteScreen
import com.example.climbtime.ui.screen.SettingsScreen
import com.example.climbtime.ui.viewmodel.HomeViewModel
import com.example.climbtime.ui.viewmodel.SettingsState
import com.example.climbtime.ui.viewmodel.UserViewModel
import org.koin.androidx.compose.koinViewModel

sealed class ClimbTimeRoute(
    val route: String, val title: String, val arguments: List<NamedNavArgument> = emptyList()
) {
    data object Home : ClimbTimeRoute("home", "Home")

    data object Location :
        ClimbTimeRoute(
            route = "location/{locationType}/{locationId}/{locationName}",
            title = "{locationName}",
            arguments = listOf(navArgument("locationType") { type = NavType.StringType },
                navArgument("locationId") { type = NavType.StringType },
                navArgument("locationName") { type = NavType.StringType })
        ) {
        fun buildRoute(locationId: String, locationType: String, locationName: String) =
            "location/$locationType/$locationId/$locationName"
    }

    data object Route :
        ClimbTimeRoute(
            route = "location/{locationType}/{locationId}/{locationName}/{routeId}/{routeName}",
            title = "{routeName}",
            arguments = listOf(navArgument("locationType") { type = NavType.StringType },
                navArgument("locationId") { type = NavType.StringType },
                navArgument("locationName") { type = NavType.StringType },
                navArgument("routeId") { type = NavType.StringType },
                navArgument("routeName") { type = NavType.StringType })
        ) {
        fun buildRoute(
            locationId: String,
            locationType: String,
            locationName: String,
            routeId: String,
            routeName: String
        ) = "location/$locationType/$locationId/$locationName/$routeId/$routeName"
    }

    data object Saved : ClimbTimeRoute("saved", "Saved")

    data object Profile : ClimbTimeRoute("profile", "Profile")

    data object Settings : ClimbTimeRoute("settings", "Settings")

    companion object {
        val routes = listOf(Home, Location, Saved, Profile, Settings)
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    locationService: LocationService,
    requestLocationFunction: () -> Unit = {},
    onThemeChange: (Theme) -> Unit = {},
    settingsState: SettingsState,
) {
    val homeViewModel = koinViewModel<HomeViewModel>()
    val homeState by homeViewModel.state.collectAsStateWithLifecycle()

    val userViewModel = koinViewModel<UserViewModel>()
    val savedLocation = userViewModel.state.savedLocation

    NavHost(
        navController = navController,
        startDestination = ClimbTimeRoute.Home.route,
        modifier = modifier
    ) {
        with(ClimbTimeRoute.Home) {
            composable(route) {
                HomeScreen(
                    navController = navController,
                    homeState = homeState,
                    homeActions = homeViewModel.actions,
                    locationService = locationService,
                    requestLocationFunction = requestLocationFunction
                )
            }
        }

        with(ClimbTimeRoute.Saved) {
            composable(route) {
                SavedLocationsScreen(
                    navController = navController,
                    user = userViewModel.state.user,
                    savedLocation = savedLocation.collectAsState().value,
                    allPLaces = homeState.allPlaces.collectAsState().value,
                    userActions = userViewModel.actions
                )
            }
        }

        with(ClimbTimeRoute.Profile) {
            composable(route) {
                ProfileScreen(
                    userState = userViewModel.state,
                    userAction = userViewModel.actions,
                    navController = navController
                )
            }
        }

        with(ClimbTimeRoute.Settings) {
            composable(route) {
                SettingsScreen(
                    navController = navController,
                    onThemeChange = onThemeChange,
                    userEmail = userViewModel.state.user.value?.email,
                    userActions = userViewModel.actions,
                    settingsState = settingsState
                )
            }
        }

        with(ClimbTimeRoute.Location) {
            composable(route, arguments = arguments) { backStackEntry ->
//                Log.d("ClimbTimeRoute", "Location composable, backStackEntry: $backStackEntry")
                val locationType = backStackEntry.arguments?.getString("locationType")
                val locationId = backStackEntry.arguments?.getString("locationId")
                val locationName = backStackEntry.arguments?.getString("locationName")
                if (locationType == null || locationId == null || locationName == null) {
                    navController.popBackStack()
                    return@composable
                }
                val location = homeState.allPlaces.collectAsState().value.firstOrNull() {
                    it.id == locationId && it.type == locationType
                }
                if (location == null) {
                    navController.popBackStack()
                    return@composable
                }
                LocationScreen(
                    navController = navController,
                    place = location,
                    user = userViewModel.state.user,
                    userActions = userViewModel.actions,
                    savedLocation = savedLocation.collectAsStateWithLifecycle().value
                )
            }
        }

        with(ClimbTimeRoute.Route) {
            composable(route, arguments = arguments) { backStackEntry ->
//                Log.d("ClimbTimeRoute", "Route composable, backStackEntry: $backStackEntry")
                val locationType = backStackEntry.arguments?.getString("locationType")
                val locationId = backStackEntry.arguments?.getString("locationId")
                val locationName = backStackEntry.arguments?.getString("locationName")
                val routeId = backStackEntry.arguments?.getString("routeId")
                val routeName = backStackEntry.arguments?.getString("routeName")

                if (locationType == null || locationId == null || locationName == null || routeId == null || routeName == null) {
                    navController.popBackStack()
                    return@composable
                }

                val route = homeState.places.value.filter {
                    it.type == FilterItem.Crag.filterName
                }.map {
                    it as Crag
                }.firstOrNull {
                    it.routes.any { route -> route.id == routeId && route.name == routeName }
                }?.routes?.firstOrNull { route -> route.id == routeId && route.name == routeName }

                if (route == null) {
                    navController.popBackStack()
                    return@composable
                }

                RouteScreen(navController = navController, route = route)
            }
        }
    }
}