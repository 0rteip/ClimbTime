package com.example.climbtime.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.example.climbtime.R
import com.example.climbtime.data.model.FilterItem
import com.example.climbtime.data.model.place.Place
import com.example.climbtime.data.model.utils.VibratorHelper
import com.example.climbtime.ui.ClimbTimeRoute
import com.example.climbtime.ui.component.extraExtraLargeDP
import com.example.climbtime.ui.component.extraLargeDP
import com.example.climbtime.ui.component.largeDP
import com.example.climbtime.ui.component.mediumDP
import com.example.climbtime.ui.component.smallDP
import com.example.climbtime.ui.viewmodel.UserActions
import com.google.firebase.auth.FirebaseUser
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedLocationsScreen(
    navController: NavHostController,
    user: MutableState<FirebaseUser?>,
    userActions: UserActions,
    savedLocation: List<String>,
    allPLaces: List<Place>,
) {
    val snackBarHostState = remember { SnackbarHostState() }
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(id = R.string.saved_locations_title)) })
    }, snackbarHost = { SnackbarHost(snackBarHostState) }) { pd ->
        if (user.value == null) {
            UserNotLoggedInScreen(navController = navController)
        } else {
            if (savedLocation.isEmpty() || savedLocation.size == 1 && savedLocation[0].isEmpty()) {
                NoSavedLocation(
                    navController = navController, modifier = Modifier.padding(pd)
                )
            } else {
                SavedLocations(
                    allPLaces = allPLaces,
                    savedLocation = savedLocation,
                    modifier = Modifier.padding(pd),
                    navController = navController,
                    snackBarHostState = snackBarHostState,
                    userActions = userActions
                )
            }
        }
    }
}

@Composable
fun UserNotLoggedInScreen(
    navController: NavHostController, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(largeDP),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(id = R.string.saved_locations), textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(extraExtraLargeDP))
        Button(onClick = { navController.navigate(ClimbTimeRoute.Profile.route) }) {
            Text(text = stringResource(id = R.string.login))
        }
    }
}

@Composable
fun NoSavedLocation(
    navController: NavHostController, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(largeDP),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(id = R.string.no_saved_locations), textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(largeDP))
        Button(onClick = { navController.navigate(ClimbTimeRoute.Home.route) }) {
            Text(text = stringResource(id = R.string.explore))
        }
    }
}

@Composable
fun SavedLocations(
    savedLocation: List<String>,
    modifier: Modifier,
    navController: NavHostController,
    allPLaces: List<Place>,
    snackBarHostState: SnackbarHostState,
    userActions: UserActions
) {
    val context = LocalContext.current

    var res by rememberSaveable {
        mutableStateOf<SnackbarResult?>(null)
    }
    var showSnackBar by rememberSaveable {
        mutableStateOf(false)
    }
    var location by rememberSaveable {
        mutableStateOf("")
    }
    LaunchedEffect(showSnackBar) {
        if (showSnackBar) {
            snackBarHostState.currentSnackbarData?.dismiss()

            res = snackBarHostState.showSnackbar(
                message = context.getString(R.string.location_removed),
                actionLabel = context.getString(R.string.undo),
                duration = SnackbarDuration.Short
            )
            if (res == SnackbarResult.ActionPerformed) {
                userActions.saveLocation(location)
            }
            showSnackBar = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = largeDP),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = largeDP)
    ) {
        items(savedLocation) { route ->
            if (route.isNotEmpty()) {
                val split = route.split("/")
                val place = allPLaces.firstOrNull { place ->
                    place.id == split[2] && place.type == split[1]
                } ?: return@items

                ItemInfoRow(place = place,
                    navController = navController,
                    route = route,
                    onDelete = {
                        userActions.removeLocation(route)
                        showSnackBar = true
                        location = route
                    })
            }
        }
    }
}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun ItemInfoRow(
    place: Place, navController: NavHostController, route: String, onDelete: () -> Unit
) {
    val context = LocalContext.current
    val swipeableState = rememberSwipeableState(SwipeState.NoAction)
    val vibrator = VibratorHelper.from(context)
    var hasVibrated by rememberSaveable { mutableStateOf(false) }
    var isDelete by rememberSaveable { mutableStateOf(false) }

    val anchors = mapOf(0f to SwipeState.NoAction,
        LocalDensity.current.run { with(LocalConfiguration.current) { screenWidthDp.dp.toPx() } } to SwipeState.ActionPerformed)

    val backgroundColor by animateColorAsState(
        calculateBackgroundColor(swipeableState.offset.value, anchors.keys.maxOrNull() ?: 0f),
        label = ""
    )

    val contentColor by animateColorAsState(
        calculateContentColor(swipeableState.offset.value, anchors.keys.maxOrNull() ?: 0f),
        label = ""
    )

    LaunchedEffect(swipeableState.offset.value) {
        val swipeValue = swipeableState.offset.value
        val maxSwipe = anchors.keys.maxOrNull() ?: 0f

        if (isDelete) {
            return@LaunchedEffect
        }

        if (!hasVibrated && swipeValue >= maxSwipe / 5) {
            vibrator?.vibrate(10)
            hasVibrated = true
        } else if (hasVibrated && swipeValue < maxSwipe / 5) {
            hasVibrated = false
        }
    }

    if (!isDelete) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize()
                .padding(bottom = largeDP)
                .clip(RoundedCornerShape(mediumDP))
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = extraLargeDP)
                    .align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "delete",
                    modifier = Modifier.size(24.dp),
                    tint = contentColor
                )
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize()
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.20f) },
                    orientation = Orientation.Horizontal
                )
                .animateContentSize()
                .offset {
                    val offset = swipeableState.offset.value.roundToInt()
                    if (offset > 0) {
                        IntOffset(offset, 0)
                    } else {
                        IntOffset(0, 0)
                    }
                }
                .clip(RoundedCornerShape(mediumDP)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                val intent = remember {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("geo:${place.location.latitude},${place.location.longitude}")
                    )
                }
                val shareIntent = remember {
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            context.getString(R.string.share_location) + "\n\n" + place.name + "\n" + "https://www.google.com/maps/search/?api=1&query=${place.location.latitude},${place.location.longitude}"
                        )
                    }
                }

                // Aggiungi un SideEffect per eseguire un'azione quando lo stato cambia
                LaunchedEffect(swipeableState.currentValue) {
                    when (swipeableState.currentValue) {
                        SwipeState.ActionPerformed -> {
                            if (!isDelete) {
                                onDelete()
                            }
                            isDelete = true
                            swipeableState.animateTo(SwipeState.NoAction)
                        }

                        SwipeState.NoAction -> {
                            isDelete = false
                        }
                    }
                }

                ItemInfoWithLocationSwipe(title = place.name,
                    value = FilterItem.items.find { filter -> filter.filterName == place.type }?.nameResourcesId?.let { id ->
                        stringResource(id = id)
                    } ?: place.type,
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.onSecondary)
                        .clickable { navController.navigate(route) },
                    intent = intent,
                    shareIntent = shareIntent
                )
            }
        }
    }
}

@Composable
fun ItemInfoWithLocationSwipe(
    title: String, value: String, modifier: Modifier = Modifier, intent: Intent, shareIntent: Intent
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(mediumDP))
    ) {
        Row(
            modifier = Modifier
                .padding(largeDP)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(2f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title, style = MaterialTheme.typography.titleMedium
                )
                Text(text = value)
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentSize(),
                horizontalArrangement = Arrangement.spacedBy(mediumDP + smallDP),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ItemInfoButton(
                    intent = shareIntent,
                    icon = Icons.Rounded.Share,
                    contentDescription = "Share",
                )
                ItemInfoButton(
                    intent = intent,
                    icon = Icons.Rounded.Directions,
                    contentDescription = "Directions",
                    alternativeColor = true
                )
            }
        }
    }
}

@Composable
fun ItemInfoButton(
    intent: Intent, icon: ImageVector, contentDescription: String, alternativeColor: Boolean = false
) {
    val context = LocalContext.current
    IconButton(
        onClick = {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        }, modifier = Modifier.background(
            color = if (alternativeColor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape
        )
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (alternativeColor) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

enum class SwipeState {
    NoAction, ActionPerformed
}

@Composable
fun calculateBackgroundColor(swipeValue: Float, maxSwipe: Float): Color {
    val color = MaterialTheme.colorScheme.errorContainer
    return when {
        swipeValue <= maxSwipe / 5 -> color.copy(alpha = 0.5f)
        else -> color
    }
}

@Composable
fun calculateContentColor(swipeValue: Float, maxSwipe: Float): Color {
    val color = MaterialTheme.colorScheme.error
    return when {
        swipeValue <= maxSwipe / 5 -> color.copy(alpha = 0.5f)
        else -> color
    }
}