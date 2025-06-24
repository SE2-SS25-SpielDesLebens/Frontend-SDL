package at.aau.serg.sdlapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp


data class FieldUI(
    val id: Int,
    val type: FieldType,
    val label: String
)
 //Aufzählung der Feldtypen
enum class FieldType{
    PAYDAY,ACTION,FAMILY,HOUSE,CAREER,INVESTMENT, NEUTRAL, RETIREMENT

}

//Einzelnes Feld, mit Farbe und Text dargestellt
@Composable
fun GameField(field: FieldUI, modifier: Modifier = Modifier) {
    // Farbe je nach Feldtyp
    val backgroundColor = when (field.type) {
        FieldType.PAYDAY -> Color(0xFFA5D6A7)
        FieldType.ACTION -> Color(0xFFFFF59D)
        FieldType.CAREER -> Color(0xFFFFCDD2)
        FieldType.INVESTMENT -> Color(0xFFFFAB91)
        FieldType.HOUSE -> Color(0xFF90CAF9)
        FieldType.FAMILY -> Color(0xFFD1C4E9)
        FieldType.NEUTRAL -> Color.Gray
        FieldType.RETIREMENT -> Color(0xFFB0BEC5)
    }
    // Layout eines einzelnen Feldes
    Box(
        modifier = modifier
            .size(100.dp)
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color.Black, shape = RoundedCornerShape(8.dp))
            .testTag("GameField"), // 👉 wichtig für UI-Tests
        contentAlignment = Alignment.Center
    ) {
        Text(field.label, style = MaterialTheme.typography.bodySmall)
    }
}
@Composable
fun GameBoard(fields: List<FieldUI>) {
    LazyVerticalGrid(columns = GridCells.Fixed(5), modifier = Modifier.fillMaxSize()) {
        items(fields) { field ->
            GameField(field = field, modifier = Modifier.padding(4.dp))
        }
    }
}

val sampleFields = listOf(
    FieldUI(0, FieldType.NEUTRAL, "Start"),
    FieldUI(1, FieldType.PAYDAY, "Zahltag"),
    FieldUI(2, FieldType.ACTION, "Aktion"),
    FieldUI(3, FieldType.CAREER, "Beruf"),
    FieldUI(4, FieldType.INVESTMENT, "Geldanlage"),
    FieldUI(5, FieldType.FAMILY, "Stiftfeld"),
    FieldUI(6, FieldType.HOUSE, "Haus"),
    FieldUI(7, FieldType.RETIREMENT, "Ruhestand")
)

