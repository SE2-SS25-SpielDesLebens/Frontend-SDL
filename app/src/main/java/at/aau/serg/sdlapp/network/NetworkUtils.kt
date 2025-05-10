package at.aau.serg.sdlapp.network

import android.util.Log
import java.net.InetSocketAddress
import java.net.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Hilfsmethoden für die Netzwerkkommunikation
 */
object NetworkUtils {
    private const val TAG = "NetworkUtils"
    private const val CONNECTION_TIMEOUT_MS = 2000 // 2 seconds
    
    /**
     * Testet die Verbindung zu mehreren möglichen Server-Adressen
     * 
     * @param port Der zu testende Port
     * @return Map von Server-Adresse zu Erreichbarkeitsstatus
     */
    suspend fun testConnections(port: Int): Map<String, Boolean> = withContext(Dispatchers.IO) {
        val hosts = listOf(
            "10.0.2.2", // Emulator -> Host
            "localhost",
            "127.0.0.1"
        )
        
        val results = mutableMapOf<String, Boolean>()
        
        for (host in hosts) {
            val reachable = isServerReachable(host, port)
            results[host] = reachable
            Log.d(TAG, "Server $host:$port ist ${if (reachable) "erreichbar" else "nicht erreichbar"}")
        }
        
        results
    }
    
    /**
     * Prüft, ob ein Server unter der angegebenen Adresse und Port erreichbar ist
     * 
     * @param host Hostname oder IP-Adresse
     * @param port Port
     * @return true wenn der Server erreichbar ist, sonst false
     */
    private fun isServerReachable(host: String, port: Int): Boolean {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port), CONNECTION_TIMEOUT_MS)
            socket.close()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Verbindung zu $host:$port fehlgeschlagen: ${e.message}")
            false
        }
    }
}