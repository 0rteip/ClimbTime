package com.example.climbtime.data.model.place

import com.example.climbtime.data.model.FilterItem

/**
 * Represents an indoor climbing location.
 * @param name the name of the indoor climbing
 * @param location the location of the indoor climbing
 * @param description the description of the indoor climbing
 * @param openingHours the opening hours of the indoor climbing
 * @param website the website of the indoor climbing
 */
data class Indoor(
    override val id: String,
    override val name: String,
    override val location: Location,
    override val description: String,
    val openingHours: String,
    val website: String,
    val closingHours: String
) : Place {
    override val type: String = FilterItem.Indoor.filterName
}