package com.example.climbtime.data.repository

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.climbtime.data.model.FilterItem
import com.example.climbtime.data.model.place.Crag
import com.example.climbtime.data.model.place.GenericPlace
import com.example.climbtime.data.model.place.Indoor
import com.example.climbtime.data.model.place.Location
import com.example.climbtime.data.model.place.Multipitch
import com.example.climbtime.data.model.place.Place
import com.example.climbtime.data.model.place.Route
import com.example.climbtime.data.model.place.ViaFerrata
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class HomeRepository(
    private val dataStore: DataStore<Preferences>, private val firestore: FirebaseFirestore
) {
    companion object {
        private val SELECTED_FILTERS_KEY = stringPreferencesKey("selected_filters")
        private val LAST_POSITION_KEY = stringPreferencesKey("last_position")
        private val LAST_ZOOM_KEY = stringPreferencesKey("last_zoom")
        private const val TAG = "HomeRepository"
    }

    val filters = dataStore.data.map {
        it[SELECTED_FILTERS_KEY] ?: emptyList<String>().joinToString(",")
    }

    val lastPosition = dataStore.data.map {
        it[LAST_POSITION_KEY] ?: "44.0, 12.0"
    }

    val lastZoom = dataStore.data.map {
        it[LAST_ZOOM_KEY] ?: "5.2"
    }

    val allPlaces = MutableStateFlow(emptyList<Place>())
    val places = mutableStateOf(emptyList<Place>())

    /**
     * Add a filter to the list if it is not in the list.
     * @param value the value to add
     */
    suspend fun addFilter(value: String) {
//        Log.d(TAG, "addFilter: $value")
        val filters = filters.first().split(",")
        if (value !in filters) {
            dataStore.edit {
                it[SELECTED_FILTERS_KEY] = filters.plus(value).joinToString(",")
            }
            updatePlaces(filters.plus(value))
        }
    }

    /**
     * Remove a filter from the list if it is in the list.
     * @param value the value to remove
     */
    suspend fun removeFilter(value: String) {
//        Log.d(TAG, "removeFilter: $value")
        val filters = filters.first().split(",")
        if (value in filters) {
            dataStore.edit {
                it[SELECTED_FILTERS_KEY] = filters.minus(value).joinToString(",")
            }
            updatePlaces(filters.minus(value))
        }
    }

    /**
     * Update the last position in the data store.
     * @param value the value to update
     */
    suspend fun updateLastPosition(value: String) {
//        Log.d(TAG, "updateLastPosition: $value")
        dataStore.edit {
            it[LAST_POSITION_KEY] = value
        }
    }

    /**
     * Get the list of places from Firestore based on the filters.
     * @return the list of places
     */
    fun updatePlaces(filters: List<String>) {
        if (allPlaces.value.isEmpty()) {
            FilterItem.items.filter { it != null }.forEach {
//                Log.d(TAG, "filterName: ${it.filterName}")
                firestore.collection(it.filterName).get().addOnSuccessListener { result ->
                    for (document in result) {
                        // if the place is not in the list, add it
                        if (allPlaces.value.none { place -> place.id == document.id }) {
                            allPlaces.value =
                                allPlaces.value.plus(fromDocument(document, it.filterName))
                        }
                    }

                    // se ultimo filtro
                    if (filters.size == 1 && filters[0] == "") {
//                        Log.d(TAG, "filters: $filters, adding all places")
                        places.value = allPlaces.value
                    } else {
//                        Log.d(TAG, "filters: $filters, filtering places")
                        places.value = allPlaces.value.filter { place -> place.type in filters }
                    }
                }.addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                }
            }
        }

//        Log.d(TAG, "filters: $filters")

        if (filters.size == 1 && filters[0] == "") {
            places.value = allPlaces.value
        } else {
            places.value = allPlaces.value.filter { it.type in filters }
        }
    }

    suspend fun updateLastZoom(value: String) {
//        Log.d(TAG, "updateLastZoom: $value")
        dataStore.edit {
            it[LAST_ZOOM_KEY] = value
        }
    }


    private fun fromDocument(document: QueryDocumentSnapshot, type: String): Place {
        fun routeFromDocument(document: QueryDocumentSnapshot): Route {
            return Route(
                id = document.id,
                name = document.data["name"] as String,
                description = document.data["description"] as String,
                grade = document.data["grade"] as String,
                anchor = document.data["anchors"] as Boolean,
                bolts = (document.data["bolts"] as Long).toInt(),
                length = (document.data["length"] as Long).toInt(),
                status = document.data["status"] as String,
                style = document.data["style"] as String
            )
        }

        return when (type) {
            FilterItem.Indoor.filterName -> {
                Indoor(
                    id = document.id,
                    name = document.data["name"] as String,
                    location = Location(
                        latitude = document.data["latitude"] as Double,
                        longitude = document.data["longitude"] as Double
                    ),
                    description = document.data["description"] as String,
                    openingHours = document.data["opening_hours"] as String,
                    website = document.data["website"] as String,
                )
            }

            FilterItem.ViaFerrata.filterName -> {
                ViaFerrata(
                    id = document.id,
                    name = document.data["name"] as String,
                    location = Location(
                        latitude = document.data["latitude"] as Double,
                        longitude = document.data["longitude"] as Double
                    ),
                    description = document.data["description"] as String,
                    difficulty = document.data["difficulty_level"] as String,
                    status = document.data["status"] as String,
                    duration = (document.data["duration_hours"] as Long).toInt()
                )
            }

            FilterItem.Bouldering.filterName -> {
                com.example.climbtime.data.model.place.Bouldering(
                    id = document.id,
                    name = document.data["name"] as String,
                    location = Location(
                        latitude = document.data["latitude"] as Double,
                        longitude = document.data["longitude"] as Double
                    ),
                    description = document.data["description"] as String,
                    difficulty = document.data["difficulty_level"] as String,
                    status = document.data["status"] as String,
                    routeCount = (document.data["route_count"] as Long).toInt()
                )
            }

            FilterItem.Multipitches.filterName -> {
                Multipitch(
                    id = document.id,
                    name = document.data["name"] as String,
                    location = Location(
                        latitude = document.data["latitude"] as Double,
                        longitude = document.data["longitude"] as Double
                    ),
                    description = document.data["description"] as String,
                    difficulty = document.data["difficulty_level"] as String,
                    length = (document.data["length"] as Double).toInt(),
                    pitches = (document.data["pitches_number"] as Long).toInt()
                )
            }

            FilterItem.Crag.filterName -> {
                val routes = mutableListOf<Route>()
                firestore.collection("$type/${document.id}/routes").get()
                    .addOnSuccessListener { result ->
                        for (route in result) {
                            routes.add(routeFromDocument(route))
                        }
                    }.addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting documents.", exception)
                    }
                return Crag(
                    id = document.id,
                    name = document.data["name"] as String,
                    location = Location(
                        latitude = document.data["latitude"] as Double,
                        longitude = document.data["longitude"] as Double
                    ),
                    description = document.data["description"] as String,
                    rockType = document.data["rock_type"] as String,
                    routes = routes
                )
            }

            else -> {
                GenericPlace.EMPTY
            }
        }
    }
}
