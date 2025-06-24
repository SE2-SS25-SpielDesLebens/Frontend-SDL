package at.aau.serg.sdlapp.game

import at.aau.serg.sdlapp.model.game.GameConstants
import org.junit.Assert.assertTrue
import org.junit.Test

class GameConstantsTest {

    @Test
    fun testFinalFieldIndicesContainValidValues() {
        assertTrue(GameConstants.FINAL_FIELD_INDICES.contains(119))
        assertTrue(GameConstants.FINAL_FIELD_INDICES.contains(134))
    }
}
