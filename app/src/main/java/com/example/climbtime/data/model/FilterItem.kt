package com.example.climbtime.data.model

import android.os.Parcelable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.climbtime.R
import kotlinx.parcelize.Parcelize

sealed class FilterItem(
    val filterName: String,
    val filterNameResourcesId: Int,
    val nameResourcesId: Int = filterNameResourcesId,
    val icon: ImageVector
) {
    @Parcelize
    data object Crag : FilterItem(
        filterName = "crags",
        filterNameResourcesId = R.string.crags,
        nameResourcesId = R.string.crag,
        icon = Icons.Filled.LocationOn
    ), Parcelable

    @Parcelize
    data object Multipitches : FilterItem(
        filterName = "multipitches",
        filterNameResourcesId = R.string.multipitches,
        nameResourcesId = R.string.multipitch,
        icon = Icons.Filled.LocationOn
    ), Parcelable

    @Parcelize
    data object Bouldering : FilterItem(
        filterName = "bouldering",
        filterNameResourcesId = R.string.bouldering,
        icon = Icons.Filled.LocationOn
    ), Parcelable

    @Parcelize
    data object Indoor : FilterItem(
        filterName = "indoor",
        filterNameResourcesId = R.string.indoor,
        icon = Icons.Filled.LocationOn
    ), Parcelable

    @Parcelize
    data object ViaFerrata : FilterItem(
        filterName = "vie_ferrate",
        filterNameResourcesId = R.string.vie_ferrate,
        nameResourcesId = R.string.via_ferrata,
        icon = Icons.Filled.LocationOn
    ), Parcelable

    companion object {
        val items = listOf<FilterItem>(Crag, Multipitches, Bouldering, Indoor, ViaFerrata)
    }
}