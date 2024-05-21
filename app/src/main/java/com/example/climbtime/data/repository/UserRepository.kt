package com.example.climbtime.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.example.climbtime.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val auth: FirebaseAuth,
    private val store: FirebaseStorage,
    private val firestore: FirebaseFirestore,
    private val context: Context
) {
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val SAVED_LOCATIONS_KEY = "savedLocations"
        private const val USER_IMAGE_KEY = "userImages"
    }

    var user = mutableStateOf(auth.currentUser)
    val userImageUri = mutableStateOf(Uri.EMPTY)
    val savedLocations = MutableStateFlow(listOf<String>())

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

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
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

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
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
        userImageUri.value = Uri.EMPTY
        savedLocations.value = listOf()
    }

    fun resetPassword(email: String, callback: (Boolean, String) -> Unit) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, "")
            } else {
                callback(false, task.exception?.message ?: "")
            }
        }
    }

    fun changePassword(
        email: String, password: String, newPassword: String, callback: (Boolean, String) -> Unit
    ) {
        val (isValid, message) = validData(email, password)
        if (!isValid) {
            callback(false, message)
            return
        }
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                auth.currentUser?.updatePassword(newPassword)?.addOnCompleteListener { updateTask ->
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
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.value = null
                auth.currentUser?.delete()?.addOnCompleteListener { deleteTask ->
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

    fun saveUserImageUri(uri: Uri) {
        val storageRef = store.reference.child("$USER_IMAGE_KEY/${user.value?.uid}")
        storageRef.putFile(uri).addOnSuccessListener {
            userImageUri.value = uri
//            Log.d("UserRepository", "Image uploaded successfully")
        }.addOnFailureListener { exception ->
            Log.e("UserRepository", "Failed to upload image: ${exception.message}")
        }
    }

    fun deleteUserImage() {
        val storageRef = store.reference.child("$USER_IMAGE_KEY/${user.value?.uid}")
        storageRef.delete().addOnSuccessListener {
            userImageUri.value = Uri.EMPTY
//            Log.d("UserRepository", "Image deleted successfully")
        }.addOnFailureListener { exception ->
            Log.e("UserRepository", "Failed to delete image: ${exception.message}")
        }
    }

    suspend fun getUserImageUri() {
        val storageRef = store.reference.child("$USER_IMAGE_KEY/${user.value?.uid}")
        userImageUri.value = try {
            storageRef.downloadUrl.await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to get image URL: ${e.message}")
            Uri.EMPTY
        }
    }

    fun saveLocation(location: String) {
        val user = auth.currentUser
        if (user != null) {
            val userDocument = firestore.collection(USERS_COLLECTION).document(user.uid)
            userDocument.set(
                mapOf(SAVED_LOCATIONS_KEY to FieldValue.arrayUnion(location)), SetOptions.merge()
            ).addOnSuccessListener {
                val currentLocations = savedLocations.value.toMutableList()
                currentLocations.add(location)
                savedLocations.value = currentLocations
//                Log.d("UserRepository", "Location saved successfully in Firestore")
            }.addOnFailureListener { exception ->
                Log.e(
                    "UserRepository",
                    "Failed to save location in Firestore: ${exception.message}"
                )
            }
        }
    }

    fun removeLocation(location: String) {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection(USERS_COLLECTION).document(user.uid)
                .update(SAVED_LOCATIONS_KEY, FieldValue.arrayRemove(location)).addOnSuccessListener {
                    val currentLocations = savedLocations.value.toMutableList()
                    currentLocations.remove(location)
                    savedLocations.value = currentLocations
//                    Log.d("UserRepository", "Location removed successfully from Firestore")
                }.addOnFailureListener { exception ->
                    Log.e(
                        "UserRepository",
                        "Failed to remove location from Firestore: ${exception.message}"
                    )
                }
        }
    }

    private fun initializeSavedLocations() {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection(USERS_COLLECTION).document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val locations = document.get(SAVED_LOCATIONS_KEY)
                        if (locations is List<*>) {
                            savedLocations.value = locations.filterIsInstance<String>()
//                            Log.d("UserRepository", "Saved locations initialized successfully, ${savedLocations.value}")
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.e(
                        "UserRepository",
                        "Failed to get saved locations from Firestore: ${exception.message}"
                    )
                }
        }
    }

    init {
        initializeSavedLocations()
    }
}