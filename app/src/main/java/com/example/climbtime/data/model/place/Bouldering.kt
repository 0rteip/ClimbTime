package com.example.climbtime.data.model.place

import com.example.climbtime.data.model.FilterItem

class Bouldering(
    override val id: String,
    override val name: String,
    override val location: Location,
    override val description: String,
    val difficulty: String,
    val status: String,
    val routeCount: Int
) : Place {
    override val type = FilterItem.Bouldering.filterName
}