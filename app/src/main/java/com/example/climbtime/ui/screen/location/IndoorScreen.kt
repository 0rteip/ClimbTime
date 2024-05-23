package com.example.climbtime.ui.screen.location

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.climbtime.R
import com.example.climbtime.data.model.place.Indoor
import com.example.climbtime.ui.component.largeDP

@Composable
fun IndoorScreen(indoor: Indoor) {

    val info = listOf(
        Pair(stringResource(id = R.string.opening_hour), indoor.openingHours),
        Pair(stringResource(id = R.string.closing_hour), indoor.closingHours)
    )

    LazyColumn(
        modifier = Modifier
            .padding(horizontal = largeDP)
            .padding(bottom = largeDP),
        verticalArrangement = Arrangement.spacedBy(largeDP)
    ) {
        item {
            Text(text = indoor.description)
        }

        items(info.chunked(2)) { inf ->
            RowInfo(info1 = inf[0], info2 = if (inf.size > 1) inf[1] else null)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CenteredButtonWithViewIntent(
                    text = stringResource(id = R.string.visit_website),
                    uri = indoor.website,
                    modifier = Modifier.weight(1f)
                )
                NavigateTo(
                    place = indoor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

}