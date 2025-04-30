package at.aau.serg.websocketbrokerdemo.ui.theme

import kotlinx.serialization.Serializable

@Serializable
data class PlayerModell(
    val name: String,
    val id: Int,
    var money: Int,
    var investments: Int,
    var salary: Int,
    var children: Int,
    var education: String,
    var relationship: String,
    var career: String
)
