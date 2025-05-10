package at.aau.serg.sdlapp.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service zum Starten und Überwachen des Backend-Servers
 * Nur für Testzwecke in der Entwicklung
 */
public class ServerStartupService {
    private static final String TAG = "ServerStartup";
    private static final String JAR_NAME = "WebSocket-Server-0.0.1-SNAPSHOT.jar";
    private static final int MAX_RETRY_COUNT = 3;
    
    private Process serverProcess;
    private boolean isRunning = false;
    private OnServerStatusListener listener;
    
    public interface OnServerStatusListener {
        void onServerStarted();
        void onServerError(String errorMessage);
    }
    
    public ServerStartupService(OnServerStatusListener listener) {
        this.listener = listener;
    }
    
    /**
     * Startet den Backend-Server
     * @param jarPath Pfad zur JAR-Datei
     */
    public void startServer(String jarPath) {
        if (isRunning) {
            Log.d(TAG, "Server läuft bereits");
            if (listener != null) {
                listener.onServerStarted();
            }
            return;
        }
        
        new ServerStartTask().execute(jarPath);
    }
    
    /**
     * Stoppt den Backend-Server
     */
    public void stopServer() {
        if (serverProcess != null) {
            Log.d(TAG, "Stoppe Server...");
            serverProcess.destroy();
            try {
                serverProcess.waitFor(5, TimeUnit.SECONDS);
                if (serverProcess.isAlive()) {
                    serverProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Fehler beim Warten auf Server-Shutdown", e);
            }
            serverProcess = null;
        }
        
        isRunning = false;
    }
    
    /**
     * Prüft, ob der Server läuft
     */
    public boolean isServerRunning() {
        return isRunning;
    }
    
    /**
     * Findet den Pfad zur Server-JAR-Datei im Dateisystem
     * @param context Android-Kontext
     * @return Pfad oder null, wenn nicht gefunden
     */
    public static String findServerJarPath(Context context) {
        // In einer echten App würden wir die JAR-Datei im Assets-Ordner speichern
        // und bei Bedarf extrahieren. Für Test-Zwecke sollte die JAR manuell auf das
        // Gerät/Emulator kopiert werden.
        
        // Prüfe übliche Speicherorte
        List<String> possibleLocations = new ArrayList<>();
        
        // Externe Speicherorte
        File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir != null) {
            possibleLocations.add(externalFilesDir.getAbsolutePath() + File.separator + JAR_NAME);
        }
        
        // App-spezifischer Speicher
        possibleLocations.add(context.getFilesDir() + File.separator + JAR_NAME);
        
        // Durchsuche alle möglichen Orte
        for (String path : possibleLocations) {
            File jarFile = new File(path);
            if (jarFile.exists() && jarFile.isFile()) {
                Log.d(TAG, "Server JAR gefunden unter: " + path);
                return path;
            }
        }
        
        Log.w(TAG, "Server JAR konnte nicht gefunden werden");
        return null;
    }
    
    /**
     * AsyncTask zum Starten des Servers
     */
    private class ServerStartTask extends AsyncTask<String, String, Boolean> {
        private String errorMessage;
        
        @Override
        protected Boolean doInBackground(String... paths) {
            if (paths.length == 0 || paths[0] == null) {
                errorMessage = "Kein Pfad zur JAR-Datei angegeben";
                return false;
            }
            
            String jarPath = paths[0];
            File jarFile = new File(jarPath);
            if (!jarFile.exists()) {
                errorMessage = "JAR-Datei nicht gefunden: " + jarPath;
                return false;
            }
            
            int retryCount = 0;
            while (retryCount < MAX_RETRY_COUNT) {
                try {
                    // Starte den Java-Prozess
                    ProcessBuilder processBuilder = new ProcessBuilder(
                            "java", "-jar", jarPath);
                    
                    // Umgebungsvariablen setzen
                    processBuilder.environment().put("PORT", "8080");
                    
                    processBuilder.redirectErrorStream(true);
                    
                    publishProgress("Starte Server...");
                    serverProcess = processBuilder.start();
                    
                    // Ausgabe des Servers lesen
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(serverProcess.getInputStream()));
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Log.d(TAG, "Server: " + line);
                        publishProgress(line);
                        
                        // Prüfe, ob der Server gestartet ist
                        if (line.contains("Started WebSocketServerApplication") ||
                            line.contains("Tomcat started on port(s): 8080")) {
                            isRunning = true;
                            return true;
                        }
                    }
                    
                    // Wenn der Prozess beendet ist, prüfe den Exit-Code
                    int exitCode = serverProcess.waitFor();
                    errorMessage = "Server beendet mit Exit-Code: " + exitCode;
                    Log.e(TAG, errorMessage);
                    
                    // Warte vor dem nächsten Versuch
                    Thread.sleep(1000);
                    retryCount++;
                    
                } catch (IOException | InterruptedException e) {
                    errorMessage = "Fehler beim Starten des Servers: " + e.getMessage();
                    Log.e(TAG, errorMessage, e);
                    return false;
                }
            }
            
            return false;
        }
        
        @Override
        protected void onProgressUpdate(String... values) {
            for (String message : values) {
                Log.d(TAG, message);
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Log.d(TAG, "Server erfolgreich gestartet");
                if (listener != null) {
                    listener.onServerStarted();
                }
            } else {
                Log.e(TAG, "Server konnte nicht gestartet werden: " + errorMessage);
                if (listener != null) {
                    listener.onServerError(errorMessage);
                }
            }
        }
    }
}
