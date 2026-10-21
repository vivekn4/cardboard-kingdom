package com.vn.cardboardkingdom

/**
 * M0 district: one greybox neighbourhood in Frostgate.
 *
 * Legend (drawn as coloured blocks for now; see docs/ASSET_SWAP.md for the
 * real sprite-pack mapping):
 *  '#' building (blocked)   '.' street / sidewalk (walkable)
 *  'D' dumpster (searchable, 3x/day)   'K' soup kitchen (meal 1x/day)
 *  'S' shelter (warm up 1x/day)         'C' camp — bridge underpass (sleep)
 *  'P' park bench (flavour, restores 5 energy when examined — M0 tiny perk)
 *
 * The map is 16 wide x 12 tall. The player starts at (2, 10).
 */
object World {
    const val COLS = 16
    const val ROWS = 12

    private val MAP = arrayOf(
        "################",
        "#.....K........#",
        "#..###....###..#",
        "#..#S#....#D#..#",
        "#..###....###..#",
        "#..............#",
        "#....P....P....#",
        "#..............#",
        "#..###....###..#",
        "#..#D#....#.#..#",
        "#.....C........#",
        "################",
    )

    fun tileAt(x: Int, y: Int): Char {
        if (x !in 0 until COLS || y !in 0 until ROWS) return '#'
        return MAP[y][x]
    }

    fun isWalkable(x: Int, y: Int): Boolean {
        val t = tileAt(x, y)
        return t != '#'
    }

    /** Interactable kind at a tile, or null. */
    fun interactableAt(x: Int, y: Int): Interactable? = when (tileAt(x, y)) {
        'D' -> Interactable.DUMPSTER
        'K' -> Interactable.KITCHEN
        'S' -> Interactable.SHELTER
        'C' -> Interactable.CAMP
        'P' -> Interactable.BENCH
        else -> null
    }

    /** Breadth-first path from (sx,sy) to (tx,ty) over walkable tiles. */
    fun findPath(sx: Int, sy: Int, tx: Int, ty: Int): List<Pair<Int, Int>> {
        if (!isWalkable(tx, ty)) return emptyList()
        val prev = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>>()
        val queue = ArrayDeque<Pair<Int, Int>>()
        val start = sx to sy
        queue.add(start)
        prev[start] = start
        while (queue.isNotEmpty()) {
            val cur = queue.removeFirst()
            if (cur == tx to ty) break
            for ((dx, dy) in listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)) {
                val nx = cur.first + dx
                val ny = cur.second + dy
                val n = nx to ny
                if (isWalkable(nx, ny) && n !in prev) {
                    prev[n] = cur
                    queue.add(n)
                }
            }
        }
        if (tx to ty !in prev) return emptyList()
        val path = mutableListOf<Pair<Int, Int>>()
        var cur = tx to ty
        while (cur != start) {
            path.add(cur)
            cur = prev[cur]!!
        }
        return path.reversed()
    }

    enum class Interactable { DUMPSTER, KITCHEN, SHELTER, CAMP, BENCH }
}
