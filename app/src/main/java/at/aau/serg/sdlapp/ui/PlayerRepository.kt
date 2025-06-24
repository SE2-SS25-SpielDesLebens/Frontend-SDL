package at.aau.serg.sdlapp.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

object PlayerRepository {

    private const val BASE_URL = "http://se2-demo.aau.at:53217/players"

    // ✅ JSON-Konfiguration: Unbekannte Keys ignorieren
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }


    // ✅ Ein einzelner Spieler nach ID
    suspend fun fetchPlayerById(id: String): PlayerModell {
        return withContext(Dispatchers.IO) {
            val url = URL("$BASE_URL/$id")
            println("🌐 Anfrage an: $url")

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            val responseCode = connection.responseCode
            println("📡 HTTP-Status: $responseCode")

            try {
                println("📥 Verbindung hergestellt. Versuche zu lesen...")

                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                println("📦 JSON empfangen: $jsonText")

                val player = json.decodeFromString<PlayerModell>(jsonText)
                println("✅ JSON erfolgreich geparsed für Spieler: ${player.id}")

                player
            } catch (e: Exception) {
                println("❌ Fehler beim Abrufen oder Parsen: ${e.message}")
                throw e
            }
        }
    }





    // ✅ Liste aller Spieler
    suspend fun fetchAllPlayers(): List<PlayerModell> {
        return withContext(Dispatchers.IO) {
            val url = URL(BASE_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            connection.inputStream.bufferedReader().use {
                json.decodeFromString(it.readText())
            }
        }
    }

    // ✅ Spieler erstellen
    suspend fun createPlayer(player: PlayerModell): PlayerModell {
        return withContext(Dispatchers.IO) {
            val url = URL(BASE_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val jsonBody = json.encodeToString(player)
            connection.outputStream.use { it.write(jsonBody.toByteArray()) }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("Fehler beim Erstellen des Spielers: ${connection.responseMessage}")
            }

            connection.inputStream.bufferedReader().use {
                json.decodeFromString(it.readText())
            }
        }
    }

    // ✅ PUT-Endpunkte
    suspend fun marryPlayer(playerId: String) {
        makePutRequest("$BASE_URL/$playerId/marry")
    }

    suspend fun addChild(playerId: String) {
        makePutRequest("$BASE_URL/$playerId/add-child")
    }

    suspend fun investForPlayer(playerId: String) {
        makePutRequest("$BASE_URL/$playerId/invest")
    }

    // ✅ Hilfsfunktion für PUT
    private suspend fun makePutRequest(urlString: String) {
        withContext(Dispatchers.IO) {
            val connection = URL(urlString).openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("Fehler bei PUT $urlString: ${connection.responseMessage}")
            }
        }
    }
}
