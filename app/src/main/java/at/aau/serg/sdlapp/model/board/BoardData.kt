package at.aau.serg.sdlapp.model.board


object BoardData {

    val board: List<Field> = listOf(
        // x größer ist rechts und y größer ist runter
        //StartNormal
        Field(index = 1, x = 0.1f, y = 0.65f, nextFields = listOf(2,3,4,5,6,7,8,9,20,21), type = FieldType.STARTNORMAL),
        Field(index = 2, x = 0.145f, y = 0.6205f, nextFields = listOf(3,4,5,6,7,8,9,20,21,22), type = FieldType.ZAHLTAG),
        Field(index = 3, x = 0.18f, y = 0.6f, nextFields = listOf(4,5,6,7,8,9,20,21,22,23), type = FieldType.AKTION),
        Field(index = 4, x = 0.2105f, y = 0.571f, nextFields = listOf(5,6,7,8,9,20,21,22,23,24), type = FieldType.ANLAGE),
        Field(index = 5, x = 0.243f, y = 0.598f, nextFields = listOf(6,7,8,9,20,21,22,23,24,25), type = FieldType.AKTION),
        Field(index = 6, x = 0.275f, y = 0.571f, nextFields = listOf(7,8,9,20,21,22,23,24,25,26), type = FieldType.FREUND),
        Field(index = 7, x = 0.308f, y = 0.548f, nextFields = listOf(8,9,20,21,22,23,24,25,26,27), type = FieldType.AKTION),
        Field(index = 8, x = 0.339f, y = 0.57f, nextFields = listOf(9,20,21,22,23,24,25,26,27,28), type = FieldType.BERUF),
        Field(index = 9, x = 0.3715f, y = 0.599f, nextFields = listOf(20,21,22,23,24,25,26,27,28), type = FieldType.AKTION),

// x größer ist rechts und y größer ist runter
        //StartUni
        Field(index = 10, x = 0.1f, y = 0.815f, nextFields = listOf(11,12,13,14,15,16,17,18,19), type = FieldType.STARTUNI),
        Field(index = 11, x = 0.145f, y = 0.785f, nextFields = listOf(12,13,14,15,16,17,18,19), type = FieldType.AKTION),
        Field(index = 12, x = 0.178f, y = 0.806f, nextFields = listOf(13,14,15,16,17,18,19), type = FieldType.FREUND),
        Field(index = 13, x = 0.2102f, y = 0.788f, nextFields = listOf(14,15,16,17,18,19), type = FieldType.AKTION),
        Field(index = 14, x = 0.243f, y = 0.7605f, nextFields = listOf(15,16,17,18,19), type = FieldType.FREUND),
        Field(index = 15, x = 0.275f, y = 0.735f, nextFields = listOf(16,17,18,19), type = FieldType.AKTION),
        Field(index = 16, x = 0.308f, y = 0.712f, nextFields = listOf(17,18,19), type = FieldType.AKTION),
        Field(index = 17, x = 0.339f, y = 0.738f, nextFields = listOf(18,19), type = FieldType.FREUND),
        Field(index = 18, x = 0.372f, y = 0.71f, nextFields = listOf(19), type = FieldType.AKTION),
        Field(index = 19, x = 0.405f, y = 0.68f, nextFields = listOf(20,21,22,23,24,25,26,27,28), type = FieldType.EXAMEN),
// x größer ist rechts und y größer ist runter
        //Gemeinsamer Weg zur Heirat
        Field(index = 20, x = 0.405f, y = 0.62f, nextFields = listOf(21,22,23,24,25,26,27,28), type = FieldType.ZAHLTAG),
        Field(index = 21, x = 0.435f, y = 0.595f, nextFields = listOf(22,23,24,25,26,27,28), type = FieldType.AKTION),
        Field(index = 22, x = 0.47f, y = 0.62f, nextFields = listOf(23,24,25,26,27,28), type = FieldType.HAUS),
        Field(index = 23, x = 0.5f, y = 0.595f, nextFields = listOf(24,25,26,27,28), type = FieldType.AKTION),
        Field(index = 24, x = 0.5f, y = 0.548f, nextFields = listOf(25,26,27,28), type = FieldType.ZAHLTAG),
        Field(index = 25, x = 0.47f, y = 0.523f, nextFields = listOf(26,27,28), type = FieldType.AKTION),
        Field(index = 26, x = 0.435f, y = 0.498f, nextFields = listOf(27,28), type = FieldType.FREUND),
        Field(index = 27, x = 0.405f, y = 0.473f, nextFields = listOf(28), type = FieldType.AKTION),
        Field(index = 28, x = 0.37f, y = 0.44f, nextFields = listOf(29), type = FieldType.HEIRAT),
// x größer ist rechts und y größer ist runter
        // Heirat Ja
        Field(index = 29, x = 0.37f, y = 0.44f, nextFields = listOf(30,31,32,33,34,35,36,37,44,45), type = FieldType.HEIRAT_JA),
        Field(index = 30, x = 0.335f, y = 0.445f, nextFields = listOf(31,32,33,34,35,36,37,44,45,46), type = FieldType.AKTION),
        Field(index = 31, x = 0.305f, y = 0.465f, nextFields = listOf(32,33,34,35,36,37,44,45,46,47), type = FieldType.AKTION),
        Field(index = 32, x = 0.275f, y = 0.49f, nextFields = listOf(33,34,35,36,37,44,45,46,47), type = FieldType.AKTION),
        Field(index = 33, x = 0.245f, y = 0.49f, nextFields = listOf(34,35,36,37,44,45,46,47), type = FieldType.ZAHLTAG),
        Field(index = 34, x = 0.215f, y = 0.463f, nextFields = listOf(35,36,37,44,45,46,47), type = FieldType.AKTION),
        Field(index = 35, x = 0.18f, y = 0.435f, nextFields = listOf(36,37,44,45,46,47), type = FieldType.HAUS),
        Field(index = 36, x = 0.18f, y = 0.385f, nextFields = listOf(37,44,45,46,47), type = FieldType.AKTION),
        Field(index = 37, x = 0.18f, y = 0.333f, nextFields = listOf(44,45,46,47), type = FieldType.ANLAGE),
// x größer ist rechts und y größer ist runter
        // Heirat Nein
        Field(index = 38, x = 0.37f, y = 0.44f, nextFields = listOf(39,40,41,42,43,44,45,46,47), type = FieldType.HEIRAT_NEIN),
        Field(index = 39, x = 0.373f, y = 0.39f, nextFields = listOf(40,41,42,43,44,45,46,47), type = FieldType.BERUF),
        Field(index = 40, x = 0.34f, y = 0.36f, nextFields = listOf(41,42,43,44,45,46,47), type = FieldType.AKTION),
        Field(index = 41, x = 0.31f, y = 0.335f, nextFields = listOf(42,43,44,45,46,47), type = FieldType.ZAHLTAG),
        Field(index = 42, x = 0.275f, y = 0.31f, nextFields = listOf(43,44,45,46,47), type = FieldType.AKTION),
        Field(index = 43, x = 0.245f, y = 0.335f, nextFields = listOf(44,45,46,47), type = FieldType.AKTION),
// x größer ist rechts und y größer ist runter
        // Gemeinsamer Weg zum Kind
        Field(index = 44, x = 0.213f, y = 0.305f, nextFields = listOf(45,46,47), type = FieldType.AKTION),
        Field(index = 45, x = 0.213f, y = 0.255f, nextFields = listOf(46,47), type = FieldType.HAUS),
        Field(index = 46, x = 0.245f, y = 0.23f, nextFields = listOf(47), type = FieldType.AKTION),
        Field(index = 47, x = 0.285f, y = 0.23f, nextFields = listOf(48), type = FieldType.KINDER),
// x größer ist rechts und y größer ist runter
        // Kinder Ja
        Field(index = 48, x = 0.285f, y = 0.23f, nextFields = listOf(49,50,51,52,53,54,55,56,57,58), type = FieldType.KINDER_JA),
        Field(index = 49, x = 0.315f, y = 0.265f, nextFields = listOf(50,51,52,53,54,55,56,57,58,59), type = FieldType.AKTION),
        Field(index = 50, x = 0.345f, y = 0.29f, nextFields = listOf(51,52,53,54,55,56,57,58,59,60), type = FieldType.BABY),
        Field(index = 51, x = 0.378f, y = 0.312f, nextFields = listOf(52,53,54,55,56,57,58,59,60,61), type = FieldType.AKTION),
        Field(index = 52, x = 0.41f, y = 0.29f, nextFields = listOf(53,54,55,56,57,58,59,60,61,71), type = FieldType.ZAHLTAG),
        Field(index = 53, x = 0.442f, y = 0.315f, nextFields = listOf(54,55,56,57,58,59,60,61,71,72), type = FieldType.AKTION),
        Field(index = 54, x = 0.442f, y = 0.363f, nextFields = listOf(55,56,57,58,59,60,61,71,72,73), type = FieldType.HAUS),
        Field(index = 55, x = 0.475f, y = 0.388f, nextFields = listOf(56,57,58,59,60,61,71,72,73,74), type = FieldType.AKTION),
        Field(index = 56, x = 0.508f, y = 0.413f, nextFields = listOf(57,58,59,60,61,71,72,73,74,75), type = FieldType.ZWILLINGE),
        Field(index = 57, x = 0.54f, y = 0.388f, nextFields = listOf(58,59,60,61,71,72,73,74,75,76), type = FieldType.AKTION),
        Field(index = 58, x = 0.54f, y = 0.342f, nextFields = listOf(59,60,61,71,72,73,74,75,76,77), type = FieldType.TIER),
        Field(index = 59, x = 0.54f, y = 0.288f, nextFields = listOf(60,61,71,72,73,74,75,76,77,78), type = FieldType.AKTION),
        Field(index = 60, x = 0.57f, y = 0.263f, nextFields = listOf(61,71,72,73,74,75,76,77,78,79), type = FieldType.AKTION),
        Field(index = 61, x = 0.57f, y = 0.228f, nextFields = listOf(71,72,73,74,75,76,77,78,79,80), type = FieldType.BABY),

// x größer ist rechts und y größer ist runter
        // Kinder Nein
        Field(index = 62, x = 0.285f, y = 0.23f, nextFields = listOf(63,64,65,66,67,68,69,70,71,72), type = FieldType.KINDER_NEIN),
        Field(index = 63, x = 0.315f, y = 0.225f, nextFields = listOf(64,65,66,67,68,69,70,71,72,73), type = FieldType.AKTION),
        Field(index = 64, x = 0.345f, y = 0.205f, nextFields = listOf(65,66,67,68,69,70,71,72,73,74), type = FieldType.BERUF),
        Field(index = 65, x = 0.376f, y = 0.225f, nextFields = listOf(66,67,68,69,70,71,72,73,74,75), type = FieldType.AKTION),
        Field(index = 66, x = 0.41f, y = 0.205f, nextFields = listOf(67,68,69,70,71,72,73,74,75,76), type = FieldType.ZAHLTAG),
        Field(index = 67, x = 0.443f, y = 0.225f, nextFields = listOf(68,69,70,71,72,73,74,75,76,77), type = FieldType.AKTION),
        Field(index = 68, x = 0.475f, y = 0.205f, nextFields = listOf(69,70,71,72,73,74,75,76,77,78), type = FieldType.ANLAGE),
        Field(index = 69, x = 0.505f, y = 0.18f, nextFields = listOf(70,71,72,73,74,75,76,77,78,79), type = FieldType.TIER),
        Field(index = 70, x = 0.54f, y = 0.152f, nextFields = listOf(71,72,73,74,75,76,77,78,79,80), type = FieldType.AKTION),
// x größer ist rechts und y größer ist runter
        //Gemeinsamer Weg zur Midlifechrisis
        Field(index = 71, x = 0.57f, y = 0.173f, nextFields = listOf(72,73,74,75,76,77,78,79,80,81), type = FieldType.ZAHLTAG),
        Field(index = 72, x = 0.605f, y = 0.152f, nextFields = listOf(73,74,75,76,77,78,79,80,81,82), type = FieldType.AKTION),
        Field(index = 73, x = 0.635f, y = 0.173f, nextFields = listOf(74,75,76,77,78,79,80,81,82,83), type = FieldType.HAUS),
        Field(index = 74, x = 0.67f, y = 0.202f, nextFields = listOf(75,76,77,78,79,80,81,82,83), type = FieldType.AKTION),
        Field(index = 75, x = 0.70f, y = 0.228f, nextFields = listOf(76,77,78,79,80,81,82,83), type = FieldType.AKTION),
        Field(index = 76, x = 0.735f, y = 0.252f, nextFields = listOf(77,78,79,80,81,82,83), type = FieldType.ZAHLTAG),
        Field(index = 77, x = 0.768f, y = 0.278f, nextFields = listOf(78,79,80,81,82,83), type = FieldType.AKTION),
        Field(index = 78, x = 0.797f, y = 0.303f, nextFields = listOf(79,80,81,82,83), type = FieldType.FREUND),
        Field(index = 79, x = 0.83f, y = 0.327f, nextFields = listOf(80,81,82,83), type = FieldType.AKTION),
        Field(index = 80, x = 0.863f, y = 0.353f, nextFields = listOf(81,82,83), type = FieldType.BERUF),
        Field(index = 81, x = 0.895f, y = 0.378f, nextFields = listOf(82,83), type = FieldType.AKTION),
        Field(index = 82, x = 0.925f, y = 0.405f, nextFields = listOf(83), type = FieldType.ANLAGE),
        Field(index = 83, x = 0.925f, y = 0.465f, nextFields = listOf(84), type = FieldType.MIDLIFECHRISIS),
// x größer ist rechts und y größer ist runter
        // Midlifechrisis rot
        Field(index = 84, x = 0.925f, y = 0.465f, nextFields = listOf(85,86,87,88,89,90,91,92,93,94), type = FieldType.MIDLIFECHRISIS_ROT),
        Field(index = 85, x = 0.895f, y = 0.465f, nextFields = listOf(86,87,88,89,90,91,92,93,94,95), type = FieldType.ANLAGE),
        Field(index = 86, x = 0.86f, y = 0.44f, nextFields = listOf(87,88,89,90,91,92,93,94,95,102), type = FieldType.AKTION),
        Field(index = 87, x = 0.83f, y = 0.44f, nextFields = listOf(88,89,90,91,92,93,94,95,102,103), type = FieldType.BERUF),
        Field(index = 88, x = 0.797f, y = 0.465f, nextFields = listOf(89,90,91,92,93,94,95,102,103,104), type = FieldType.AKTION),
        Field(index = 89, x = 0.765f, y = 0.49f, nextFields = listOf(90,91,92,93,94,95,102,103,104,105), type = FieldType.HAUS),
        Field(index = 90, x = 0.765f, y = 0.54f, nextFields = listOf(91,92,93,94,95,102,103,104,105,106), type = FieldType.AKTION),
        Field(index = 91, x = 0.797f, y = 0.565f, nextFields = listOf(92,93,94,95,102,103,104,105,106,107), type = FieldType.ZAHLTAG),
        Field(index = 92, x = 0.797f, y = 0.62f, nextFields = listOf(93,94,95,102,103,104,105,106,107,108), type = FieldType.AKTION),
        Field(index = 93, x = 0.83f, y = 0.645f, nextFields = listOf(94,95,102,103,104,105,106,107,108,109), type = FieldType.TIER),
        Field(index = 94, x = 0.83f, y = 0.695f, nextFields = listOf(95,102,103,104,105,106,107,108,109,110), type = FieldType.ANLAGE),
        Field(index = 95, x = 0.862f, y = 0.722f, nextFields = listOf(102,103,104,105,106,107,108,109,110,111), type = FieldType.AKTION),
// x größer ist rechts und y größer ist runter
        // Midlifechrisis schwarz
        Field(index = 96, x = 0.925f, y = 0.465f, nextFields = listOf(97,98,99,100,101,102,103,104,105,106), type = FieldType.MIDLIFECHRISIS_SCHWARZ),
        Field(index = 97, x = 0.895f, y = 0.495f, nextFields = listOf(98,99,100,101,102,103,104,105,106,107), type = FieldType.AKTION),
        Field(index = 98, x = 0.895f, y = 0.543f, nextFields = listOf(99,100,101,102,103,104,105,106,107,108), type = FieldType.ZAHLTAG),
        Field(index = 99, x = 0.895f, y = 0.593f, nextFields = listOf(100,101,102,103,104,105,106,107,108,109), type = FieldType.AKTION),
        Field(index = 100, x = 0.895f, y = 0.642f, nextFields = listOf(101,102,103,104,105,106,107,108,109,110), type = FieldType.FREUND),
        Field(index = 101, x = 0.895f, y = 0.694f, nextFields = listOf(102,103,104,105,106,107,108,109,110,111), type = FieldType.AKTION),
// x größer ist rechts und y größer ist runter
        // Gemeinsamer Weg zum Ruhestand
        Field(index = 102, x = 0.895f, y = 0.745f, nextFields = listOf(103,104,105,106,107,108,109,110,111,112), type = FieldType.ZAHLTAG),
        Field(index = 103, x = 0.895f, y = 0.798f, nextFields = listOf(104,105,106,107,108,109,110,111,112,113), type = FieldType.AKTION),
        Field(index = 104, x = 0.863f, y = 0.823f, nextFields = listOf(105,106,107,108,109,110,111,112,113,114), type = FieldType.ANLAGE),
        Field(index = 105, x = 0.83f, y = 0.8f, nextFields = listOf(106,107,108,109,110,111,112,113,114,115), type = FieldType.AKTION),
        Field(index = 106, x = 0.8f, y = 0.823f, nextFields = listOf(107,108,109,110,111,112,113,114,115), type = FieldType.HAUS),
        Field(index = 107, x = 0.765f, y = 0.8f, nextFields = listOf(108,109,110,111,112,113,114,115,), type = FieldType.AKTION),
        Field(index = 108, x = 0.733f, y = 0.772f, nextFields = listOf(109,110,111,112,113,114,115), type = FieldType.BERUF),
        Field(index = 109, x = 0.7f, y = 0.748f, nextFields = listOf(110,111,112,113,114,115), type = FieldType.ANLAGE),
        Field(index = 110, x = 0.67f, y = 0.77f, nextFields = listOf(111,112,113,114,115), type = FieldType.ZAHLTAG),
        Field(index = 111, x = 0.645f, y = 0.75f, nextFields = listOf(112,113,114,115), type = FieldType.AKTION),
        Field(index = 112, x = 0.605f, y = 0.772f, nextFields = listOf(113,114,115), type = FieldType.FREUND),
        Field(index = 113, x = 0.575f, y = 0.75f, nextFields = listOf(114,115), type = FieldType.AKTION),
        Field(index = 114, x = 0.575f, y = 0.698f, nextFields = listOf(115), type = FieldType.AKTION),
        Field(index = 115, x = 0.575f, y = 0.64f, nextFields = listOf(116), type = FieldType.FRUEHPENSION),
// x größer ist rechts und y größer ist runter
        // Frühpension Ja
        Field(index = 116, x = 0.575f, y = 0.645f, nextFields = listOf(117,118,119), type = FieldType.FRUEHPENSION_JA),
        Field(index = 117, x = 0.61f, y = 0.645f, nextFields = listOf(118,119), type = FieldType.AKTION),
        Field(index = 118, x = 0.64f, y = 0.615f, nextFields = listOf(119), type = FieldType.ZAHLTAG),
        Field(index = 119, x = 0.665f, y = 0.565f, nextFields = listOf(119), type = FieldType.RUHESTAND),
// Frühpension Nein
        Field(index = 120, x = 0.575f, y = 0.64f, nextFields = listOf(121,122,123,124,125,126,127,128,129,130), type = FieldType.FRUEHPENSION_NEIN),
        Field(index = 121, x = 0.575f, y = 0.585f, nextFields = listOf(122,123,124,125,126,127,128,129,130,131), type = FieldType.AKTION),
        Field(index = 122, x = 0.575f, y = 0.535f, nextFields = listOf(123,124,125,126,127,128,129,130,131,132), type = FieldType.BERUF),
        Field(index = 123, x = 0.604f, y = 0.51f, nextFields = listOf(124,125,126,127,128,129,130,131,132,133), type = FieldType.ANLAGE),
        Field(index = 124, x = 0.604f, y = 0.458f, nextFields = listOf(125,126,127,128,129,130,131,132,133,134), type = FieldType.TIER),
        Field(index = 125, x = 0.604f, y = 0.407f, nextFields = listOf(126,127,128,129,130,131,132,133,134), type = FieldType.AKTION),
        Field(index = 126, x = 0.635f, y = 0.378f, nextFields = listOf(127,128,129,130,131,132,133,134), type = FieldType.ZAHLTAG),
        Field(index = 127, x = 0.668f, y = 0.35f, nextFields = listOf(128,129,130,131,132,133,134), type = FieldType.AKTION),
        Field(index = 128, x = 0.7f, y = 0.325f, nextFields = listOf(129,130,131,132,133,134), type = FieldType.HAUS),
        Field(index = 129, x = 0.735f, y = 0.35f, nextFields = listOf(130,131,132,133,134), type = FieldType.AKTION),
        Field(index = 130, x = 0.735f, y = 0.4f, nextFields = listOf(131,132,133,134), type = FieldType.TIER),
        Field(index = 131, x = 0.703f, y = 0.428f, nextFields = listOf(132,133,134), type = FieldType.AKTION),
        Field(index = 132, x = 0.668f, y = 0.454f, nextFields = listOf(133,134), type = FieldType.ZAHLTAG),
        Field(index = 133, x = 0.668f, y = 0.504f, nextFields = listOf(134), type = FieldType.AKTION),
        Field(index = 134, x = 0.665f, y = 0.565f, nextFields = listOf(134), type = FieldType.RUHESTAND)



    )
}
