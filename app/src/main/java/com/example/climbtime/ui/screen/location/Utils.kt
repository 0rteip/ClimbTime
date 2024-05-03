package com.example.climbtime.ui.screen.location

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.climbtime.R
import com.example.climbtime.data.model.place.Place
import com.example.climbtime.ui.component.largeDP
import com.example.climbtime.ui.component.mediumDP

@Composable
fun RowInfo(info1: Pair<String, String>, info2: Pair<String, String>? = null) {
    Row(
        modifier = Modifier
            .fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(largeDP),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemInfo(
            title = info1.first,
            value = info1.second,
            modifier = Modifier.weight(1f)
        )
        if (info2 != null) {
            ItemInfo(
                title = info2.first,
                value = info2.second,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
fun ItemInfo(title: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(mediumDP))
            .background(MaterialTheme.colorScheme.onSecondary)
    ) {
        Column(
            modifier = Modifier.padding(largeDP),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = value)
        }
    }
}

@Composable
fun CenteredButtonWithViewIntent(
    text: String,
    uri: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val intent = remember { Intent(Intent.ACTION_VIEW, Uri.parse(uri)) }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            }
        ) {
            Text(text = text)
        }
    }
}

@Composable
fun NavigateTo(place: Place, modifier: Modifier = Modifier) {
    CenteredButtonWithViewIntent(
        text = stringResource(id = R.string.navigate_to),
        uri = "geo:${place.location.latitude},${place.location.longitude}",
        modifier = modifier
    )
}