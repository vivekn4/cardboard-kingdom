package com.vn.cardboardkingdom

import android.app.Activity
import android.os.Bundle

/** Single-activity game: the whole M0 experience lives in GameView. */
class MainActivity : Activity() {
    private var gameView: GameView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameView = GameView(this)
        setContentView(gameView)
    }

    override fun onPause() {
        super.onPause()
        // SurfaceDestroyed saves; nothing else needed here.
    }
}
