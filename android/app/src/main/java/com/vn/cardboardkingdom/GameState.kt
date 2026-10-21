package com.vn.cardboardkingdom

/**
 * M0 game state + pure rules. Every number here is mirrored 1:1 in
 * tests/game_math_tests.py — if you change a constant here, change it there
 * too, and keep the harness green.
 *
 * Clock: 1 real minute = 1 game hour. tickHour() runs once per game hour.
 * All stats are 0..100 floats (money/food are ints). Everything clamps.
 */
data class GameState(
    var day: Int = 1,
    var hour: Int = 8,
    var hunger: Float = 70f,
    var thirst: Float = 70f,
    var health: Float = 100f,
    var warmth: Float = 80f,
    var energy: Float = 80f,
    var money: Int = 0,
    var food: Int = 0,
    var playerX: Int = 2,
    var playerY: Int = 10,
    var kitchenUsedDay: Int = 0,
    var shelterUsedDay: Int = 0,
    var dumpsterCountToday: Int = 0,
    var season: String = "winter",
    var gameOver: Boolean = false,
) {
    fun isNight(): Boolean = hour >= 22 || hour < 6

    /** One game hour passes. Applies drains, advances the clock, autosave hook. */
    fun tickHour() {
        if (gameOver) return
        hunger = (hunger - HUNGER_DRAIN).coerceIn(0f, 100f)
        thirst = (thirst - THIRST_DRAIN).coerceIn(0f, 100f)
        energy = (energy - ENERGY_DRAIN).coerceIn(0f, 100f)
        val night = isNight()
        val warmthDrain = if (season == "winter") {
            if (night) WARMTH_DRAIN_WINTER_NIGHT else WARMTH_DRAIN_WINTER_DAY
        } else {
            if (night) WARMTH_DRAIN_MILD_NIGHT else WARMTH_DRAIN_MILD_DAY
        }
        warmth = (warmth - warmthDrain).coerceIn(0f, 100f)

        var healthDrain = 0f
        if (hunger <= 0f) healthDrain += HEALTH_DRAIN_STARVING
        if (thirst <= 0f) healthDrain += HEALTH_DRAIN_PARCHED
        if (warmth <= 0f) healthDrain += HEALTH_DRAIN_FROZEN
        if (hunger < 20f) healthDrain += HEALTH_DRAIN_LOW
        if (thirst < 20f) healthDrain += HEALTH_DRAIN_LOW
        health = (health - healthDrain).coerceIn(0f, 100f)
        if (health <= 0f) gameOver = true

        hour += 1
        if (hour >= 24) {
            hour = 0
            day += 1
            dumpsterCountToday = 0
        }
    }

    /** Walking one tile costs a little energy. Returns false if exhausted. */
    fun stepCost(): Boolean {
        if (energy <= 0f) return false
        energy = (energy - MOVE_COST).coerceIn(0f, 100f)
        return true
    }

    /** Sleep. Camp = full restore; anywhere else = rough sleep, half restore. */
    fun sleep(atCamp: Boolean) {
        energy = (energy + if (atCamp) SLEEP_ENERGY_CAMP else SLEEP_ENERGY_ROUGH)
            .coerceIn(0f, 100f)
        health = (health + if (atCamp) SLEEP_HEALTH_CAMP else SLEEP_HEALTH_ROUGH)
            .coerceIn(0f, 100f)
        warmth = (if (atCamp) SLEEP_WARMTH_CAMP else SLEEP_WARMTH_ROUGH)
            .coerceAtLeast(warmth).coerceIn(0f, 100f)
        hunger = (hunger - SLEEP_HUNGER_COST).coerceIn(0f, 100f)
        thirst = (thirst - SLEEP_THIRST_COST).coerceIn(0f, 100f)
        day += 1
        hour = 8
        dumpsterCountToday = 0
    }

    /** Eat one carried food item. Returns false if none. */
    fun eat(): Boolean {
        if (food <= 0) return false
        food -= 1
        hunger = (hunger + EAT_HUNGER).coerceIn(0f, 100f)
        thirst = (thirst + EAT_THIRST).coerceIn(0f, 100f)
        return true
    }

    /** Search a dumpster. Max [DUMPSTER_MAX_PER_DAY] finds per day. */
    fun searchDumpster(): Boolean {
        if (dumpsterCountToday >= DUMPSTER_MAX_PER_DAY) return false
        dumpsterCountToday += 1
        food += 1
        energy = (energy - DUMPSTER_ENERGY_COST).coerceIn(0f, 100f)
        return true
    }

    /** Free soup-kitchen meal, once per day. */
    fun soupKitchenMeal(): Boolean {
        if (kitchenUsedDay == day) return false
        kitchenUsedDay = day
        hunger = (hunger + KITCHEN_HUNGER).coerceIn(0f, 100f)
        thirst = (thirst + KITCHEN_THIRST).coerceIn(0f, 100f)
        return true
    }

    /** Warm up at the shelter, once per day. */
    fun shelterWarmUp(): Boolean {
        if (shelterUsedDay == day) return false
        shelterUsedDay = day
        warmth = (warmth + SHELTER_WARMTH).coerceIn(0f, 100f)
        return true
    }

    companion object {
        const val HUNGER_DRAIN = 2f
        const val THIRST_DRAIN = 2.5f
        const val ENERGY_DRAIN = 1.5f
        const val WARMTH_DRAIN_WINTER_NIGHT = 5f
        const val WARMTH_DRAIN_WINTER_DAY = 1.5f
        const val WARMTH_DRAIN_MILD_NIGHT = 2f
        const val WARMTH_DRAIN_MILD_DAY = 0.5f
        const val HEALTH_DRAIN_STARVING = 4f
        const val HEALTH_DRAIN_PARCHED = 5f
        const val HEALTH_DRAIN_FROZEN = 6f
        const val HEALTH_DRAIN_LOW = 1f
        const val MOVE_COST = 0.3f
        const val SLEEP_ENERGY_CAMP = 80f
        const val SLEEP_ENERGY_ROUGH = 40f
        const val SLEEP_HEALTH_CAMP = 15f
        const val SLEEP_HEALTH_ROUGH = 5f
        const val SLEEP_WARMTH_CAMP = 80f
        const val SLEEP_WARMTH_ROUGH = 50f
        const val SLEEP_HUNGER_COST = 15f
        const val SLEEP_THIRST_COST = 15f
        const val EAT_HUNGER = 30f
        const val EAT_THIRST = 2f
        const val DUMPSTER_MAX_PER_DAY = 3
        const val DUMPSTER_ENERGY_COST = 2f
        const val KITCHEN_HUNGER = 50f
        const val KITCHEN_THIRST = 20f
        const val SHELTER_WARMTH = 40f
    }
}
