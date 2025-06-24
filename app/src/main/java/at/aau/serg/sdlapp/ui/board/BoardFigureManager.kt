package at.aau.serg.sdlapp.ui.board

import android.content.Context
import android.view.Gravity
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.model.player.CarColor
import at.aau.serg.sdlapp.model.player.PlayerManager
import at.aau.serg.sdlapp.network.StompConnectionManager
import com.otaliastudios.zoom.ZoomLayout

/**
 * Verwaltet die Spielfiguren auf dem Spielbrett
 */
class BoardFigureManager(
    private val context: Context,
    private val playerManager: PlayerManager,
    private val boardContainer: FrameLayout,
    private val boardImage: ImageView,
    private val zoomLayout: ZoomLayout
) {
    // Map für alle Spielerfiguren: playerId -> ImageView
    private val playerFigures = mutableMapOf<String, ImageView>()

    // Map für alle Spieler-Badges: playerId -> TextView
    private val playerBadges = mutableMapOf<String, TextView>()

    // Liste der aktuellen Highlight-Marker für mögliche Felder
    private val nextMoveMarkers = mutableListOf<ImageView>()

    /**
     * Bewegt eine Spielfigur zu einer bestimmten Position auf dem Brett
     */
    fun moveFigureToPosition(xPercent: Float, yPercent: Float, playerId: String) {
        println("⭐⭐⭐ MOVE FIGURE: Beginne moveFigureToPosition für Spieler $playerId zu Position $xPercent, $yPercent")

        // Überprüfung auf ungültige Eingabeparameter
        if (xPercent < 0 || xPercent > 1 || yPercent < 0 || yPercent > 1) {
            println("⚠️ WARNING: Ungültige Koordinaten außerhalb des gültigen Bereichs: x=$xPercent, y=$yPercent")
            // Koordinaten begrenzen
            val safeX = xPercent.coerceIn(0f, 1f)
            val safeY = yPercent.coerceIn(0f, 1f)
            println("🔄 Koordinaten korrigiert zu: x=$safeX, y=$safeY")
        }




        if (boardImage.width <= 0 || boardImage.height <= 0) {
            println("❌ KRITISCHER FEHLER: boardImage hat ungültige Dimensionen: ${boardImage.width}x${boardImage.height}")

            // Versuche, die Bewegung zu verzögern, falls das Board noch nicht gemessen wurde
            boardImage.post {
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    moveFigureToPosition(xPercent, yPercent, playerId)
                }, 500)
            }
            return
        }

        try {
            boardImage.post {
                try {
                    // Berechne die Position relativ zum Spielbrett
                    val x = xPercent * boardImage.width
                    val y = yPercent * boardImage.height

                    boardImage.post {
                        val x = xPercent * boardImage.width
                        val y = yPercent * boardImage.height

                        // Hole die entsprechende Spielfigur aus der Map
                        val playerFigure = getOrCreatePlayerFigure(playerId)
                        val playerBadge = playerBadges[playerId]

                        println("🎮 Spielfigur für ID $playerId: ${if (playerFigure != null) "gefunden" else "NICHT GEFUNDEN"}")
                        println("🏷️ Badge für ID $playerId: ${if (playerBadge != null) "gefunden" else "NICHT GEFUNDEN"}")

                        val targetX = x - playerFigure.width / 2f
                        val targetY = y - playerFigure.height / 2f
                        val badgeX = targetX + playerFigure.width - 20
                        val badgeY = targetY - 15

                        // Figur animieren
                        playerFigure.animate()
                            .x(targetX).y(targetY).setDuration(800)
                            .setInterpolator(OvershootInterpolator(1.2f))
                            .withStartAction {
                                playerFigure.animate().scaleX(1.2f).scaleY(1.2f).duration = 200
                            }
                            .withEndAction {
                                playerFigure.animate().scaleX(1.0f).scaleY(1.0f).duration = 200
                                playerFigure.x = targetX
                                playerFigure.y = targetY
                            }.start()

                        // Badge animieren
                        playerBadge?.animate()
                            ?.x(badgeX)?.y(badgeY)?.duration = 800
                        playerBadge?.x = badgeX
                        playerBadge?.y = badgeY

                        // Debug log für Ziel-Positionen
                        println("🎯 Ziel-Positionen - Figur: ($targetX, $targetY), Badge: ($badgeX, $badgeY)")

                        // Bewege die Figur mit verbesserter Animation
                        playerFigure.animate()
                            .x(targetX)
                            .y(targetY)
                            .setDuration(800)  // 800ms Animation
                            .setInterpolator(OvershootInterpolator(1.2f)) // Überschwingender Effekt
                            .withStartAction {
                                // Vor der Animation: kleine Vergrößerung
                                playerFigure.animate()
                                    .scaleX(1.2f)
                                    .scaleY(1.2f)
                                    .setDuration(200)
                                    .start()
                            }
                            .withEndAction {
                                // Nach der Animation: Größe normalisieren
                                playerFigure.animate()
                                    .scaleX(if (playerManager.isLocalPlayer(playerId)) 1.1f else 1.0f)
                                    .scaleY(if (playerManager.isLocalPlayer(playerId)) 1.1f else 1.0f)
                                    .setDuration(200)
                                    .start()

                                // Setze absolute Position nach Animation
                                playerFigure.x = targetX
                                playerFigure.y = targetY

                                // Debug-Log nach Animation
                                println("✅ Figur-Animation abgeschlossen, finale Position: (${playerFigure.x}, ${playerFigure.y})")
                            }
                            .start()

                        // Bewege auch das Badge mit Animation
                        playerBadge?.animate()
                            ?.x(badgeX)
                            ?.y(badgeY)
                            ?.setDuration(800)
                            ?.setInterpolator(OvershootInterpolator(1.2f))
                            ?.withEndAction {
                                // Setze absolute Position nach Animation
                                playerBadge.x = badgeX
                                playerBadge.y = badgeY

                                // Debug-Log nach Badge-Animation
                                println("✅ Badge-Animation abgeschlossen, finale Position: (${playerBadge.x}, ${playerBadge.y})")
                            }
                            ?.start()

                        // Stellen Sie sicher, dass die Figur sichtbar ist
                        playerFigure.visibility = android.view.View.VISIBLE
                        playerBadge?.visibility = android.view.View.VISIBLE
                    }

                } catch (e: Exception) {
                    println("❌ Fehler während der Figurenbewegung: ${e.message}")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            println("❌ Fehler vor der Figurenbewegung: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Erstellt eine Spielfigur mit ID-Badge zur besseren Unterscheidung
     */
    fun getOrCreatePlayerFigure(playerId: String): ImageView {
        // Prüfen, ob die Figur bereits existiert
        if (!playerFigures.containsKey(playerId)) {
            val player = playerManager.getPlayer(playerId)
                ?: playerManager.addPlayer(playerId, "Spieler $playerId")

            val newFigure = ImageView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    context.resources.getDimensionPixelSize(R.dimen.player_figure_size),
                    context.resources.getDimensionPixelSize(R.dimen.player_figure_size)
                )

                // Setze das richtige Auto-Bild basierend auf der Spieler-ID
                val player = playerManager.getPlayer(playerId) ?: playerManager.addPlayer(
                    playerId,
                    "Spieler $playerId"
                )

                setImageResource(player.getCarImageResource())
                translationZ = 10f
                alpha = if (playerManager.isLocalPlayer(playerId)) 1.0f else 0.9f
                scaleX = if (playerManager.isLocalPlayer(playerId)) 1.1f else 1.0f
                scaleY = if (playerManager.isLocalPlayer(playerId)) 1.1f else 1.0f

                setOnClickListener {
                    val playerInfo = playerManager.getPlayer(playerId)
                    val message = "Spieler ${playerInfo?.id} (${playerInfo?.color})"
                    android.widget.Toast.makeText(
                        context,
                        message,
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }

            val badge = TextView(context).apply {
                text = playerId
                setTextColor(android.graphics.Color.WHITE)

                val badgeBackground = playerManager.getPlayer(playerId)?.color?.let { color ->
                    when (color) {
                        CarColor.BLUE -> R.drawable.badge_blue
                        CarColor.GREEN -> R.drawable.badge_green
                        CarColor.RED -> R.drawable.badge_red
                        CarColor.YELLOW -> R.drawable.badge_yellow
                    }
                    textSize = 12f
                    gravity = Gravity.CENTER
                    setPadding(8, 4, 8, 4)
                    translationZ = 15f
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                }
            }
            boardContainer.addView(newFigure)
            boardContainer.addView(badge)

            playerFigures[playerId] = newFigure
            playerBadges[playerId] = badge
        }

        return playerFigures[playerId]
            ?: throw IllegalStateException("No player figure found for playerId=$playerId")
    }

    /**
     * Fügt einen klickbaren Marker für ein mögliches nächstes Feld hinzu
     */
    fun addNextMoveMarker(
        xPercent: Float,
        yPercent: Float,
        fieldIndex: Int,
        stompClient: StompConnectionManager,
        playerName: String
    ) {
        boardImage.post {
            val marker = ImageView(context)
            marker.setImageResource(R.drawable.move_indicator)

            val x = xPercent * boardImage.width
            val y = yPercent * boardImage.height

            // Setze Größe und Position des Markers
            val size =
                context.resources.getDimensionPixelSize(R.dimen.marker_size) // Definiere eine angemessene Größe
            val params = FrameLayout.LayoutParams(size, size)
            marker.layoutParams = params
            marker.x = x - size / 2f
            marker.y = y - size / 2f

            boardContainer.addView(marker)
            nextMoveMarkers.add(marker)

            marker.setOnClickListener {
                stompClient.sendMove(playerName, "move:$fieldIndex")
            }
        }
    }

    /**
     * Entfernt alle aktuellen Highlight-Marker vom Brett
     */
    fun clearAllMarkers() {
        nextMoveMarkers.forEach { boardContainer.removeView(it) }
        nextMoveMarkers.clear()
    }
    /**
     * Spielt eine Animation ab, wenn ein neuer Spieler beigetreten ist
     */
    /**
     * Spielt eine Animation für eine neue Spielfigur
     */
    fun playNewPlayerAnimation(playerId: String) {
        val playerFigure = playerFigures[playerId] ?: return

        // Erstelle eine Animation, die die Figur blinken lässt
        val blinkAnimation = AlphaAnimation(0.3f, 1.0f)
        blinkAnimation.duration = 500 // 0.5 Sekunden pro Blinken
        blinkAnimation.repeatMode = Animation.REVERSE
        blinkAnimation.repeatCount = 5 // 5x blinken (insgesamt 5 Sekunden)

        // Zusätzlich eine Größenänderung für mehr Aufmerksamkeit
        playerFigure.scaleX = 0.5f
        playerFigure.scaleY = 0.5f
        playerFigure.animate()
            .scaleX(1.3f)
            .scaleY(1.3f)
            .setDuration(500)
            .setInterpolator(OvershootInterpolator())
            .withEndAction {
                playerFigure.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(300)
                    .start()

                // Starte das Blinken nach der Größenänderung
                playerFigure.startAnimation(blinkAnimation)
            }
            .start()
    }

    /**
     * Entfernt die Figur eines Spielers vom Brett
     */
    fun removePlayerFigure(playerId: String) {
        val figure = playerFigures[playerId]
        val badge = playerBadges[playerId]

        // Entferne die Views aus dem Layout
        if (figure != null) {
            boardContainer.removeView(figure)
            playerFigures.remove(playerId)
        }
        playerBadges[playerId]?.let {
            boardContainer.removeView(it)
            playerBadges.remove(playerId)
        }
    }

    /**
     * Aktualisiert das Aussehen einer Spielfigur nach einer Farbänderung
     */
    fun updateFigureAppearance(playerId: String) {
        // Hole die existierende Figur und das Badge
        val playerFigure = playerFigures[playerId] ?: return
        val playerBadge = playerBadges[playerId]

        // Hole den Spieler aus dem PlayerManager
        val player = playerManager.getPlayer(playerId) ?: return

        // Aktualisiere das Bild der Figur basierend auf der neuen Farbe
        playerFigure.setImageResource(player.getCarImageResource())

        // Aktualisiere das Hintergrundbild des Badges basierend auf der neuen Farbe
        playerBadge?.setBackgroundResource(
            when (player.color) {
                CarColor.BLUE -> R.drawable.badge_blue
                CarColor.GREEN -> R.drawable.badge_green
                CarColor.RED -> R.drawable.badge_red
                CarColor.YELLOW -> R.drawable.badge_yellow
            }
        )

        // Spiele eine kurze Animation zur Verdeutlichung der Änderung
        playerFigure.animate()
            .rotationBy(360f)
            .scaleX(1.3f)
            .scaleY(1.3f)
            .setDuration(500)
            .withEndAction {
                playerFigure.animate()
                    .scaleX(if (playerManager.isLocalPlayer(playerId)) 1.1f else 1.0f)
                    .scaleY(if (playerManager.isLocalPlayer(playerId)) 1.1f else 1.0f)
                    .setDuration(300)
                    .start()
            }
            .start()

        println("🎨 Spielfigur für $playerId wurde mit neuer Farbe ${player.color} aktualisiert")
    }

}