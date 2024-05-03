package com.example.climbtime.data.model.place

import com.example.climbtime.data.model.FilterItem

class Crag(
    override val id: String,
    override val name: String,
    override val location: Location,
    override val description: String,
    val rockType: String,
    val routes: List<Route>
) : Place {
    override val type = FilterItem.Crag.filterName
}

class Route(
    val id: String,
    val name: String,
    val description: String,
    val grade: String,
    val anchor: Boolean,
    val bolts: Int,
    val length: Int,
    val status: String,
    val style: String
) {
}
