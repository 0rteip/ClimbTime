package com.example.climbtime.data.model.place

import com.example.climbtime.data.model.FilterItem

class Multipitch(
    override val id: String,
    override val name: String,
    override val location: Location,
    override val description: String,
    val difficulty: String,
    val length: Int,
    val pitches: Int
) : Place {
    override val type = FilterItem.Multipitches.filterName
}