package at.aau.serg.sdlapp.model.player

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PlayerRepositoryTest {

    private lateinit var server: MockWebServer

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()

        // BASE_URL im Repository temporär überschreiben
        val baseUrlField = PlayerRepository::class.java.getDeclaredField("BASE_URL")
        baseUrlField.isAccessible = true
        baseUrlField.set(null, server.url("/players").toString().removeSuffix("/"))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `fetchPlayerById returns valid player`() = runBlocking {
        val responseJson = """
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

        server.enqueue(MockResponse().setBody(responseJson).setResponseCode(200))

        val player = PlayerRepository.fetchPlayerById("test123")

        assertEquals("test123", player.id)
        assertEquals(5000, player.money)
        assertTrue(player.education)
    }

    @Test
    fun `createPlayer sends POST request`() = runBlocking {
        val player = PlayerModell(
            id = "abc",
            money = 2000,
            investments = 1,
            salary = 1000,
            children = 0,
            education = true,
            relationship = false
        )

        server.enqueue(MockResponse().setBody(Json.encodeToString(player)).setResponseCode(200))

        val result = PlayerRepository.createPlayer(player)

        assertEquals("abc", result.id)
        assertEquals(2000, result.money)

        val recordedRequest = server.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertTrue(recordedRequest.body.readUtf8().contains("abc"))
    }

    @Test
    fun `makePutRequest throws exception on error`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("Error"))

        val exception = assertFailsWith<RuntimeException> {
            PlayerRepository.marryPlayer("fail")
        }
        assertTrue(exception.message!!.contains("Fehler bei PUT"))
    }
    @Test
    fun `fetchAllPlayers returns list of players`() = runBlocking {
        val responseJson = """
        [
            {
                "id": "p1",
                "money": 1000,
                "investments": 1,
                "salary": 500,
                "children": 0,
                "education": true,
                "relationship": false
            },
            {
                "id": "p2",
                "money": 2000,
                "investments": 2,
                "salary": 800,
                "children": 1,
                "education": false,
                "relationship": true
            }
        ]
    """.trimIndent()

        server.enqueue(MockResponse().setBody(responseJson).setResponseCode(200))

        val result = PlayerRepository.fetchAllPlayers()

        assertEquals(2, result.size)
        assertEquals("p1", result[0].id)
        assertEquals("p2", result[1].id)
    }

    @Test
    fun `marryPlayer sends PUT request successfully`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200))

        PlayerRepository.marryPlayer("p1")

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertTrue(request.path!!.contains("/p1/marry"))
    }

    @Test
    fun `addChild sends PUT request successfully`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200))

        PlayerRepository.addChild("p2")

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertTrue(request.path!!.contains("/p2/add-child"))
    }

    @Test
    fun `investForPlayer sends PUT request successfully`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200))

        PlayerRepository.investForPlayer("p3")

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertTrue(request.path!!.contains("/p3/invest"))
    }

    @Test
    fun `createPlayer throws exception on failure`() = runBlocking {
        val player = PlayerModell(
            id = "fail",
            money = 0,
            investments = 0,
            salary = 0,
            children = 0,
            education = false,
            relationship = false
        )

        server.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

        val exception = assertFailsWith<RuntimeException> {
            PlayerRepository.createPlayer(player)
        }

        assertTrue(exception.message!!.contains("Fehler beim Erstellen des Spielers"))
    }

}
