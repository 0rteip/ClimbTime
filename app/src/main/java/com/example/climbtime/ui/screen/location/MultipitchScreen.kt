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
import com.example.climbtime.data.model.place.Multipitch
import com.example.climbtime.ui.component.largeDP

@Composable
fun MultipitchScreen(multipitch: Multipitch) {

    val info = listOf(
        Pair(stringResource(id = R.string.difficulty), multipitch.difficulty),
        Pair(stringResource(id = R.string.length), multipitch.length.toString() + "m"),
        Pair(stringResource(id = R.string.pitches), multipitch.pitches.toString()),
    )

    LazyColumn(
        modifier = Modifier
            .padding(horizontal = largeDP)
            .padding(bottom = largeDP),
        verticalArrangement = Arrangement.spacedBy(largeDP)
    ) {
        item {
            Text(text = multipitch.description)
        }

        items(info.chunked(2)) { inf ->
            RowInfo(info1 = inf[0], info2 = if (inf.size > 1) inf[1] else null)
        }

        item {
            NavigateTo(place = multipitch)
        }

    }
}
