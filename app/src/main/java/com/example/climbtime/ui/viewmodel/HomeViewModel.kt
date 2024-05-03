package com.example.climbtime.ui.viewmodel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.climbtime.data.model.place.Place
import com.example.climbtime.data.repository.HomeRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeState(
    val filters: MutableState<List<String>> = mutableStateOf(emptyList()),
    val places: MutableState<List<Place>> = mutableStateOf(emptyList()),
    val allPlaces: MutableStateFlow<List<Place>> = MutableStateFlow(emptyList()),
    var lastPosition: StateFlow<LatLng>,
    val lastZoom: StateFlow<Float>
)

data class CameraState(
    var position: LatLng,
    var zoom: Float
)

interface HomeActions {
    fun addFilter(value: String)
    fun removeFilter(value: String)
    fun onCameraPositionChanged(newPosition: LatLng, zoom: Float)
}

class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {

    private val lastPosition = repository.lastPosition
        .map { position ->
            position.split(",").let {
                LatLng(it[0].toDouble(), it[1].toDouble())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = LatLng(0.0, 0.0)
        )

    private val lastZoom = repository.lastZoom
        .map { zoom ->
            zoom.toFloat()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = 8f
        )

    val state = repository.filters
        .map {
//            Log.d("HomeViewModel", "repository filters: $it")
            HomeState(
                filters = mutableStateOf(it.split(",")),
                places = repository.places,
                lastPosition = lastPosition,
                lastZoom = lastZoom,
                allPlaces = repository.allPlaces
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = HomeState(
                lastPosition = lastPosition,
                lastZoom = lastZoom
            )
        )

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {
        if (cameraState.position != lastPosition.value) {
            viewModelScope.launch {
                repository.updateLastPosition(cameraState.position.let {
                    listOf(it.latitude, it.longitude).joinToString(",")


                })
            }
        }

        if (cameraState.zoom != lastZoom.value) {
            viewModelScope.launch {
                repository.updateLastZoom(cameraState.zoom.toString())
            }
        }
    }

    private var cameraState = CameraState(lastPosition.value, lastZoom.value)
    val actions = object : HomeActions {
        override fun addFilter(value: String) {
//            Log.d("HomeViewModel", "addFilter: $value")
            viewModelScope.launch {
                repository.addFilter(value)
            }
        }

        override fun removeFilter(value: String) {
            viewModelScope.launch {
                repository.removeFilter(value)
            }
        }

        override fun onCameraPositionChanged(newPosition: LatLng, zoom: Float) {
            cameraState.position = newPosition
            cameraState.zoom = zoom

            // Annulla qualsiasi Runnable in sospeso
            handler.removeCallbacks(runnable)

            // Programma un nuovo Runnable per essere eseguito dopo n secondi
            handler.postDelayed(runnable, 400)
        }
    }

    init {
        viewModelScope.launch {
            repository.filters
                .distinctUntilChanged()
                .collect { filter ->
//                    Log.d("HomeViewModel", "Filters: $filter")
                    repository.updatePlaces(filter.split(","))
                }
        }
        viewModelScope.launch {
            repository.allPlaces.collect { places ->
                state.value.places.value = places
            }
        }
    }
}