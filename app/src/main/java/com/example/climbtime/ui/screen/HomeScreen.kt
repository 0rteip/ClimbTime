package com.example.climbtime.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.example.climbtime.R
import com.example.climbtime.data.model.FilterItem
import com.example.climbtime.data.model.place.Place
import com.example.climbtime.data.model.utils.LocationService
import com.example.climbtime.data.model.utils.MonitoringStatus
import com.example.climbtime.ui.ClimbTimeRoute
import com.example.climbtime.ui.component.extraLargeDP
import com.example.climbtime.ui.component.extraSmallDP
import com.example.climbtime.ui.component.largeDP
import com.example.climbtime.ui.component.mediumDP
import com.example.climbtime.ui.viewmodel.HomeActions
import com.example.climbtime.ui.viewmodel.HomeState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.abs

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    navController: NavHostController,
    homeState: HomeState,
    homeActions: HomeActions,
    locationService: LocationService,
    requestLocationFunction: () -> Unit = {},
) {
    val filters = rememberSaveable {
        mutableStateOf(FilterItem.items.associateWith {
            mutableStateOf(false)
        })
    }

    homeState.filters.value.forEach {
            if (filters.value.keys.filter { it != null }.any { f -> f.filterName == it }) {
                filters.value[FilterItem.items.filter { it != null }
                    .first { f -> f.filterName == it }]?.value = true
            }
        }

    val zoom = homeState.lastZoom.collectAsState()
    val position = homeState.lastPosition.collectAsState()

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = {
            requestLocationFunction()
        }) {
            val icon = when (locationService.monitoringStatus) {
                MonitoringStatus.Monitoring -> Icons.Filled.MyLocation
                else -> locationService.coordinates?.let {
                    if (abs(it.latitude - homeState.lastPosition.value.latitude) < 0.0001 && abs(
                            it.longitude - homeState.lastPosition.value.longitude
                        ) < 0.0001
                    ) {
                        Icons.Filled.MyLocation
                    } else {
                        Icons.Filled.LocationSearching
                    }
                } ?: Icons.Filled.LocationSearching
            }

            Icon(
                imageVector = icon, contentDescription = "Request location"
            )
        }
    }) {
        Box(modifier = Modifier
            .fillMaxSize()
            .semantics { isTraversalGroup = true }) {
            Map(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                places = homeState.places,
                lastPosition = position,
                lastZoom = zoom,
                navController = navController,
                locationService = locationService,
                homeActions = homeActions
            )


            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(extraSmallDP)
            ) {
                SearchLocation(
                    places = homeState.allPlaces, navController = navController
                )
                LazyRow(
                    contentPadding = PaddingValues(start = largeDP, end = mediumDP),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    items(filters.value.entries.toList()) {
                        // padding between filter items if not last item
                        FilterItem(
                            filterItem = it.key,
                            homeActions = homeActions,
                            isSelected = it.value,
                            modifier = Modifier.padding(end = mediumDP)
                        )
                    }
                }

            }

        }
    }
}

@Composable
fun FilterItem(
    filterItem: FilterItem,
    homeActions: HomeActions,
    isSelected: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    action: () -> Unit = {}
) {
    ElevatedFilterChip(
        modifier = modifier,
        shape = CircleShape,
        onClick = {
            if (!isSelected.value) {
                isSelected.value = true
                homeActions.addFilter(filterItem.filterName)
            } else {
                isSelected.value = false
                homeActions.removeFilter(filterItem.filterName)
            }
            action()
//            Log.d(
//                "FilterItem",
//                "filterItem: ${filterItem.filterName}, isSelected: ${isSelected.value}"
//            )
        },
        label = {
            Text(text = stringResource(id = filterItem.filterNameResourcesId))
        },
        selected = isSelected.value,
        leadingIcon = if (isSelected.value) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Done icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            {
                Icon(
                    imageVector = filterItem.icon,
                    contentDescription = "${stringResource(id = filterItem.filterNameResourcesId)} icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        },
    )
}

@Composable
fun Map(
    modifier: Modifier = Modifier,
    places: MutableState<List<Place>>,
    lastPosition: State<LatLng>,
    lastZoom: State<Float>,
    navController: NavHostController,
    locationService: LocationService,
    homeActions: HomeActions,
) {
    var currentLocation by rememberSaveable {
        mutableStateOf(lastPosition.value)
    }

    var currentZoom by rememberSaveable {
        mutableFloatStateOf(lastZoom.value)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, currentZoom)
//        Log.d(
//            "Map",
//            "camera position: $position"
//        )
    }

    val items = remember {
        mutableStateListOf<MyItem>()
    }

    LaunchedEffect(
        locationService.coordinates
    ) {
//        Log.d(
//            "map",
//            "locationService.coordinates has changed: ${locationService.coordinates}"
//        )

        currentLocation = locationService.coordinates?.let {
            LatLng(
                it.latitude, it.longitude
            )
        } ?: lastPosition.value
        currentZoom = lastZoom.value

//        Log.d(
//            "map",
//            "currentLocation is: $currentLocation, currentZoom is: $currentZoom"
//        )

        if (currentLocation.latitude != lastPosition.value.latitude && currentLocation.longitude != lastPosition.value.longitude) {

            cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation, 10f)
//            Log.d("Map", "changed camera position to: ${cameraPositionState.position.target}")
            currentLocation = cameraPositionState.position.target

            homeActions.onCameraPositionChanged(
                currentLocation, cameraPositionState.position.zoom
            )
        }
    }

    LaunchedEffect(
        cameraPositionState.position.target
    ) {
        val position = cameraPositionState.position.target
        if (position.latitude != 0.0 && position.longitude != 0.0 && position.latitude != currentLocation.latitude && position.longitude != currentLocation.longitude) {
//            Log.d(
//                "cameraPosition",
//                "camera update: $position"
//            )
            currentLocation = position
            homeActions.onCameraPositionChanged(
                position, cameraPositionState.position.zoom
            )
        }
    }

    LaunchedEffect(Unit) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation, currentZoom)
    }

    LaunchedEffect(
        places.value
    ) {
        items.clear()
        places.value.forEach {
            items.add(
                MyItem(
                    place = it, itemZIndex = 0f
                )
            )
        }
    }

    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = false,
                mapToolbarEnabled = false,
                compassEnabled = false,
            )
        )
    }


    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = mapUiSettings
    ) {
        DefaultClustering(items = items, navController = navController)
    }
}


@OptIn(MapsComposeExperimentalApi::class)
@Composable
private fun DefaultClustering(items: List<MyItem>, navController: NavHostController) {
    Clustering(items = items,
        // Optional: Handle clicks on clusters, cluster items, and cluster item info windows
        onClusterClick = {
//            Log.d(TAG, "Cluster clicked! $it")
            false
        }, onClusterItemClick = {
//            Log.d(TAG, "Cluster item clicked! $it")
            false
        }, onClusterItemInfoWindowClick = {
//            Log.d(TAG, "Cluster item info window clicked! $it")
            navController.navigate(
                ClimbTimeRoute.Location.buildRoute(
                    locationId = it.place.id,
                    locationType = it.place.type,
                    locationName = it.place.name
                )
            )
        },
        // Optional: Custom rendering for non-clustered items
        clusterItemContent = null
    )
}

data class MyItem(
    val place: Place, val itemZIndex: Float
) : ClusterItem {
    private val maxSnippetsLength = 25

    override fun getPosition() = place.location.let { location ->
        LatLng(
            location.latitude, location.longitude
        )
    }

    override fun getTitle() = place.name

    override fun getSnippet() = place.description.let { description ->
        if (description.length > maxSnippetsLength) {
            description.substring(0, maxSnippetsLength) + "..."
        } else {
            description
        }
    }

    override fun getZIndex() = itemZIndex
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchLocation(
    modifier: Modifier = Modifier,
    places: MutableStateFlow<List<Place>>,
    navController: NavHostController
) {
    var text by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }
//    val searchHistory = remember { mutableStateListOf("") }
    val allPlaces by places.collectAsState()

    SearchBar(modifier = if (active) {
        modifier
    } else {
        modifier.padding(horizontal = largeDP)
    }
        .fillMaxWidth()
        .semantics { traversalIndex = -1f },
        query = text,
        onQueryChange = { text = it },
        onSearch = {
            active = false
//            if (searchHistory.contains(text).not()) {
//                searchHistory.add(text)
//            }
        },
        active = active,
        onActiveChange = {
            active = it
        },
        placeholder = {
            Text(
                text = stringResource(id = R.string.search_hint)
            )
        },
        leadingIcon = {
            if (active) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Search location",
                    modifier = Modifier.clickable {
                        active = false
                    }
                )
            } else {
                Icon(Icons.Default.Search, contentDescription = "Search location")
            }
        },
        trailingIcon = {
            if (active && text.isNotEmpty()) {
                Icon(
                    modifier = Modifier.clickable {
                            text = ""
                        }, imageVector = Icons.Filled.Close, contentDescription = "Cancel research"
                )
            }
        }

    ) {
        LazyColumn {

//            if (active) {
//                items(searchHistory) {
//                    if (it.isNotEmpty()) {
//                        Row(
//                            modifier = Modifier
//                                .padding(all = largeDP)
//                                .fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Row(
//                                horizontalArrangement = Arrangement.spacedBy(largeDP),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Icon(
//                                    modifier = Modifier.size(2smallDp),
//                                    imageVector = Icons.Filled.History,
//                                    contentDescription = "History icon"
//                                )
//                                Text(text = it)
//                            }
//                            Icon(
//                                imageVector = Icons.Filled.Remove,
//                                contentDescription = "Remove $it from history",
//                                modifier = Modifier
//                                    .clickable {
//                                        searchHistory.remove(it)
//                                    }
//                                    .size(2smallDp)
//                            )
//                        }
//                    }
//                }
//            }

            if (active) {

                val items = when (text) {
                    "" -> {
                        allPlaces.shuffled()
                    }

                    else -> {
                        allPlaces.filter { it.name.contains(text, ignoreCase = true) }
                    }
                }
                if (items.isEmpty()) {
                    item {
                        Text(
                            modifier = Modifier
                                .padding(all = largeDP)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            text = "No results"
                        )
                    }
                } else {
                    items(items) {
                        ResultRow(navController = navController, place = it)
                        HorizontalDivider(modifier = Modifier.padding(horizontal = largeDP))
                    }
                }
            }


//            if (searchHistory.size > 1) {
//                item {
//                    Divider()
//                }
//                item {
//                    Text(
//                        modifier = Modifier
//                            .padding(all = 1smallDp)
//                            .fillMaxWidth()
//                            .clickable {
//                                searchHistory.clear()
//                            },
//                        textAlign = TextAlign.Center,
//                        fontWeight = FontWeight.Bold,
//                        text = "clear all history"
//                    )
//                }
//            }
        }
    }
}


@Composable
fun ResultRow(navController: NavHostController, place: Place) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                navController.navigate(
                    ClimbTimeRoute.Location.buildRoute(
                        locationId = place.id, locationType = place.type, locationName = place.name
                    )
                )
            }),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(largeDP),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(largeDP))
            Icon(
                modifier = Modifier

                    .size(extraLargeDP),
                imageVector = Icons.Filled.PinDrop,
                contentDescription = "Pin icon"
            )
            Text(
                text = place.name, modifier = Modifier.padding(
                    top = largeDP, bottom = largeDP, end = largeDP
                )
            )
        }
    }
}