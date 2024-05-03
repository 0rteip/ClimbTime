package com.example.climbtime.ui.screen.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.climbtime.R
import com.example.climbtime.data.model.place.Route
import com.example.climbtime.ui.component.largeDP

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(navController: NavHostController, route: Route) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = route.name) }, navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        })
    }) { pd ->
        val info = listOf(
            Pair(stringResource(id = R.string.grade), route.grade),
            Pair(stringResource(id = R.string.length), route.length.toString() + "m"),
            Pair(stringResource(id = R.string.bolts), route.bolts.toString()),
            Pair(stringResource(id = R.string.style), route.style),
            Pair(
                stringResource(id = R.string.anchor),
                if (route.anchor) stringResource(id = R.string.yes) else stringResource(id = R.string.no)
            ),
            Pair(stringResource(id = R.string.status), route.status)
        )

        LazyColumn(
            modifier = Modifier
                .padding(pd)
                .padding(horizontal = largeDP),
            verticalArrangement = Arrangement.spacedBy(largeDP)
        ) {
            item {
                Text(text = route.description)
            }

            items(info.chunked(2)) { inf ->
                RowInfo(info1 = inf[0], info2 = inf[1])
            }
        }
    }
}