package com.example.climbtime.data.model.place

import com.example.climbtime.data.model.FilterItem


/**
 * Represents a via ferrata climbing location.
 * @param name the name of the via ferrata
 * @param location the location of the via ferrata
 * @param description the description of the via ferrata
 * @param difficulty the difficulty of the via ferrata
 * @param status the status of the via ferrata
 * @param duration the duration of the via ferrata
 */
class ViaFerrata(
    override val id: String,
    override val name: String,
    override val location: Location,
    override val description: String,
    val difficulty: String,
    val status: String,
    val duration: Int,
) : Place {
    override val type = FilterItem.ViaFerrata.filterName
}