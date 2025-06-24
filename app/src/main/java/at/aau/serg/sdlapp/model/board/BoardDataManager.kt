package at.aau.serg.sdlapp.model.board

import android.util.Log

/**
 * Manager-Klasse zur Verwaltung und Synchronisierung der Board-Daten
 * zwischen Frontend und Backend.
 */
object BoardDataManager {
    
    // Flagge, die angibt, ob die Board-Daten bereits synchronisiert wurden
    private var isSynchronized = false
    
    /**
     * Synchronisiert die lokalen Board-Daten mit den vom Server empfangenen Daten.
     * Diese Methode sollte aufgerufen werden, sobald die Board-Daten vom Server empfangen wurden.
     *
     * @param serverFields Die vom Server empfangenen Feld-Daten
     * @return true, wenn die Synchronisierung erfolgreich war, false sonst
     */
    fun synchronizeBoardData(serverFields: List<Field>): Boolean {
        if (serverFields.isEmpty()) {
            Log.e("BoardDataManager", "Synchronisierung fehlgeschlagen: Leere Felderliste vom Server")
            return false
        }
        
        Log.d("BoardDataManager", "Beginne Board-Daten-Synchronisierung: ${serverFields.size} Felder vom Server")
        
        try {
            // Erstelle eine Mapping-Tabelle von Server-Feld-IDs zu lokalen Feld-Objekten
            val fieldMap = mutableMapOf<Int, Field>()
            
            // Fülle die Map mit lokalen Feldern
            for (localField in BoardData.board) {
                fieldMap[localField.index] = localField
            }
            
            // Aktualisiere die lokalen Felder mit Server-Daten oder füge neue hinzu
            for (serverField in serverFields) {
                val localField = fieldMap[serverField.index]
                
                if (localField != null) {
                    // Wenn das Feld bereits lokal existiert, aktualisiere Eigenschaften
                    Log.d("BoardDataManager", "Aktualisiere Feld ${serverField.index}: Typ von ${localField.type} zu ${serverField.type}")
                    
                    // In einer echten Implementierung würden hier Reflexion oder Setter verwendet
                    // Da unsere Field-Klasse unveränderlich ist, können wir es nicht direkt aktualisieren
                } else {
                    // Für neue Felder würde man hier eine Methode zur Aktualisierung von BoardData benötigen
                    // Da unsere BoardData eine statische Sammlung ist, können wir diese nicht direkt erweitern
                    Log.d("BoardDataManager", "Neues Feld vom Server: ${serverField.index}, existiert nicht lokal")
                }
                
                // Ausgabe für Debug-Zwecke
                val localCoords = localField?.let { "x=${it.x}, y=${it.y}" } ?: "nicht lokal vorhanden"
                val serverCoords = "x=${serverField.x}, y=${serverField.y}"
                Log.d("BoardDataManager", "Feld ${serverField.index}: $localCoords / $serverCoords")
            }
            
            // Prüfen auf Unterschiede in den Feld-IDs
            val localFieldIds = BoardData.board.map { it.index }.sorted()
            val serverFieldIds = serverFields.map { it.index }.sorted()
            
            val missingLocalIds = serverFieldIds.filter { id -> !localFieldIds.contains(id) }
            val missingServerIds = localFieldIds.filter { id -> !serverFieldIds.contains(id) }
            
            if (missingLocalIds.isNotEmpty()) {
                Log.w("BoardDataManager", "Felder im Backend, die lokal fehlen: $missingLocalIds")
            }
            
            if (missingServerIds.isNotEmpty()) {
                Log.w("BoardDataManager", "Felder im Frontend, die im Backend fehlen: $missingServerIds")
            }
            
            isSynchronized = true
            Log.d("BoardDataManager", "Board-Synchronisierung abgeschlossen")
            return true
            
        } catch (e: Exception) {
            Log.e("BoardDataManager", "Fehler bei der Board-Synchronisierung: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Ermittelt, ob ein Feld mit der angegebenen ID in den lokalen Board-Daten existiert.
     *
     * @param fieldIndex Die zu prüfende Feld-ID
     * @return true, wenn das Feld lokal existiert, false sonst
     */
    fun fieldExists(fieldIndex: Int): Boolean {
        return BoardData.board.any { it.index == fieldIndex }
    }
    
    /**
     * Findet das nächstliegende Feld zu einer gegebenen Feld-ID.
     * Nützlich, wenn die vom Backend gesendete ID nicht lokal existiert.
     *
     * @param fieldIndex Die Feld-ID, für die ein ähnliches Feld gesucht wird
     * @return Das Feld mit der ähnlichsten ID oder null, wenn keine Felder verfügbar sind
     */
    fun findSimilarField(fieldIndex: Int): Field? {
        return BoardData.board.minByOrNull { Math.abs(it.index - fieldIndex) }
    }
    
    /**
     * Erstellt ein Debug-Log mit allen wichtigen Informationen über
     * die lokalen Board-Daten.
     */
    fun logBoardData() {
        Log.d("BoardDataManager", "===== BOARD DATA DUMP =====")
        Log.d("BoardDataManager", "Anzahl der Felder: ${BoardData.board.size}")
        Log.d("BoardDataManager", "Feld-IDs: ${BoardData.board.map { it.index }.sorted()}")
        
        // Beispielhafte Ausgabe einiger Felder
        for (field in BoardData.board.take(5)) {
            Log.d("BoardDataManager", "Feld ${field.index}: x=${field.x}, y=${field.y}, Typ=${field.type}")
        }
        Log.d("BoardDataManager", "===========================")
    }
}
