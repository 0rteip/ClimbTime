package com.example.climbtime.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.example.climbtime.R
import com.example.climbtime.data.model.theme.Theme
import com.example.climbtime.ui.component.extraLargeDP
import com.example.climbtime.ui.component.largeDP
import com.example.climbtime.ui.component.mediumDP
import com.example.climbtime.ui.component.smallDP
import com.example.climbtime.ui.viewmodel.SettingsState
import com.example.climbtime.ui.viewmodel.UserActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsState: SettingsState,
    userActions: UserActions,
    onThemeChange: (Theme) -> Unit,
    userEmail: String?
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }
    val loginState by remember { mutableStateOf(LogState()) }

    if (showDialog) {
        DeleteAccountDialog(dismissAction = { showDialog = false },
            errorMessage = loginState.message.value,
            deleteAction = { password ->
                userActions.deleteAccount(userEmail ?: "", password) { exception, message ->
                    loginState.exception.value = !exception
                    loginState.message.value = message
                    if (exception) {
                        showDialog = false
                    }
                }
            })
    }

    Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) }, topBar = {
        TopAppBar(title = { Text(text = stringResource(id = R.string.settings)) },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            })
    }) { pd ->
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(pd)
                .padding(horizontal = largeDP)
        ) {
            Text(
                text = stringResource(id = R.string.theme),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = smallDP)
            )
            Column(
                Modifier.selectableGroup(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
            ) {
                Theme.entries.forEach { theme ->
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(mediumDP))
                            .fillMaxWidth()
                            .selectable(
                                selected = (theme == settingsState.theme),
                                onClick = { onThemeChange(theme) },
                                role = Role.RadioButton
                            )
                            .padding(
                                top = smallDP, bottom = smallDP, start = smallDP, end = largeDP
                            ), verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == settingsState.theme), onClick = null
                        )
                        Text(
                            text = stringResource(
                                id = when (theme) {
                                    Theme.LIGHT -> R.string.theme_light
                                    Theme.DARK -> R.string.theme_dark
                                    Theme.SYSTEM -> R.string.theme_system
                                }
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(
                                start = largeDP
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(smallDP))
                }
            }

            if (userEmail != null) {
                Spacer(modifier = Modifier.height(largeDP))
                Text(
                    text = stringResource(id = R.string.account),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = smallDP)
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showDialog = true },
                ) {
                    Text(text = stringResource(id = R.string.delete_account))
                }
            }
        }
    }
}

@Composable
fun DeleteAccountDialog(
    dismissAction: () -> Unit,
    deleteAction: (String) -> Unit,
    errorMessage: String,
) {
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    AlertDialog(onDismissRequest = {
        dismissAction()
    }, confirmButton = {
        Button(
            onClick = {
                deleteAction(password)
            }, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(text = stringResource(id = R.string.delete_account))
        }
    }, dismissButton = {
        TextButton(onClick = { dismissAction() }) {
            Text(text = stringResource(id = R.string.cancel))
        }
    }, title = { Text(text = stringResource(id = R.string.delete_account)) }, text = {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(mediumDP),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.delete_account_message),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(value = password,
                onValueChange = { password = it },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                label = { Text(stringResource(id = R.string.password)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                placeholder = { Text(stringResource(id = R.string.password)) })
            if (errorMessage.isNotEmpty()) {

                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    })
}
