package com.example.climbtime.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.climbtime.data.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserState(
    val user: MutableState<FirebaseUser?> = mutableStateOf(null),
    val savedLocation: StateFlow<List<String>> = MutableStateFlow(emptyList()),
    val imageUri: StateFlow<Uri> = MutableStateFlow(Uri.EMPTY)
)

interface UserActions {
    fun login(email: String, password: String, callback: (Boolean, String) -> Unit)
    fun register(email: String, password: String, callback: (Boolean, String) -> Unit)
    fun logout()
    fun resetPassword(email: String, callback: (Boolean, String) -> Unit)
    fun deleteAccount(email: String, password: String, callback: (Boolean, String) -> Unit)
    fun uploadImage(uri: Uri)
    fun removeImage()
    fun changePassword(
        email: String, password: String, newPassword: String, callback: (Boolean, String) -> Unit
    )

    fun saveLocation(location: String)
    fun removeLocation(location: String)
}

class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val imageUri = repository.userImageUri.map {
        Uri.parse(it)
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = Uri.EMPTY
    )
    private val savedLocation = repository.savedLocations.map {
        it.split(",")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )
    val state = UserState(
        user = repository.user, savedLocation = savedLocation, imageUri = imageUri
    )


    val actions = object : UserActions {
        override fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
            repository.login(email, password, callback)
        }

        override fun register(
            email: String, password: String, callback: (Boolean, String) -> Unit
        ) {
            repository.register(email, password, callback)
        }

        override fun changePassword(
            email: String,
            password: String,
            newPassword: String,
            callback: (Boolean, String) -> Unit
        ) {
            repository.changePassword(email, password, newPassword, callback)
        }

        override fun resetPassword(email: String, callback: (Boolean, String) -> Unit) {
            repository.resetPassword(email, callback)
        }

        override fun logout() {
            repository.logout()
        }

        override fun deleteAccount(email: String, password: String, callback: (Boolean, String) -> Unit) {
            repository.deleteAccount(email, password, callback)
        }

        override fun uploadImage(uri: Uri) {
            viewModelScope.launch {
                repository.saveUserImageUri(uri)
            }
        }

        override fun removeImage() {
            viewModelScope.launch {
                repository.clearUserImageUri()
            }
        }

        override fun saveLocation(location: String) {
            viewModelScope.launch {
                repository.saveLocation(location)
            }
        }

        override fun removeLocation(location: String) {
            viewModelScope.launch {
                repository.removeLocation(location)
            }
        }
    }
}
