package at.aau.serg.sdlapp.viewModels

import at.aau.serg.sdlapp.network.StompConnectionManager
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import at.aau.serg.sdlapp.network.viewModels.ConnectionViewModel
import io.mockk.*

class ConnectionViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule() // Ermöglicht LiveData-Synchronität im Test

    private lateinit var viewModel: ConnectionViewModel

    @Before
    fun setup() {
        // Application ist hier egal, da nicht verwendet – wir übergeben einfach ein Mock
        viewModel = ConnectionViewModel(mockk(relaxed = true))
    }

    @Test
    fun `initializeStomp should set MyStomp if null`() {
        val callback: (String) -> Unit = {}
        Assert.assertNull(viewModel.myStomp.value)

        viewModel.initializeStomp(callback)

        Assert.assertNotNull(viewModel.myStomp.value)
        Assert.assertTrue(viewModel.myStomp.value is StompConnectionManager)
    }

    @Test
    fun `initializeStomp should NOT overwrite existing MyStomp`() {
        val firstInstance = mockk<StompConnectionManager>(relaxed = true)
        viewModel.myStomp.value = firstInstance

        val callback: (String) -> Unit = { Assert.fail("Callback should not be called") }

        viewModel.initializeStomp(callback)

        Assert.assertSame(firstInstance, viewModel.myStomp.value)
    }
}