package at.aau.serg.sdlapp.network.viewModels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.viewModelScope
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.subscribeText
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class LobbyViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var session: StompSession
    private lateinit var viewModel: LobbyViewModel
    private val testScope = TestScope()
    private val testDispatcher = StandardTestDispatcher(testScope.testScheduler)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        session = mockk(relaxed = true)
        mockkStatic("org.hildan.krossbow.stomp.StompSessionKt")
        viewModel = LobbyViewModel(session)
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.coroutineContext.cancelChildren()
        unmockkAll()
        clearAllMocks()
        Dispatchers.resetMain()
    }

    @Test
    fun `initialize sets current player`() = runTest {
        // Given
        coEvery { session.subscribeText(any()) } returns flow { }
        
        // When
        viewModel.initialize("lobby123", "Anna")
        testScope.testScheduler.advanceUntilIdle()
        
        // Then
        assertEquals(listOf("Anna"), viewModel.players.value)
    }

    @Test
    fun `startObserving does not duplicate existing player`() = runTest {
        // Given
        val lobbyId = "lobby123"
        val sampleJson = """{"playerName":"Anna","isSuccessful":true}"""
        
        coEvery { session.subscribeText("/topic/$lobbyId") } returns flow { 
            emit(sampleJson)
        }
        
        // When
        viewModel.initialize(lobbyId, "Anna")
        testScope.testScheduler.advanceUntilIdle()
        
        // Then
        assertEquals(listOf("Anna"), viewModel.players.value)
    }

}
