package at.aau.serg.sdlapp.model.player

import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.*
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.test.assertFailsWith

class PlayerRepositoryTest {

    private lateinit var mockConnection: HttpURLConnection

    private val dummyPlayerJson = """
        {
            "id": "test123",
            "money": 5000,
            "investments": 2,
            "salary": 3000,
            "children": 1,
            "education": true,
            "relationship": false
        }
    """.trimIndent()

    @Before
    fun setUp() {
        mockConnection = mockk(relaxed = true)
        mockkStatic(URL::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `fetchPlayerById returns valid player`() = runBlocking {
        every { URL(any()).openConnection() } returns mockConnection
        every { mockConnection.responseCode } returns 200
        every { mockConnection.inputStream } returns ByteArrayInputStream(dummyPlayerJson.toByteArray())

        val player = PlayerRepository.fetchPlayerById("test123")

        Assert.assertEquals("test123", player.id)
        Assert.assertEquals(5000, player.money)
        Assert.assertTrue(player.education)
        Assert.assertFalse(player.relationship)
        Assert.assertEquals(2, player.investments)
    }

    @Test
    fun `createPlayer sends POST request and parses response`() = runBlocking {
        val player = PlayerModell(
            id = "create123",
            money = 10000,
            investments = 1,
            salary = 5000,
            children = 0,
            education = false,
            relationship = true
        )
        val responseJson = Json.encodeToString(player)

        every { URL(any()).openConnection() } returns mockConnection
        every { mockConnection.requestMethod = any() } just Runs
        every { mockConnection.setRequestProperty(any(), any()) } just Runs
        every { mockConnection.doOutput = any() } just Runs
        every { mockConnection.outputStream } returns mockk(relaxed = true)
        every { mockConnection.responseCode } returns HttpURLConnection.HTTP_OK
        every { mockConnection.inputStream } returns ByteArrayInputStream(responseJson.toByteArray())

        val created = PlayerRepository.createPlayer(player)

        Assert.assertEquals("create123", created.id)
        Assert.assertEquals(10000, created.money)
        Assert.assertTrue(created.relationship)
    }

    @Test
    fun `marryPlayer calls makePutRequest`() = runBlocking {
        mockSuccessfulPut()
        PlayerRepository.marryPlayer("marry123")
    }

    @Test
    fun `addChild calls makePutRequest`() = runBlocking {
        mockSuccessfulPut()
        PlayerRepository.addChild("child123")
    }

    @Test
    fun `investForPlayer calls makePutRequest`() = runBlocking {
        mockSuccessfulPut()
        PlayerRepository.investForPlayer("invest123")
    }

    @Test
    fun `makePutRequest throws exception on failure`() = runBlocking {
        every { URL(any()).openConnection() } returns mockConnection
        every { mockConnection.requestMethod = "PUT" } just Runs
        every { mockConnection.responseCode } returns HttpURLConnection.HTTP_INTERNAL_ERROR
        every { mockConnection.responseMessage } returns "Server Error"

        val ex = assertFailsWith<RuntimeException> {
            PlayerRepository.addChild("invalid123")
        }
        Assert.assertTrue(ex.message!!.contains("Fehler bei PUT"))
    }

    private fun mockSuccessfulPut() {
        every { URL(any()).openConnection() } returns mockConnection
        every { mockConnection.requestMethod = "PUT" } just Runs
        every { mockConnection.responseCode } returns HttpURLConnection.HTTP_OK
    }
}
