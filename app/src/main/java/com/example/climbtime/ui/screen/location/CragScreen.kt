package com.example.climbtime.ui.screen.location

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.climbtime.R
import com.example.climbtime.data.model.place.Crag
import com.example.climbtime.data.model.place.Route
import com.example.climbtime.ui.ClimbTimeRoute
import com.example.climbtime.ui.component.largeDP
import com.example.climbtime.ui.component.smallDP
import com.example.climbtime.ui.graph.BarGraphCompose


@Composable
fun CragScreen(crag: Crag, navController: NavHostController) {
    val data by rememberSaveable {
        mutableStateOf(crag.routes.sortedBy { it.grade }.groupBy { it.grade }
            .mapValues { it.value.size })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = largeDP),
        verticalArrangement = Arrangement.spacedBy(largeDP),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                style = MaterialTheme.typography.bodyMedium,
                text = crag.description,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }
        item {
            Text(
                text = stringResource(id = R.string.rock_type) + ": " + crag.rockType,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }
        item {
            NavigateTo(place = crag)
        }
        item {
            Text(
                style = MaterialTheme.typography.titleLarge,
                text = stringResource(id = R.string.routes),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }
        item {
            BarGraphCompose(
                data = data,
                height = 100.dp,
                barWidth = 25.dp,
                barColor = MaterialTheme.colorScheme.primaryContainer,
                horizontalLineColor = MaterialTheme.colorScheme.onPrimaryContainer,
                barArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.padding(start = largeDP, top = largeDP, end = largeDP)
            )
        }

        items(crag.routes.sortedBy { it.grade }) {
            RouteCard(
                route = it,
                navController = navController
            )
        }
        item {
            Spacer(modifier = Modifier.height(largeDP))
        }
    }
}

@Composable
fun RouteCard(
    route: Route,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    Card(
        modifier = modifier
            .clickable(onClick = {
                val backStackEntry = navController.currentBackStackEntry
                val locationType = backStackEntry?.arguments?.getString("locationType")
                val locationId = backStackEntry?.arguments?.getString("locationId")
                val locationName = backStackEntry?.arguments?.getString("locationName")

                if (locationType == null || locationId == null || locationName == null) {
                    return@clickable
                }
                navController.navigate(
                    ClimbTimeRoute.Route.buildRoute(
                        locationId = locationId,
                        locationName = locationName,
                        locationType = locationType,
                        routeId = route.id,
                        routeName = route.name
                    )
                )
            }),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onSecondary,
            contentColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.padding(start = largeDP),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = route.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = smallDP)
                )
                Text(
                    text = route.status,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = smallDP)
                )
            }
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.padding(32.dp),
                    text = route.grade,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}