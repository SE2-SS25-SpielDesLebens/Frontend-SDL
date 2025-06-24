package at.aau.serg.sdlapp.ui

import kotlinx.serialization.Serializable

@Serializable
data class PlayerModell(
    val id: String,
    var money: Int,
    var investments: Int,
    var salary: Int,
    var children: Int,
    var education: Boolean,
    var relationship: Boolean = false,
    var jobId: Int? = null,
    var houseId: Map<Int, Int> = emptyMap(),
    var carColor: String? = null,
    var retired: Boolean = false,
    var host: Boolean = false,
    var active: Boolean = false,
    var fieldId: Int = 0
)

