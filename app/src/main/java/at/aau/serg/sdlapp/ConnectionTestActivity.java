package at.aau.serg.sdlapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import at.aau.serg.sdlapp.network.GameClient;
import at.aau.serg.sdlapp.network.GameState;
import at.aau.serg.sdlapp.network.GameStateListener;
import java.util.List;

/**
 * Test-Activity für die Verbindung zwischen Frontend und Backend
 */
public class ConnectionTestActivity extends AppCompatActivity implements GameStateListener {
    private static final String TAG = "ConnectionTest";
    
    private GameClient gameClient;
    private TextView statusTextView;
    private TextView gameStateTextView;
    private Button connectButton;
    private Button joinGameButton;
    private Button movePlayerButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_test);
        
        // UI-Elemente referenzieren
        statusTextView = findViewById(R.id.statusTextView);
        gameStateTextView = findViewById(R.id.gameStateTextView);
        connectButton = findViewById(R.id.connectButton);
        joinGameButton = findViewById(R.id.joinGameButton);
        movePlayerButton = findViewById(R.id.movePlayerButton);
        
        // GameClient initialisieren
        String playerName = "TestSpieler";
        String gameId = "testgame-" + System.currentTimeMillis();
        gameClient = new GameClient(playerName, gameId, this);
        
        // Button-Actions setzen
        connectButton.setOnClickListener(v -> {
            updateStatus("Verbinde...");
            gameClient.connect();
            connectButton.setEnabled(false);
        });
        
        joinGameButton.setOnClickListener(v -> {
            updateStatus("Trete Spiel bei...");
            gameClient.joinGame(0); // Starte bei Position 0 (normal)
            joinGameButton.setEnabled(false);
        });
        
        movePlayerButton.setOnClickListener(v -> {
            updateStatus("Bewege Spieler...");
            gameClient.movePlayer(2); // Bewege um 2 Felder
        });
    }
    
    @Override
    public void onGameStateUpdated(GameState gameState) {
        runOnUiThread(() -> {
            updateStatus("Spielstatus aktualisiert");
            gameStateTextView.setText("Spielstatus:\n" + 
                                    "Spielerpositionen: " + gameState.getPlayerPositions() + "\n" +
                                    "Aktueller Spieler: " + gameState.getCurrentPlayer() + "\n" +
                                    "Status: " + gameState.getGameStatus());
            
            joinGameButton.setEnabled(true);
            movePlayerButton.setEnabled(true);
        });
    }
    
    @Override
    public void onChoiceRequired(List<Integer> options) {
        runOnUiThread(() -> {
            updateStatus("Auswahl erforderlich");
            String optionsText = "Wähle ein Feld: " + options;
            Toast.makeText(this, optionsText, Toast.LENGTH_LONG).show();
            
            // In einer richtigen App würden wir hier UI-Elemente für die Auswahl anzeigen
        });
    }
    
    @Override
    public void onError(String message) {
        runOnUiThread(() -> {
            updateStatus("Fehler: " + message);
            connectButton.setEnabled(true);
        });
    }
    
    private void updateStatus(String status) {
        Log.d(TAG, status);
        statusTextView.setText(status);
    }
    
    @Override
    protected void onDestroy() {
        if (gameClient != null) {
            gameClient.disconnect();
        }
        super.onDestroy();
    }
}
