package at.aau.serg.sdlapp.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {
    var player by mutableStateOf<PlayerModell?>(null)
        private set

    fun loadPlayer(id: Int) {
        viewModelScope.launch {
            try {
                val allPlayers = PlayerRepository.fetchPlayers()
                player = allPlayers.find { it.id == id }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refresh(id: Int) {
        loadPlayer(id)
    }
}
