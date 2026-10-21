package com.vn.cardboardkingdom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.hypot
import kotlin.math.min

/**
 * M0 engine: a Canvas/SurfaceView game loop. No engine dependency.
 *
 * Screens: TITLE / PLAYING / PAUSED / HELP / GAMEOVER, plus a tiny modal
 * dialog (sleep-rough confirm). Input: virtual joystick (bottom-left),
 * tap-to-move (tap any tile), ACT button (bottom-right), EAT button
 * (when carrying food), pause button (top-right).
 *
 * GREYBOX ART: tiles and the player are coloured blocks. The asset-swap
 * plan (docs/ASSET_SWAP.md) maps each block to its real sprite-pack
 * replacement; the player block has a beanie + backpack drawn on it to
 * mark exactly where the Ruzty base sprite + custom layers will slot in.
 */
class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private enum class Screen { TITLE, PLAYING, PAUSED, HELP, GAMEOVER }

    private var screen = Screen.TITLE
    private var state = GameState()
    private var hasSave = SaveManager.hasSave(context)
    private var message = ""
    private var messageUntil = 0L
    private var pendingRoughSleep = false

    // Smooth player position in tile coordinates (floats).
    private var px = state.playerX.toFloat()
    private var py = state.playerY.toFloat()
    private var tapPath: List<Pair<Int, Int>> = emptyList()
    private var pathIndex = 0

    // Joystick state.
    private var joyActive = false
    private var joyId = -1
    private var joyBaseX = 0f
    private var joyBaseY = 0f
    private var joyDx = 0f
    private var joyDy = 0f
    private var downX = 0f
    private var downY = 0f
    private var downTime = 0L

    // Clock: 1 real minute = 1 game hour.
    private var lastTickMs = System.currentTimeMillis()
    private var lastFrameMs = System.currentTimeMillis()

    private var thread: Thread? = null
    @Volatile private var running = false

    private val paint = Paint().apply { isAntiAlias = true }
    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textSize = 34f
    }
    private val smallPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textSize = 26f
    }

    // Layout, recomputed per frame.
    private var tile = 0f
    private var mapOx = 0f
    private var mapOy = 0f
    private val actBtn = RectF()
    private val eatBtn = RectF()
    private val pauseBtn = RectF()

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    // ------------------------------------------------------------ lifecycle
    override fun surfaceCreated(holder: SurfaceHolder) {
        running = true
        thread = Thread {
            while (running) {
                val now = System.currentTimeMillis()
                val dt = ((now - lastFrameMs) / 1000f).coerceIn(0f, 0.1f)
                lastFrameMs = now
                if (screen == Screen.PLAYING) {
                    update(dt, now)
                }
                val c: Canvas? = holder.lockCanvas()
                if (c != null) {
                    try {
                        drawFrame(c)
                    } finally {
                        holder.unlockCanvasAndPost(c)
                    }
                }
                try {
                    Thread.sleep(33)
                } catch (_: InterruptedException) {
                }
            }
        }.also { it.start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        try {
            thread?.join(500)
        } catch (_: InterruptedException) {
        }
        SaveManager.save(context, state)
    }

    // ------------------------------------------------------------ update
    private fun update(dt: Float, now: Long) {
        // Game clock.
        if (now - lastTickMs >= 60_000L) {
            lastTickMs = now
            state.tickHour()
            SaveManager.save(context, state)
            if (state.gameOver) screen = Screen.GAMEOVER
        }
        if (state.gameOver) {
            screen = Screen.GAMEOVER
            return
        }

        // Movement: joystick wins, otherwise follow tap path.
        var mx = 0f
        var my = 0f
        if (joyActive) {
            mx = joyDx
            my = joyDy
        } else if (pathIndex < tapPath.size) {
            val (tx, ty) = tapPath[pathIndex]
            val dx = tx + 0.5f - px
            val dy = ty + 0.5f - py
            val d = hypot(dx, dy)
            if (d < 0.15f) {
                pathIndex++
            } else {
                mx = dx / d
                my = dy / d
            }
        }
        if (mx != 0f || my != 0f) {
            val speed = 5f // tiles per second
            val nx = px + mx * speed * dt
            val ny = py + my * speed * dt
            // Collision: only enter walkable tiles.
            if (World.isWalkable(nx.toInt(), py.toInt())) px = nx
            if (World.isWalkable(px.toInt(), ny.toInt())) py = ny
            // Charge energy when crossing into a new tile.
            val tx = px.toInt()
            val ty = py.toInt()
            if (tx != state.playerX || ty != state.playerY) {
                if (state.stepCost()) {
                    state.playerX = tx
                    state.playerY = ty
                } else {
                    // Exhausted: snap back — no free movement at 0 energy.
                    px = state.playerX + 0.5f
                    py = state.playerY + 0.5f
                    tapPath = emptyList()
                }
            }
        }
        if (messageUntil > 0 && now > messageUntil) message = ""
    }

    private fun show(msg: String) {
        message = msg
        messageUntil = System.currentTimeMillis() + 4000
    }

    // ------------------------------------------------------------ actions
    private fun nearbyInteractable(): World.Interactable? {
        val here = World.interactableAt(state.playerX, state.playerY)
        if (here != null) return here
        for ((dx, dy) in listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)) {
            val it = World.interactableAt(state.playerX + dx, state.playerY + dy)
            if (it != null) return it
        }
        return null
    }

    private fun doAction() {
        when (nearbyInteractable()) {
            World.Interactable.DUMPSTER -> {
                if (state.searchDumpster()) {
                    show("You found food! (${state.dumpsterCountToday}/3 dumpsters today)")
                    SaveManager.save(context, state)
                } else {
                    show("This dumpster is picked clean for today.")
                }
            }
            World.Interactable.KITCHEN -> {
                if (state.soupKitchenMeal()) {
                    show("Warm meal at Mama June's kitchen. Hunger +50, Thirst +20.")
                    SaveManager.save(context, state)
                } else {
                    show("You already ate here today. Come back tomorrow.")
                }
            }
            World.Interactable.SHELTER -> {
                if (state.shelterWarmUp()) {
                    show("You warm up inside the shelter. Warmth +40.")
                    SaveManager.save(context, state)
                } else {
                    show("You already warmed up here today.")
                }
            }
            World.Interactable.CAMP -> {
                state.sleep(atCamp = true)
                SaveManager.save(context, state)
                show("You sleep at camp. Morning of day ${state.day}.")
            }
            World.Interactable.BENCH -> {
                state.energy = (state.energy + 5f).coerceIn(0f, 100f)
                show("You rest on the bench for a while. Energy +5.")
            }
            null -> {
                pendingRoughSleep = true
            }
        }
    }

    // ------------------------------------------------------------ input
    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x = e.x
        val y = e.y
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = x
                downY = y
                downTime = System.currentTimeMillis()
                when (screen) {
                    Screen.TITLE -> handleTitleTap(x, y)
                    Screen.PLAYING -> {
                        if (pendingRoughSleep) {
                            // Modal dialog owns all touches until answered.
                            handleDialogTap(x, y)
                        } else when {
                            pauseBtn.contains(x, y) -> screen = Screen.PAUSED
                            actBtn.contains(x, y) -> doAction()
                            eatBtn.contains(x, y) && state.food > 0 -> {
                                if (state.eat()) show("You eat. Hunger +30.")
                            }
                            x < width * 0.45f && y > height * 0.5f -> {
                                joyActive = true
                                joyId = e.getPointerId(0)
                                joyBaseX = x
                                joyBaseY = y
                                joyDx = 0f
                                joyDy = 0f
                            }
                            else -> {
                                // Tap-to-move: convert to a tile, pathfind.
                                val tx = ((x - mapOx) / tile).toInt()
                                val ty = ((y - mapOy) / tile).toInt()
                                val path = World.findPath(
                                    state.playerX, state.playerY, tx, ty,
                                )
                                if (path.isNotEmpty()) {
                                    tapPath = path
                                    pathIndex = 0
                                }
                            }
                        }
                    }
                    Screen.PAUSED -> handlePausedTap(x, y)
                    Screen.HELP -> screen = Screen.PAUSED
                    Screen.GAMEOVER -> handleGameOverTap(x, y)
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (joyActive) {
                    val dx = x - joyBaseX
                    val dy = y - joyBaseY
                    val d = hypot(dx, dy)
                    val r = 90f
                    if (d > r) {
                        joyDx = dx / d
                        joyDy = dy / d
                    } else if (d > 12f) {
                        joyDx = dx / r
                        joyDy = dy / r
                    } else {
                        joyDx = 0f
                        joyDy = 0f
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (joyActive) {
                    joyActive = false
                    joyDx = 0f
                    joyDy = 0f
                }
                return true
            }
        }
        return super.onTouchEvent(e)
    }

    private fun buttonRect(cx: Float, cy: Float, w: Float, h: Float): RectF =
        RectF(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2)

    private fun handleTitleTap(x: Float, y: Float) {
        val w = width.toFloat()
        val h = height.toFloat()
        val newBtn = buttonRect(w / 2, h * 0.62f, w * 0.7f, 110f)
        val contBtn = buttonRect(w / 2, h * 0.74f, w * 0.7f, 110f)
        when {
            newBtn.contains(x, y) -> {
                state = GameState()
                px = state.playerX.toFloat()
                py = state.playerY.toFloat()
                tapPath = emptyList()
                SaveManager.clear(context)
                hasSave = false
                lastTickMs = System.currentTimeMillis()
                screen = Screen.PLAYING
                show("Day 1, 8 AM. Find food, stay warm, get to camp by night.")
            }
            contBtn.contains(x, y) && hasSave -> {
                SaveManager.load(context)?.let {
                    state = it
                    px = state.playerX.toFloat()
                    py = state.playerY.toFloat()
                }
                lastTickMs = System.currentTimeMillis()
                screen = Screen.PLAYING
                show("Welcome back. Day ${state.day}, ${state.hour}:00.")
            }
        }
    }

    private fun handlePausedTap(x: Float, y: Float) {
        val w = width.toFloat()
        val h = height.toFloat()
        val resume = buttonRect(w / 2, h * 0.42f, w * 0.7f, 110f)
        val help = buttonRect(w / 2, h * 0.54f, w * 0.7f, 110f)
        val restart = buttonRect(w / 2, h * 0.66f, w * 0.7f, 110f)
        when {
            resume.contains(x, y) -> {
                screen = Screen.PLAYING
                lastTickMs = System.currentTimeMillis()
            }
            help.contains(x, y) -> screen = Screen.HELP
            restart.contains(x, y) -> {
                state = GameState()
                px = state.playerX.toFloat()
                py = state.playerY.toFloat()
                SaveManager.clear(context)
                hasSave = false
                screen = Screen.PLAYING
            }
        }
    }

    private fun handleGameOverTap(x: Float, y: Float) {
        val w = width.toFloat()
        val h = height.toFloat()
        val again = buttonRect(w / 2, h * 0.72f, w * 0.7f, 110f)
        if (again.contains(x, y)) {
            state = GameState()
            px = state.playerX.toFloat()
            py = state.playerY.toFloat()
            SaveManager.clear(context)
            hasSave = false
            lastTickMs = System.currentTimeMillis()
            screen = Screen.PLAYING
        }
    }

    private fun handleDialogTap(x: Float, y: Float) {
        val w = width.toFloat()
        val h = height.toFloat()
        val yes = buttonRect(w / 2 - w * 0.22f, h * 0.56f, w * 0.36f, 100f)
        val no = buttonRect(w / 2 + w * 0.22f, h * 0.56f, w * 0.36f, 100f)
        when {
            yes.contains(x, y) -> {
                state.sleep(atCamp = false)
                SaveManager.save(context, state)
                pendingRoughSleep = false
                show("Rough night. Morning of day ${state.day}.")
            }
            no.contains(x, y) -> pendingRoughSleep = false
        }
    }

    // ------------------------------------------------------------ drawing
    private fun drawFrame(c: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        c.drawColor(Color.rgb(11, 18, 32))
        when (screen) {
            Screen.TITLE -> drawTitle(c, w, h)
            Screen.PLAYING -> {
                drawWorld(c, w, h)
                drawHud(c, w, h)
                if (pendingRoughSleep) drawDialog(c, w, h)
            }
            Screen.PAUSED -> {
                drawWorld(c, w, h)
                drawHud(c, w, h)
                drawPaused(c, w, h)
            }
            Screen.HELP -> drawHelp(c, w, h)
            Screen.GAMEOVER -> drawGameOver(c, w, h)
        }
    }

    private fun drawCentered(c: Canvas, text: String, y: Float, p: Paint) {
        c.drawText(text, (width - p.measureText(text)) / 2, y, p)
    }

    private fun drawButton(c: Canvas, r: RectF, label: String, enabled: Boolean = true) {
        paint.color = if (enabled) Color.rgb(245, 158, 11) else Color.rgb(90, 90, 90)
        c.drawRoundRect(r, 24f, 24f, paint)
        paint.color = Color.BLACK
        paint.textSize = 36f
        paint.textAlign = Paint.Align.CENTER
        c.drawText(label, r.centerX(), r.centerY() + 13f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawTitle(c: Canvas, w: Float, h: Float) {
        textPaint.textSize = 84f
        textPaint.color = Color.rgb(245, 158, 11)
        drawCentered(c, "CARDBOARD", h * 0.26f, textPaint)
        drawCentered(c, "KINGDOM", h * 0.36f, textPaint)
        textPaint.textSize = 36f
        textPaint.color = Color.WHITE
        drawCentered(c, "A homeless survival story", h * 0.44f, textPaint)
        smallPaint.color = Color.rgb(180, 180, 180)
        drawCentered(c, "Frostgate — winter is coming", h * 0.50f, smallPaint)
        drawButton(c, buttonRect(w / 2, h * 0.62f, w * 0.7f, 110f), "NEW GAME")
        drawButton(
            c, buttonRect(w / 2, h * 0.74f, w * 0.7f, 110f),
            "CONTINUE", enabled = hasSave,
        )
        smallPaint.color = Color.rgb(140, 140, 140)
        drawCentered(c, "By VN • M0 greybox • Free, offline, no ads", h * 0.90f, smallPaint)
    }

    private fun drawWorld(c: Canvas, w: Float, h: Float) {
        val hudH = 260f
        val availH = h - hudH - 260f
        tile = min(w / World.COLS, availH / World.ROWS)
        mapOx = (w - tile * World.COLS) / 2
        mapOy = hudH
        for (y in 0 until World.ROWS) {
            for (x in 0 until World.COLS) {
                val t = World.tileAt(x, y)
                val left = mapOx + x * tile
                val top = mapOy + y * tile
                paint.color = when (t) {
                    '#' -> Color.rgb(74, 53, 39)
                    'D' -> Color.rgb(46, 125, 50)
                    'K' -> Color.rgb(217, 119, 6)
                    'S' -> Color.rgb(2, 132, 199)
                    'C' -> Color.rgb(139, 107, 74)
                    'P' -> Color.rgb(107, 114, 128)
                    else -> Color.rgb(42, 42, 51)
                }
                c.drawRect(left, top, left + tile, top + tile, paint)
                // Interactable glyphs.
                smallPaint.color = Color.WHITE
                smallPaint.textSize = tile * 0.4f
                val glyph = when (t) {
                    'D' -> "D"
                    'K' -> "K"
                    'S' -> "S"
                    'C' -> "C"
                    'P' -> "P"
                    else -> null
                }
                if (glyph != null) {
                    c.drawText(
                        glyph,
                        left + tile / 2 - smallPaint.measureText(glyph) / 2,
                        top + tile * 0.65f,
                        smallPaint,
                    )
                }
            }
        }
        // Player block figure (greybox; Ruzty sprite + layers slot in here).
        val pLeft = mapOx + px * tile
        val pTop = mapOy + py * tile
        val pw = tile * 0.7f
        val ph = tile * 0.9f
        val cx = pLeft + tile * 0.15f
        val cy = pTop + tile * 0.05f
        paint.color = Color.rgb(59, 59, 59) // coat
        c.drawRect(cx, cy + ph * 0.35f, cx + pw, cy + ph, paint)
        paint.color = Color.rgb(232, 195, 158) // head
        c.drawRect(cx + pw * 0.2f, cy, cx + pw * 0.8f, cy + ph * 0.35f, paint)
        paint.color = Color.rgb(192, 57, 43) // beanie
        c.drawRect(cx + pw * 0.2f, cy, cx + pw * 0.8f, cy + ph * 0.12f, paint)
        paint.color = Color.rgb(139, 107, 74) // backpack
        c.drawRect(cx - pw * 0.12f, cy + ph * 0.4f, cx + pw * 0.12f, cy + ph * 0.85f, paint)

        // Night tint.
        val tint = nightAlpha()
        if (tint > 0) {
            paint.color = Color.argb(tint, 5, 5, 25)
            c.drawRect(mapOx, mapOy, mapOx + tile * World.COLS, mapOy + tile * World.ROWS, paint)
        }
    }

    private fun nightAlpha(): Int {
        val h = state.hour
        return when {
            h in 7..17 -> 0
            h in 18..21 -> 40 + (h - 18) * 30
            h >= 22 || h <= 5 -> 150
            h == 6 -> 90
            else -> 0
        }
    }

    private fun drawBar(c: Canvas, x: Float, y: Float, w: Float, label: String, v: Float, color: Int) {
        smallPaint.color = Color.WHITE
        smallPaint.textSize = 26f
        c.drawText(label, x, y + 24f, smallPaint)
        paint.color = Color.rgb(60, 60, 60)
        c.drawRoundRect(RectF(x + 150f, y, x + w, y + 30f), 15f, 15f, paint)
        paint.color = color
        val fill = (w - 150f) * (v / 100f).coerceIn(0f, 1f)
        c.drawRoundRect(RectF(x + 150f, y, x + 150f + fill, y + 30f), 15f, 15f, paint)
    }

    private fun drawHud(c: Canvas, w: Float, h: Float) {
        textPaint.textSize = 34f
        textPaint.color = Color.WHITE
        val clock = "Day ${state.day}  ${state.hour}:00  •  $${state.money}  •  Food: ${state.food}"
        c.drawText(clock, 24f, 44f, textPaint)
        drawBar(c, 24f, 60f, w - 48f, "Hunger", state.hunger, Color.rgb(249, 115, 22))
        drawBar(c, 24f, 100f, w - 48f, "Thirst", state.thirst, Color.rgb(56, 189, 248))
        drawBar(c, 24f, 140f, w - 48f, "Health", state.health, Color.rgb(239, 68, 68))
        drawBar(c, 24f, 180f, w - 48f, "Warmth", state.warmth, Color.rgb(34, 211, 238))
        drawBar(c, 24f, 220f, w - 48f, "Energy", state.energy, Color.rgb(250, 204, 21))

        // Pause button.
        pauseBtn.set(w - 120f, 8f, w - 24f, 104f)
        paint.color = Color.rgb(60, 60, 60)
        c.drawRoundRect(pauseBtn, 20f, 20f, paint)
        smallPaint.color = Color.WHITE
        smallPaint.textSize = 44f
        c.drawText("II", w - 84f, 66f, smallPaint)

        // ACT button (bottom-right).
        actBtn.set(w - 230f, h - 260f, w - 30f, h - 60f)
        paint.color = Color.rgb(245, 158, 11)
        c.drawRoundRect(actBtn, 100f, 100f, paint)
        paint.color = Color.BLACK
        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER
        c.drawText("ACT", actBtn.centerX(), actBtn.centerY() + 14f, paint)
        paint.textAlign = Paint.Align.LEFT

        // EAT button (only when carrying food).
        if (state.food > 0) {
            eatBtn.set(w - 450f, h - 260f, w - 260f, h - 60f)
            paint.color = Color.rgb(34, 197, 94)
            c.drawRoundRect(eatBtn, 100f, 100f, paint)
            paint.color = Color.BLACK
            paint.textSize = 36f
            paint.textAlign = Paint.Align.CENTER
            c.drawText("EAT", eatBtn.centerX(), eatBtn.centerY() + 13f, paint)
            paint.textAlign = Paint.Align.LEFT
        } else {
            eatBtn.setEmpty()
        }

        // Joystick.
        if (joyActive) {
            paint.color = Color.argb(90, 255, 255, 255)
            c.drawCircle(joyBaseX, joyBaseY, 90f, paint)
            paint.color = Color.argb(180, 255, 255, 255)
            c.drawCircle(joyBaseX + joyDx * 90f, joyBaseY + joyDy * 90f, 40f, paint)
        } else {
            smallPaint.color = Color.rgb(140, 140, 140)
            smallPaint.textSize = 26f
            c.drawText("◉ drag to move", 24f, h - 300f, smallPaint)
        }

        // Message line.
        if (message.isNotEmpty()) {
            smallPaint.color = Color.WHITE
            smallPaint.textSize = 28f
            val tw = smallPaint.measureText(message)
            paint.color = Color.argb(200, 0, 0, 0)
            c.drawRoundRect(RectF(24f, h - 360f, 24f + tw + 32f, h - 306f), 16f, 16f, paint)
            c.drawText(message, 40f, h - 322f, smallPaint)
        }
    }

    private fun drawDialog(c: Canvas, w: Float, h: Float) {
        paint.color = Color.argb(200, 0, 0, 0)
        c.drawRect(0f, 0f, w, h, paint)
        paint.color = Color.rgb(30, 30, 40)
        c.drawRoundRect(RectF(w * 0.1f, h * 0.36f, w * 0.9f, h * 0.64f), 24f, 24f, paint)
        textPaint.textSize = 38f
        textPaint.color = Color.WHITE
        drawCentered(c, "Sleep rough here?", h * 0.44f, textPaint)
        smallPaint.textSize = 28f
        smallPaint.color = Color.rgb(200, 200, 200)
        drawCentered(c, "Less rest than camp. Advances to morning.", h * 0.50f, smallPaint)
        drawButton(c, buttonRect(w / 2 - w * 0.22f, h * 0.56f, w * 0.36f, 100f), "SLEEP")
        drawButton(c, buttonRect(w / 2 + w * 0.22f, h * 0.56f, w * 0.36f, 100f), "NO")
    }

    private fun drawPaused(c: Canvas, w: Float, h: Float) {
        paint.color = Color.argb(180, 0, 0, 0)
        c.drawRect(0f, 0f, w, h, paint)
        textPaint.textSize = 64f
        textPaint.color = Color.WHITE
        drawCentered(c, "PAUSED", h * 0.32f, textPaint)
        drawButton(c, buttonRect(w / 2, h * 0.42f, w * 0.7f, 110f), "RESUME")
        drawButton(c, buttonRect(w / 2, h * 0.54f, w * 0.7f, 110f), "HELP")
        drawButton(c, buttonRect(w / 2, h * 0.66f, w * 0.7f, 110f), "RESTART")
    }

    private fun drawHelp(c: Canvas, w: Float, h: Float) {
        c.drawColor(Color.rgb(11, 18, 32))
        textPaint.textSize = 56f
        textPaint.color = Color.rgb(245, 158, 11)
        drawCentered(c, "HOW TO PLAY", h * 0.12f, textPaint)
        smallPaint.textSize = 30f
        smallPaint.color = Color.WHITE
        val lines = listOf(
            "Drag bottom-left to move, or tap a tile.",
            "ACT near a tile to interact:",
            "  D green = dumpster (food, 3x/day)",
            "  K orange = soup kitchen (meal, 1x/day)",
            "  S blue = shelter (warm up, 1x/day)",
            "  C brown = camp — sleep here for full rest",
            "  P grey = bench — rest a little",
            "EAT button eats carried food.",
            "1 real minute = 1 game hour. Nights are cold.",
            "Health at 0 = game over. Autosaves hourly.",
            "",
            "Tap anywhere to go back.",
        )
        var y = h * 0.20f
        for (line in lines) {
            c.drawText(line, 40f, y, smallPaint)
            y += 44f
        }
    }

    private fun drawGameOver(c: Canvas, w: Float, h: Float) {
        c.drawColor(Color.rgb(20, 10, 10))
        textPaint.textSize = 64f
        textPaint.color = Color.rgb(239, 68, 68)
        drawCentered(c, "LOST TO THE COLD", h * 0.24f, textPaint)
        textPaint.textSize = 36f
        textPaint.color = Color.WHITE
        drawCentered(c, "You survived ${state.day} day(s) in Frostgate.", h * 0.34f, textPaint)
        smallPaint.textSize = 30f
        smallPaint.color = Color.rgb(200, 200, 200)
        drawCentered(c, "Final: Hunger ${state.hunger.toInt()}  Thirst ${state.thirst.toInt()}", h * 0.42f, smallPaint)
        drawCentered(c, "Warmth ${state.warmth.toInt()}  Energy ${state.energy.toInt()}  Food ${state.food}", h * 0.48f, smallPaint)
        drawCentered(c, "Every path teaches something. Try a different one.", h * 0.56f, smallPaint)
        drawCentered(c, "If you or someone you know needs shelter,", h * 0.63f, smallPaint)
        drawCentered(c, "call 211 (Canada/US) or visit a local shelter.", h * 0.68f, smallPaint)
        drawButton(c, buttonRect(w / 2, h * 0.72f, w * 0.7f, 110f), "TRY AGAIN")
    }
}
