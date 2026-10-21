package com.vn.cardboardkingdom

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * JSON autosave to internal storage (no permissions, no cloud).
 * Written every game hour and on sleep; loaded on launch if present.
 */
object SaveManager {
    private const val NAME = "cardboard_kingdom_save.json"

    fun save(context: Context, s: GameState) {
        try {
            val o = JSONObject()
            o.put("day", s.day)
            o.put("hour", s.hour)
            o.put("hunger", s.hunger.toDouble())
            o.put("thirst", s.thirst.toDouble())
            o.put("health", s.health.toDouble())
            o.put("warmth", s.warmth.toDouble())
            o.put("energy", s.energy.toDouble())
            o.put("money", s.money)
            o.put("food", s.food)
            o.put("playerX", s.playerX)
            o.put("playerY", s.playerY)
            o.put("kitchenUsedDay", s.kitchenUsedDay)
            o.put("shelterUsedDay", s.shelterUsedDay)
            o.put("dumpsterCountToday", s.dumpsterCountToday)
            o.put("season", s.season)
            o.put("gameOver", s.gameOver)
            File(context.filesDir, NAME).writeText(o.toString())
        } catch (_: Exception) {
            // Save failures must never crash the game; next autosave retries.
        }
    }

    fun load(context: Context): GameState? {
        return try {
            val f = File(context.filesDir, NAME)
            if (!f.exists()) return null
            val o = JSONObject(f.readText())
            GameState(
                day = o.optInt("day", 1),
                hour = o.optInt("hour", 8),
                hunger = o.optDouble("hunger", 70.0).toFloat(),
                thirst = o.optDouble("thirst", 70.0).toFloat(),
                health = o.optDouble("health", 100.0).toFloat(),
                warmth = o.optDouble("warmth", 80.0).toFloat(),
                energy = o.optDouble("energy", 80.0).toFloat(),
                money = o.optInt("money", 0),
                food = o.optInt("food", 0),
                playerX = o.optInt("playerX", 2),
                playerY = o.optInt("playerY", 10),
                kitchenUsedDay = o.optInt("kitchenUsedDay", 0),
                shelterUsedDay = o.optInt("shelterUsedDay", 0),
                dumpsterCountToday = o.optInt("dumpsterCountToday", 0),
                season = o.optString("season", "winter"),
                gameOver = o.optBoolean("gameOver", false),
            )
        } catch (_: Exception) {
            null
        }
    }

    fun hasSave(context: Context): Boolean =
        File(context.filesDir, NAME).exists()

    fun clear(context: Context) {
        try {
            File(context.filesDir, NAME).delete()
        } catch (_: Exception) {
        }
    }
}
