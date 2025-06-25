package at.aau.serg.sdlapp.network.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.sdlapp.model.player.PlayerModell
import at.aau.serg.sdlapp.model.player.PlayerRepository
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {
    var player by mutableStateOf<PlayerModell?>(null)

    var allPlayers by mutableStateOf<List<PlayerModell>>(emptyList())

    fun loadPlayer(id: String) {
        viewModelScope.launch {
            try {
                val loadedPlayer = PlayerRepository.fetchPlayerById(id)
                println("🟢 Spieler erfolgreich geladen: ${loadedPlayer.id}")
                player = loadedPlayer
            } catch (e: Exception) {
                println("🔴 Fehler beim Laden des Spielers mit ID $id: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun loadAllPlayers() {
        viewModelScope.launch {
            try {
                allPlayers = PlayerRepository.fetchAllPlayers()
            } catch (e: Exception) {
                println("Fehler beim Laden aller Spieler: ${e.message}")
            }
        }
    }


}