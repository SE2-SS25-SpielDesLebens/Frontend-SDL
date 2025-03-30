import android.os.Handler
import android.os.Looper
import android.util.Log
import at.aau.serg.websocketbrokerdemo.Callbacks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import org.json.JSONObject


private  val WEBSOCKET_URI = "ws://10.0.2.2:8080/ws";
class MyStomp(val callbacks: Callbacks) {

    private lateinit var topicFlow: Flow<String>
    private lateinit var collector:Job

    private lateinit var jsonFlow: Flow<String>
    private lateinit var jsonCollector:Job

    private lateinit var client:StompClient
    private lateinit var session: StompSession

    private val scope:CoroutineScope=CoroutineScope(Dispatchers.IO)
    private var connected = false

    fun connect() {
        client = StompClient(OkHttpWebSocketClient())
        scope.launch {
            try {
                session = client.connect(WEBSOCKET_URI)
                // Abonnements einrichten
                topicFlow = session.subscribeText("/topic/hello-response")
                collector = scope.launch {
                    topicFlow.collect { msg ->
                        callback(msg)
                    }
                }
                jsonFlow = session.subscribeText("/topic/rcv-object")
                jsonCollector = scope.launch {
                    jsonFlow.collect { msg ->
                        val o = JSONObject(msg)
                        callback(o.get("text").toString())
                    }
                }
                connected = true  // Nur hier setzen, wenn die Verbindung erfolgreich ist
                callback("connected")
            } catch (e: Exception) {
                Log.e("MyStomp", "Error connecting: ${e.localizedMessage}")
                callback("Error connecting: ${e.localizedMessage}")
            }
        }
    }
    private fun callback(msg:String){
        Handler(Looper.getMainLooper()).post{
            callbacks.onResponse(msg)
        }
    }
    fun sendHello() {
        scope.launch {
            if (connected) {  // Nur senden, wenn die Session initialisiert wurde
                Log.e("tag", "sending hello")
                session.sendText("/app/hello", "message from client")
            } else {
                Log.e("tag", "session not connected yet!")
                callback("session not connected yet!")
            }
        }
    }

    fun sendJson(){
        val json=JSONObject();
        json.put("from","client")
        json.put("text","from client")
        val o=json.toString()

        scope.launch {
            session.sendText("/app/object",o);
        }

    }

}