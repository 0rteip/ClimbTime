package com.example.climbtime.data.model.place

class Location(
    val longitude: Double,
    val latitude: Double
) {
    companion object {
        val EMPTY: Location = Location(0.0, 0.0)
    }
}

interface Place {
    val id: String
    val name: String
    val location: Location
    val description: String
    val type: String
}