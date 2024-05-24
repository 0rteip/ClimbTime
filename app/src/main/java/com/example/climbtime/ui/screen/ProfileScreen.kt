package com.example.climbtime.ui.screen

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavHostController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.climbtime.R
import com.example.climbtime.data.model.utils.PermissionStatus
import com.example.climbtime.data.model.utils.rememberCameraLauncher
import com.example.climbtime.data.model.utils.rememberPermission
import com.example.climbtime.data.model.utils.saveImageToStorage
import com.example.climbtime.ui.ClimbTimeRoute
import com.example.climbtime.ui.component.OpenSettings
import com.example.climbtime.ui.component.PermissionDeniedAlert
import com.example.climbtime.ui.component.largeDP
import com.example.climbtime.ui.component.mediumDP
import com.example.climbtime.ui.component.smallDP
import com.example.climbtime.ui.viewmodel.UserActions
import com.example.climbtime.ui.viewmodel.UserState

data class LogState(
    var exception: MutableState<Boolean> = mutableStateOf(false),
    var message: MutableState<String> = mutableStateOf("")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userState: UserState, userAction: UserActions, navController: NavHostController
) {
    val snackBarHostState = remember { SnackbarHostState() }
    var editProfile by rememberSaveable { mutableStateOf(false) }

    Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) }, topBar = {
        TopAppBar(title = {
            Text(
                if (editProfile) stringResource(R.string.edit_profile) else if (userState.user.value != null) stringResource(
                    R.string.profile
                ) else stringResource(
                    R.string.login
                )
            )

        }, actions = {
            if (userState.user.value != null && !editProfile) {
                IconButton(onClick = { editProfile = !editProfile }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit profile")
                }
            }
            if (!editProfile) {
                IconButton(onClick = { navController.navigate(ClimbTimeRoute.Settings.route) }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
            }
            if (editProfile) {
                IconButton(onClick = { editProfile = !editProfile }) {
                    Icon(Icons.Filled.Done, contentDescription = "Done")
                }
            }
        })
    }) { paddingValues ->
        if (userState.user.value == null) {
            UserNotLoggedIn(
                modifier = Modifier.padding(paddingValues),
                userAction = userAction,
                snackBarHostState = snackBarHostState
            )
        } else {
            userAction.getImage()
            if (editProfile) {
                EditProfile(
                    userState = userState,
                    userAction = userAction,
                    snackBarHostState = snackBarHostState,
                    modifier = Modifier.padding(paddingValues)
                )
            } else {
                UserLoggedIn(
                    userState = userState,
                    onLogout = { userAction.logout() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun ResetPasswordDialog(
    onDismiss: () -> Unit,
    email: String,
    userAction: UserActions,
    context: Context,
    snackBarHostState: SnackbarHostState
) {
    CreateNotificationChannel()

    val postPermission =
        rememberPermission(permission = Manifest.permission.POST_NOTIFICATIONS) {
        }
    // create an intent to open email app on click on the notification
    val emailIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_APP_EMAIL)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    val pendingIntent = PendingIntent.getActivity(
        context, 0, emailIntent, PendingIntent.FLAG_IMMUTABLE
    )

    AlertDialog(onDismissRequest = { onDismiss() },
        title = { Text(stringResource(id = R.string.reset_password)) },
        text = { Text(stringResource(id = R.string.email_set_to) + " $email") },
        confirmButton = {
            Button(onClick = {
                userAction.resetPassword(email) { result, message ->
                    if (!result) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    } else {
                        var builder = NotificationCompat.Builder(
                            context,
                            context.getString(R.string.reset_password)
                        )
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(context.getString(R.string.reset_password))
                            .setContentText(context.getString(R.string.email_sent) + " $email")
                            .setContentIntent(pendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true)

                        with(NotificationManagerCompat.from(context)) {
                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                notify(1, builder.build())
                            } else {
                                postPermission.launchPermissionRequest()
                            }
                        }
                        onDismiss()
                    }
                }
            }) {
                Text(stringResource(id = R.string.send))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
            ) {
                Text(stringResource(id = R.string.cancel))
            }
        })
}

@Composable
fun CreateNotificationChannel() {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val context = LocalContext.current
        val name = context.getString(R.string.reset_password)
        val descriptionText = context.getString(R.string.reset_password)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = android.app.NotificationChannel(name, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(context, NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

}

@Composable
fun EditProfile(
    userState: UserState,
    userAction: UserActions,
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val imageUri = userState.imageUri.value
    val keyboardController = LocalSoftwareKeyboardController.current
    var showSnack by remember { mutableStateOf(false) }
    var openSettings by remember { mutableStateOf(false) }

    val cameraLauncher = rememberCameraLauncher {
        saveImageToStorage(it, context.applicationContext.contentResolver)
        userAction.uploadImage(it)
        Log.d("TakeImageOrSelectImage", "URI: $it")
    }


    if (openSettings) {
        OpenSettings()
    }

    LaunchedEffect(showSnack) {
        if (showSnack) {
            snackBarHostState.currentSnackbarData?.dismiss()
            val res = snackBarHostState.showSnackbar(
                message = context.getString(R.string.camera_permantely_denied),
                actionLabel = context.getString(R.string.open_settings),
                duration = SnackbarDuration.Long
            )
            if (res == SnackbarResult.ActionPerformed) {
                openSettings = true
            }
            showSnack = false
        }
    }

    val cameraPermission = rememberPermission(
        permission = android.Manifest.permission.CAMERA
    ) {
        when (it) {
            PermissionStatus.Unknown -> {}
            PermissionStatus.Granted -> cameraLauncher.launchCamera()
            PermissionStatus.Denied -> {
                Toast.makeText(
                    context, context.getText(R.string.permisison_denied), Toast.LENGTH_SHORT
                ).show()
            }

            PermissionStatus.PermanentlyDenied -> {
                showSnack = true
            }
        }
    }

    fun takePicture() {
        if (cameraPermission.status.isGranted) {
            cameraLauncher.launchCamera()
        } else {
            cameraPermission.launchPermissionRequest()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { it?.let { userAction.uploadImage(it) } }

    var email by rememberSaveable { mutableStateOf(userState.user.value?.email ?: "") }
    var password by rememberSaveable { mutableStateOf("") }
    var oldPassword by rememberSaveable { mutableStateOf("") }
    var showDialog by rememberSaveable { mutableStateOf(false) }

    if (showDialog) {
        ResetPasswordDialog(
            onDismiss = { showDialog = false },
            email = email,
            userAction = userAction,
            context = context,
            snackBarHostState = snackBarHostState
        )
    }

    Column(
        modifier = modifier.padding(horizontal = largeDP),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(mediumDP)

    ) {
        ChangeImage(
            imageUri = imageUri, takePicture = { takePicture() }, launcher = launcher
        )

        Spacer(modifier = Modifier.size(largeDP))

        EmailField(email = email,
            label = stringResource(id = R.string.email),
            placeholder = stringResource(id = R.string.enter_email),
            isReadOnly = true,
            onEmailChange = { email = it })

        Spacer(modifier = Modifier.size(largeDP))

        PasswordField(password = oldPassword,
            label = stringResource(id = R.string.old_password),
            placeholder = stringResource(id = R.string.enter_old_password),
            onPasswordChange = { oldPassword = it })

        PasswordField(password = password,
            label = stringResource(id = R.string.new_password),
            placeholder = stringResource(id = R.string.enter_new_password),
            onPasswordChange = { password = it })

        Spacer(modifier = Modifier.size(largeDP))

        Button(
            onClick = {
                userAction.changePassword(email, oldPassword, password) { result, message ->
                    if (!result) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    } else {
                        keyboardController?.hide()
                        Toast.makeText(
                            context, context.getText(R.string.password_updated), Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !(oldPassword.isEmpty() || password.isEmpty())
        ) {
            Text(stringResource(id = R.string.change_password))
        }

        TextButton(onClick = { showDialog = true }) {
            Text(text = stringResource(id = R.string.password_forgot))
        }
    }
}

@Composable
fun ChangeImage(
    modifier: Modifier = Modifier,
    imageUri: Uri,
    takePicture: () -> Unit,
    launcher: ActivityResultLauncher<String>
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize(), contentAlignment = Alignment.TopCenter
    ) {
        ProfileImage(imageUri = imageUri, modifier = Modifier.align(Alignment.Center))
        IconButton(
            onClick = { takePicture() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
        ) {
            Icon(
                Icons.Filled.CameraAlt,
                contentDescription = "Take a picture",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        IconButton(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)

        ) {
            Icon(
                Icons.Filled.Image,
                contentDescription = "Select an image from gallery",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun EmailField(
    email: String,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isReadOnly: Boolean = false,
    onEmailChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        leadingIcon = { Icon(imageVector = Icons.Filled.Person, contentDescription = null) },
        singleLine = true,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        placeholder = { Text(placeholder) },
        readOnly = isReadOnly
    )
}

@Composable
fun PasswordField(
    password: String,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    onPasswordChange: (String) -> Unit,
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(value = password,
        onValueChange = onPasswordChange,
        leadingIcon = { Icon(imageVector = Icons.Filled.Key, contentDescription = null) },
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
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        placeholder = { Text(placeholder) })

}

@Composable
fun UserNotLoggedIn(
    modifier: Modifier = Modifier, userAction: UserActions, snackBarHostState: SnackbarHostState
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val loginState by remember { mutableStateOf(LogState()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var showSnack by remember { mutableStateOf(false) }

    if (showDialog) {
        ResetPasswordDialog(
            onDismiss = { showDialog = false },
            email = email,
            userAction = userAction,
            context = context,
            snackBarHostState = snackBarHostState
        )
    }

    LaunchedEffect(showSnack) {

        Log.d("UserNotLoggedIn", "Show snack: $showSnack")
        if (showSnack) {
            snackBarHostState.currentSnackbarData?.dismiss()
            var msg =
                context.getString(R.string.email) + ": " + email + " " + context.getString(R.string.email_invalid)
            if (email.trim().isEmpty()) {
                msg = context.getString(R.string.email_empty)
            }
            snackBarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
            showSnack = false
        }
    }

    LaunchedEffect(loginState.message.value) {
        if (loginState.exception.value) {
            snackBarHostState.currentSnackbarData?.dismiss()
            snackBarHostState.showSnackbar(
                message = loginState.message.value, duration = SnackbarDuration.Long
            )
            loginState.exception.value = false
            loginState.message.value = ""
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
                .padding(horizontal = largeDP),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(mediumDP)
        ) {
            EmailField(email = email,
                label = stringResource(id = R.string.email),
                placeholder = stringResource(id = R.string.enter_email),
                onEmailChange = { email = it })

            PasswordField(password = password,
                label = stringResource(id = R.string.password),
                placeholder = stringResource(id = R.string.enter_password),
                onPasswordChange = { password = it })

            Spacer(modifier = Modifier.size(mediumDP))

            Button(
                onClick = {
                    keyboardController?.hide()
                    userAction.login(email, password) { result, message ->
                        loginState.message.value = message
                        loginState.exception.value = !result
                    }
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.login))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(id = R.string.no_account))
                Text(
                    text = stringResource(id = R.string.register),
                    modifier = Modifier
                        .padding(start = smallDP)
                        .clickable {
                            keyboardController?.hide()
                            userAction.register(email, password) { result, message ->
                                loginState.message.value = message
                                loginState.exception.value = !result
                            }
                        },
                    color = MaterialTheme.colorScheme.primary
                )
            }

            TextButton(onClick = {
                // emial corret cormat
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    showSnack = true
                    keyboardController?.hide()
                } else {
                    showDialog = true
                }
            }) {
                Text(text = stringResource(id = R.string.password_forgot))
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfileImage(
    imageUri: Uri, modifier: Modifier = Modifier
) {
    if (imageUri != Uri.EMPTY) {
        GlideImage(
            model = imageUri,
            contentDescription = "Profile image",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size((LocalConfiguration.current.screenWidthDp / 2.5).dp)
                .aspectRatio(1f)
                .clip(CircleShape),
            loading = placeholder(ColorPainter(MaterialTheme.colorScheme.primary))
        )
    } else {
        Box(
            modifier = modifier
                .size((LocalConfiguration.current.screenWidthDp / 2.5).dp)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Profile image",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(LocalConfiguration.current.screenWidthDp.dp / 4)
            )
        }
    }
}

@Composable
fun UserLoggedIn(
    userState: UserState, onLogout: () -> Unit, modifier: Modifier = Modifier
) {
    val imageUri = userState.imageUri.value
    Log.d("UserLoggedIn", "Image URI: $imageUri")
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = largeDP),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(largeDP)
    ) {
        ProfileImage(imageUri = imageUri)
        val user = stringResource(id = R.string.user)
        Text(user + ": " + (userState.user.value?.email ?: ""))
        Button(
            onClick = onLogout, modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.logout))
        }
    }
}
