package com.example.climbtime.ui.screen.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.climbtime.R
import com.example.climbtime.data.model.place.Bouldering
import com.example.climbtime.ui.component.largeDP

@Composable
fun BoulderingScreen(bouldering: Bouldering) {

    val info = listOf(
        Pair(stringResource(id = R.string.difficulty), bouldering.difficulty),
        Pair(stringResource(id = R.string.route_count), bouldering.routeCount.toString()),
        Pair(stringResource(id = R.string.status), bouldering.status),
    )

    LazyColumn(
        modifier = Modifier
            .padding(horizontal = largeDP)
            .padding(bottom = largeDP),
        verticalArrangement = Arrangement.spacedBy(largeDP)
    ) {
        item {
            Text(text = bouldering.description)
        }

        items(info.chunked(2)) { inf ->
            RowInfo(info1 = inf[0], info2 = if (inf.size > 1) inf[1] else null)
        }

        item {
            NavigateTo(place = bouldering)
        }

    }
}
