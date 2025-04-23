package at.aau.serg.websocketbrokerdemo.model

data class JobMessage(
    val jobId: Int,
    val title: String,
    val salary: Int,
    val bonusSalary: Int,
    val requiresHighSchoolDiploma: Boolean,
    val isTaken: Boolean,
    val takenByPlayerName: String?,  // Nullable if not taken
    val playerName: String,
    val timestamp: String
)
