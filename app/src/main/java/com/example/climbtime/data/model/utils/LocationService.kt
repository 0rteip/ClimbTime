package com.example.climbtime.data.model.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

enum class MonitoringStatus {
    Monitoring,
    Paused,
    NotMonitoring
}

data class Location(
    val latitude: Double,
    val longitude: Double
)

class LocationService(
    private val context: Context
) {
    var coordinates: Location? by mutableStateOf(null)
        private set
    var monitoringStatus by mutableStateOf(MonitoringStatus.NotMonitoring)
        private set
    var isLocationEnabled: Boolean? by mutableStateOf(null)
        private set

    private val locationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        1000
    ).apply {
        setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
    }.build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            coordinates = Location(
                locationResult.locations.last().latitude,
                locationResult.locations.last().longitude
            )
            stopLocationRequests()
        }
    }

    fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    fun isLocationEnabled(): Boolean {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
    }


    @SuppressLint("MissingPermission")
    fun requestCurrentLocation() {
        coordinates = null

        // Check if location is enabled
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        isLocationEnabled =
            locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)

        if (isLocationEnabled == false) {
            return
        }

        // Check if location permission is granted
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            // Request location permission
            return
        }

        locationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        monitoringStatus = MonitoringStatus.Monitoring
    }

    fun pauseLocationRequests() {
        if (monitoringStatus != MonitoringStatus.Monitoring) return

        locationProviderClient.removeLocationUpdates(locationCallback)
        monitoringStatus = MonitoringStatus.Paused

    }

    fun resumeLocationRequests() {
        if (monitoringStatus != MonitoringStatus.Paused) return

        requestCurrentLocation()
        monitoringStatus = MonitoringStatus.Monitoring
    }

    fun stopLocationRequests() {
        // Stop location requests
        if (monitoringStatus == MonitoringStatus.NotMonitoring) return
        locationProviderClient.removeLocationUpdates(locationCallback)
        monitoringStatus = MonitoringStatus.NotMonitoring
    }
}