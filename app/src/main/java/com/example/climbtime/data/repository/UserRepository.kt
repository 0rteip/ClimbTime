package com.example.climbtime.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.climbtime.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class UserRepository(
    private val auth: FirebaseAuth,
    private val dataStore: DataStore<Preferences>,
    private val context: Context
) {
    companion object {
        private val USER_IMAGE_URI = stringPreferencesKey("user_image_uri")
        private val SAVED_LOCATIONS = stringPreferencesKey("saved_locations")
    }

    var user = mutableStateOf(auth.currentUser)
    val userImageUri = dataStore.data.map {
        it[USER_IMAGE_URI] ?: ""
    }
    val savedLocations = dataStore.data.map {
        it[SAVED_LOCATIONS] ?: ""
    }

    private fun validData(email: String, password: String): Pair<Boolean, String> {
        if (email.isEmpty() && password.isEmpty()) {
            // string resource as result
            return Pair(false, context.getString(R.string.email_psw_empy))
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Pair(false, context.getString(R.string.email_not_valid))
        }
        if (password.length < 6) {
            return Pair(false, context.getString(R.string.psw_short))
        }
        return Pair(true, "")
    }

    fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        val (isValid, message) = validData(email, password)
        if (!isValid) {
            callback(false, message)
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, auth.currentUser?.uid ?: "")
                    user.value = auth.currentUser
                } else {
                    Log.e("UserRepository", "Failed to login: ${task.exception?.message}")
                    callback(false, task.exception?.message ?: "")
                }
            }
    }

    fun register(email: String, password: String, callback: (Boolean, String) -> Unit) {
        val (isValid, message) = validData(email, password)
        if (!isValid) {
            callback(false, message)
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, auth.currentUser?.uid ?: "")
                    user.value = auth.currentUser
                } else {
                    callback(false, task.exception?.message ?: "")
                }
            }
    }

    fun logout() {
        auth.signOut()
        user.value = null
    }

    fun resetPassword(email: String, callback: (Boolean, String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "")
                } else {
                    callback(false, task.exception?.message ?: "")
                }
            }
    }

    fun changePassword(
        email: String,
        password: String,
        newPassword: String,
        callback: (Boolean, String) -> Unit
    ) {
        val (isValid, message) = validData(email, password)
        if (!isValid) {
            callback(false, message)
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.updatePassword(newPassword)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                callback(true, auth.currentUser?.uid ?: "")
                                user.value = auth.currentUser
                            } else {
                                callback(false, updateTask.exception?.message ?: "")
                            }
                        }
                } else {
                    callback(false, task.exception?.message ?: "")
                }
            }

    }

    fun deleteAccount(email: String, password: String, callback: (Boolean, String) -> Unit) {
        val (isValid, message) = validData(email, password)
        if (!isValid) {
            callback(false, message)
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.value = null
                    auth.currentUser?.delete()
                        ?.addOnCompleteListener { deleteTask ->
                            if (deleteTask.isSuccessful) {
                                callback(true, "")
                            } else {
                                callback(false, deleteTask.exception?.message ?: "")
                            }
                        }
                } else {
                    callback(false, task.exception?.message ?: "")
                }
            }
    }

    suspend fun saveUserImageUri(uri: Uri) {
        dataStore.edit {
            it[USER_IMAGE_URI] = uri.toString()
        }
    }

    suspend fun clearUserImageUri() {
        dataStore.edit {
            it.remove(USER_IMAGE_URI)
        }
    }

    suspend fun saveLocation(location: String) {
        val savedLocations = savedLocations.first().split(",")
        if (location !in savedLocations) {
            dataStore.edit {
                it[SAVED_LOCATIONS] = savedLocations.plus(location).joinToString(",")
            }
        }
    }

    suspend fun removeLocation(location: String) {
        val savedLocations = savedLocations.first().split(",")
        if (location in savedLocations) {
            dataStore.edit {
                it[SAVED_LOCATIONS] = savedLocations.minus(location).joinToString(",")
            }
        }
    }
}