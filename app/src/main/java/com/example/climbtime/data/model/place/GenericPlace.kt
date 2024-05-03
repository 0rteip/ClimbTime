package com.example.climbtime.data.model.place

class GenericPlace(
    override val id: String,
    override val name: String,
    override val location: Location,
    override val description: String,
    override val type: String
) : Place {
    companion object {
        val EMPTY: GenericPlace = GenericPlace("", "", Location.EMPTY, "", "")
    }
}