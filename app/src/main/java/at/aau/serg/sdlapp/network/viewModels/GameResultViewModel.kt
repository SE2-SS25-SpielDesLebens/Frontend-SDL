package at.aau.serg.sdlapp.network.viewModels

import androidx.lifecycle.ViewModel
import at.aau.serg.sdlapp.model.player.Player
import at.aau.serg.sdlapp.model.player.PlayerManager

class GameResultViewModel : ViewModel() {

    private val players: List<Player> = PlayerManager.getAllPlayers()

    val sortedPlayers = players.sortedByDescending { it.money + it.investments }

    val mostChildren: String
        get() = players.maxByOrNull { it.children }?.name ?: "-"

    val topInvestor: String
        get() = players.maxByOrNull { it.investments }?.name ?: "-"

    val highestSalary: String
        get() = players.maxByOrNull { it.salary }?.name ?: "-"

    val academics: String
        get() = players.filter { it.hasEducation }.joinToString { it.name }.ifBlank { "–" }

    fun clearGameData() {
        PlayerManager.clearPlayers()
    }
}
