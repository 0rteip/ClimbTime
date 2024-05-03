package com.example.climbtime.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.climbtime.R

@Composable
fun LocationDisableAlert(
    onDismissAction: () -> Unit,
    onClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismissAction() },
        title = { Text(stringResource(R.string.location_disabled_title)) },
        text = {
            Text(stringResource(R.string.location_disabled_text))
        },
        confirmButton = {
            TextButton(
                onClick = { onClick() }
            ) {
                Text(stringResource(R.string.open_settings))
            }
        }
    )
}

@Composable
fun PermissionDeniedAlert(
    onDismissAction: () -> Unit,
    onClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismissAction() },
        title = { Text(stringResource(R.string.permisison_denied)) },
        text = {
            Text(stringResource(R.string.location_denied_description))
        },
        confirmButton = {
            Button(
                onClick = {
                    onClick()
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismissAction() }
            ) {
                Text(stringResource(R.string.dismiss))
            }
        }

    )
}