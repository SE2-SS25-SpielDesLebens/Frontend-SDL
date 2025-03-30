package at.aau.serg.websocketbrokerdemo

import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import at.aau.serg.websocketbrokerdemo.network.MyStomp
import com.example.myapplication.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import org.mockito.kotlin.argumentCaptor

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class MainActivityTest {

    private lateinit var activity: MainActivity

    @Mock
    private lateinit var mockStomp: MyStomp

    @Mock
    private lateinit var mockConnectButton: Button

    @Mock
    private lateinit var mockHelloButton: Button

    @Mock
    private lateinit var mockJsonButton: Button

    @Mock
    private lateinit var mockStatusText: TextView

    @Mock
    private lateinit var mockResources: Resources

    @Mock
    private lateinit var mockTheme: Resources.Theme

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        activity = spy(MainActivity())

        // Mock findViewById für alle UI-Elemente
        doReturn(mockConnectButton).`when`(activity).findViewById<Button>(R.id.connectbtn)
        doReturn(mockHelloButton).`when`(activity).findViewById<Button>(R.id.hellobtn)
        doReturn(mockJsonButton).`when`(activity).findViewById<Button>(R.id.jsonbtn)
        doReturn(mockStatusText).`when`(activity).findViewById<TextView>(R.id.statusText)

        // Mock für Resources
        doReturn(mockResources).`when`(activity).resources
        doReturn(mockTheme).`when`(activity).theme
        doReturn(123).`when`(mockResources).getColor(anyInt(), any())

        // Injektion des Mock MyStomp
        val stompField = MainActivity::class.java.getDeclaredField("stomp")
        stompField.isAccessible = true
        stompField.set(activity, mockStomp)
    }

    @Test
    fun `test onCreate initializes UI and sets click listeners`() {
        // OnClickListener für Buttons auslösen und speichern
        var connectListener: View.OnClickListener? = null
        var helloListener: View.OnClickListener? = null
        var jsonListener: View.OnClickListener? = null

        doAnswer { invocation ->
            connectListener = invocation.arguments[0] as View.OnClickListener
            null
        }.`when`(mockConnectButton).setOnClickListener(any())

        doAnswer { invocation ->
            helloListener = invocation.arguments[0] as View.OnClickListener
            null
        }.`when`(mockHelloButton).setOnClickListener(any())

        doAnswer { invocation ->
            jsonListener = invocation.arguments[0] as View.OnClickListener
            null
        }.`when`(mockJsonButton).setOnClickListener(any())

        // onCreate aufrufen
        val onCreateMethod = MainActivity::class.java.getDeclaredMethod("onCreate", Bundle::class.java)
        onCreateMethod.isAccessible = true
        onCreateMethod.invoke(activity, null)

        // Verifizieren, dass setContentView aufgerufen wurde
        verify(activity).setContentView(R.layout.fragment_fullscreen)

        // Prüfen, ob alle OnClickListener gesetzt wurden
        verify(mockConnectButton).setOnClickListener(any())
        verify(mockHelloButton).setOnClickListener(any())
        verify(mockJsonButton).setOnClickListener(any())

        // OnClickListener auslösen und verifizieren
        connectListener?.onClick(mockConnectButton)
        verify(mockStomp).connect()

        helloListener?.onClick(mockHelloButton)
        verify(mockStomp).sendMove("Anna", "würfelt 6")

        jsonListener?.onClick(mockJsonButton)
        verify(mockStomp).sendChat("Anna", "Hallo an alle!")
    }

    @Test
    fun `test onResponse with connected message`() {
        val response = "Verbunden zum Server"

        activity.onResponse(response)

        // Korrigierter ArgumentCaptor mit Import von org.mockito.kotlin
        val runnableCaptor = argumentCaptor<Runnable>()
        verify(activity).runOnUiThread(runnableCaptor.capture())
        runnableCaptor.firstValue.run()

        verify(mockStatusText).text = "🟢 $response"
        verify(mockStatusText).setTextColor(anyInt())
    }

    @Test
    fun `test onResponse with disconnected message`() {
        val response = "Nicht verbunden zum Server"

        activity.onResponse(response)

        val runnableCaptor = argumentCaptor<Runnable>()
        verify(activity).runOnUiThread(runnableCaptor.capture())
        runnableCaptor.firstValue.run()

        verify(mockStatusText).text = "🔴 $response"
        verify(mockStatusText).setTextColor(anyInt())
    }

    @Test
    fun `test onResponse with error message`() {
        val response = "Fehler beim Verbinden"

        // Mock für Toast
        val originalToast = Toast::class.java
        doNothing().`when`(activity).runOnUiThread(any())

        activity.onResponse(response)

        val runnableCaptor = argumentCaptor<Runnable>()
        verify(activity).runOnUiThread(runnableCaptor.capture())
        runnableCaptor.firstValue.run()

        verify(mockStatusText).text = "🟠 $response"
        verify(mockStatusText).setTextColor(anyInt())
    }

    @Test
    fun `test onResponse with other message`() {
        val response = "Sonstige Nachricht"

        activity.onResponse(response)

        val runnableCaptor = argumentCaptor<Runnable>()
        verify(activity).runOnUiThread(runnableCaptor.capture())
        runnableCaptor.firstValue.run()

        verify(mockStatusText).text = "ℹ️ $response"
        verify(mockStatusText).setTextColor(anyInt())
    }

    @Test
    fun `test updateStatus sets text and color correctly`() {
        val testText = "Test Status"
        val testColor = R.color.black

        // Private Methode mit Reflection testen
        val method = MainActivity::class.java.getDeclaredMethod("updateStatus", String::class.java, Int::class.java)
        method.isAccessible = true
        method.invoke(activity, testText, testColor)

        verify(mockStatusText).text = testText
        verify(mockResources).getColor(eq(testColor), eq(mockTheme))
        verify(mockStatusText).setTextColor(anyInt())
    }
}