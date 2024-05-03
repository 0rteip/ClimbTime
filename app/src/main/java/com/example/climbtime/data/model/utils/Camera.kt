package com.example.climbtime.data.model.utils

import android.net.Uri
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

interface CameraLauncher {
    val capturedImageURI: MutableState<Uri>

    fun launchCamera()
}

@Composable
fun rememberCameraLauncher(
    onImageCaptured: (Uri) -> Unit = {}
): CameraLauncher {
    val context = LocalContext.current

    var capturedImageURI by remember {
        mutableStateOf(Uri.EMPTY)
    }

    val cameraActivityLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) {
            if (it) {
                onImageCaptured(capturedImageURI)
            }
        }

    val cameraLauncher by remember {
        derivedStateOf {
            object : CameraLauncher {
                override val capturedImageURI: MutableState<Uri>
                    get() = mutableStateOf(capturedImageURI)

                override fun launchCamera() {
                    val imageFile =
                        File.createTempFile("tmp_image_", ".jpg", context.externalCacheDir)
                    val imageURI = FileProvider.getUriForFile(
                        context,
                        context.packageName + ".provider",
                        imageFile
                    )
                    capturedImageURI = imageURI
                    cameraActivityLauncher.launch(imageURI)
                }
            }
        }
    }

    return cameraLauncher
}