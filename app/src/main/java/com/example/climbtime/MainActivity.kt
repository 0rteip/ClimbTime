package com.example.climbtime

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.climbtime.data.model.theme.Theme
import com.example.climbtime.ui.NavGraph
import com.example.climbtime.ui.component.ClimbTimeNavBar
import com.example.climbtime.ui.theme.ClimbTimeTheme
import com.example.climbtime.data.model.utils.LocationService
import com.example.climbtime.data.model.utils.PermissionStatus
import com.example.climbtime.data.model.utils.rememberPermission
import com.example.climbtime.ui.ClimbTimeRoute
import com.example.climbtime.ui.component.LocationDisableAlert
import com.example.climbtime.ui.component.PermissionDeniedAlert
import com.example.climbtime.ui.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    private lateinit var locationService: LocationService
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var connectivityManager: ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        locationService = LocationService(this)

        setContent {
            val settingsViewModel = koinViewModel<SettingsViewModel>()
            val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()

            ClimbTimeTheme(
                dynamicColor = false, darkTheme = when (settingsState.theme) {
                    Theme.DARK -> true
                    Theme.LIGHT -> false
                    Theme.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    var onlineState by remember { mutableStateOf(false) }

                    connectivityManager =
                        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                    networkCallback = object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            runOnUiThread { onlineState = true }
                        }

                        override fun onLost(network: Network) {
                            runOnUiThread { onlineState = false }
                        }
                    }

                    connectivityManager.registerNetworkCallback(
                        NetworkRequest.Builder()
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),
                        networkCallback
                    )

                    val snackBarHostState = remember { SnackbarHostState() }
                    var showPermissionDeniedAlert by remember { mutableStateOf(false) }
                    var showPermissionPermanentlyDeniedSnackBar by remember { mutableStateOf(false) }
                    var showLocationDisabledAlert by remember { mutableStateOf(false) }
                    val context = LocalContext.current
                    val locationPermission =
                        rememberPermission(permission = Manifest.permission.ACCESS_COARSE_LOCATION) {
                            when (it) {
                                PermissionStatus.Granted -> {
                                    locationService.requestCurrentLocation()
                                }

                                PermissionStatus.Denied -> {
                                    showPermissionDeniedAlert = true
                                }

                                PermissionStatus.PermanentlyDenied -> {
                                    showPermissionPermanentlyDeniedSnackBar = true
                                }

                                else -> {}
                            }
                        }

                    fun requestLocation() {
                        if (!locationService.isLocationEnabled()) {
                            showLocationDisabledAlert = true
                            return
                        }

                        if (locationPermission.status.isGranted) {
                            locationService.requestCurrentLocation()
                        } else {
                            locationPermission.launchPermissionRequest()
                        }
                    }

                    fun openIntent(intent: Intent) {
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                    }

                    fun openWirelessSettings() {
                        openIntent(Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }

                    fun openGPSSettings() {
                        openIntent(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            data = Uri.fromParts("package", context.packageName, null)
                        })
                    }

                    LaunchedEffect(onlineState) {
                        if (!onlineState) {
                        snackBarHostState.currentSnackbarData?.dismiss()
                            val res = snackBarHostState.showSnackbar(
                                message = context.getString(R.string.no_internet_connection),
                                actionLabel = context.getString(R.string.open_settings),
                                duration = SnackbarDuration.Long
                            )
                            if (res == SnackbarResult.ActionPerformed) {
                                openWirelessSettings()
                            }
                        }
                    }


                    LaunchedEffect(locationService.isLocationEnabled) {
                        showLocationDisabledAlert = locationService.isLocationEnabled == false
                    }

                    val navController = rememberNavController()
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute by remember {
                        derivedStateOf {
                            ClimbTimeRoute.routes.find {
                                it.route == backStackEntry?.destination?.route
                            } ?: ClimbTimeRoute.Home
                        }
                    }

                    Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        bottomBar = {
                            ClimbTimeNavBar(
                                navController = navController, currentRoute = currentRoute
                            )
                        }) { paddingValues ->

                        Log.println(Log.INFO, "paddingValues", paddingValues.toString())
                        NavGraph(
                            navController = navController,
                            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
                            requestLocationFunction = { requestLocation() },
                            locationService = locationService,
                            onThemeChange = settingsViewModel::changeTheme,
                            settingsState = settingsState

                        )
                    }

                    if (showLocationDisabledAlert) {
                        LocationDisableAlert(onDismissAction = {
                            showLocationDisabledAlert = false
                        }, onClick = {
                            locationService.openLocationSettings()
                            showLocationDisabledAlert = false
                        })
                    }

                    if (showPermissionDeniedAlert) {
                        PermissionDeniedAlert(onDismissAction = {
                            showPermissionDeniedAlert = false
                        }, onClick = {
                            locationPermission.launchPermissionRequest()
                            showPermissionDeniedAlert = false
                        })
                    }

                    if (showPermissionPermanentlyDeniedSnackBar) {
                        val ctx = this
                        LaunchedEffect(key1 = snackBarHostState) {
                            snackBarHostState.currentSnackbarData?.dismiss()
                            val res = snackBarHostState.showSnackbar(
                                message = ctx.getString(R.string.location_permanently_denied),
                                actionLabel = ctx.getString(R.string.open_settings),
                                duration = SnackbarDuration.Long
                            )
                            if (res == SnackbarResult.ActionPerformed) {
                                openGPSSettings()
                            }
                            showPermissionPermanentlyDeniedSnackBar = false
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        locationService.pauseLocationRequests()
    }

    override fun onResume() {
        super.onResume()
        locationService.resumeLocationRequests()

    }

    override fun onDestroy() {
        super.onDestroy()
        locationService.stopLocationRequests()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

}

