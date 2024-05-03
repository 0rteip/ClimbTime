package com.example.climbtime.ui.screen

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.climbtime.R
import com.example.climbtime.data.model.FilterItem
import com.example.climbtime.data.model.place.Bouldering
import com.example.climbtime.data.model.place.Crag
import com.example.climbtime.data.model.place.Indoor
import com.example.climbtime.data.model.place.Multipitch
import com.example.climbtime.data.model.place.Place
import com.example.climbtime.data.model.place.ViaFerrata
import com.example.climbtime.ui.ClimbTimeRoute
import com.example.climbtime.ui.component.largeDP
import com.example.climbtime.ui.component.mediumDP
import com.example.climbtime.ui.component.smallDP
import com.example.climbtime.ui.screen.location.BoulderingScreen
import com.example.climbtime.ui.screen.location.CragScreen
import com.example.climbtime.ui.screen.location.IndoorScreen
import com.example.climbtime.ui.screen.location.MultipitchScreen
import com.example.climbtime.ui.screen.location.ViaFerrataScreen
import com.example.climbtime.ui.viewmodel.UserActions
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    navController: NavHostController,
    place: Place,
    user: MutableState<FirebaseUser?>,
    savedLocation: List<String>,
    userActions: UserActions
) {
    val context = LocalContext.current
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val route by rememberSaveable {
        mutableStateOf(
            ClimbTimeRoute.Location.buildRoute(
                place.id, place.type, place.name
            )
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

    if (showDialog) {
        UserNotLoggedDialog(onDismiss = { showDialog = false }, onDismissButton = {
            Toast.makeText(
                context, context.getText(R.string.location_not_saved), Toast.LENGTH_SHORT
            ).show()
            showDialog = false
        }, navController = navController
        )
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = place.name, style = MaterialTheme.typography.titleLarge)
        }, navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }, actions = {
            IconButton(onClick = {
                if (shareIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(shareIntent)
                } else {
                    Log.e("LocationScreen", "No app found to share location")
                }
            }) {
                Icon(Icons.Filled.Share, contentDescription = "Save location")
            }
            IconButton(onClick = {
                if (user.value == null) {
                    showDialog = true
                } else {
                    if (route !in savedLocation) {
                        userActions.saveLocation(route)
                        Toast.makeText(
                            context, context.getText(R.string.location_saved), Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        userActions.removeLocation(route)
                        Toast.makeText(
                            context, context.getText(R.string.location_removed), Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }) {
                val icon = if (route in savedLocation) {
                    Icons.Filled.Star
                } else {
                    Icons.Filled.StarBorder
                }
                Icon(icon, contentDescription = "Save location")
            }
        })
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(largeDP),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = FilterItem.items.find { it.filterName == place.type }?.nameResourcesId?.let {
                stringResource(id = it)
            } ?: place.type,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = largeDP)
                    .padding(top = smallDP),
                textAlign = TextAlign.Start)
            when (place) {
                is Bouldering -> BoulderingScreen(place)
                is Crag -> CragScreen(place, navController)
                is Indoor -> IndoorScreen(place)
                is Multipitch -> MultipitchScreen(place)
                is ViaFerrata -> ViaFerrataScreen(place)
            }

        }
    }
}

@Composable
fun UserNotLoggedDialog(
    navController: NavHostController, onDismiss: () -> Unit, onDismissButton: () -> Unit
) {
    AlertDialog(onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.required_login)) },
        text = { Text(stringResource(id = R.string.please_login)) },
        confirmButton = {
            Button(onClick = {
                navController.navigate(ClimbTimeRoute.Profile.route)
                onDismiss()
            }) {
                Text(stringResource(id = R.string.login))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismissButton() },
            ) {
                Text(stringResource(id = R.string.cancel))
            }
        })
}

