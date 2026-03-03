package com.redscreenfilter.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

/**
 * OverlayView - Custom View for Red Screen Overlay
 * 
 * Draws a full-screen red rectangle with configurable opacity.
 * Touch events pass through this view to underlying apps.
 */
class OverlayView(context: Context) : View(context) {
    
    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        alpha = 128 // Default 50% opacity (128/255)
    }
    
    /**
     * Set overlay opacity
     * @param opacity Float between 0.0 (transparent) and 1.0 (opaque)
     */
    fun setOpacity(opacity: Float) {
        val clampedOpacity = opacity.coerceIn(0.0f, 1.0f)
        paint.alpha = (clampedOpacity * 255).toInt()
        invalidate() // Request redraw
    }
    
    /**
     * Set overlay color
     * @param color Integer color value (e.g., Color.RED, Color.argb(...))
     */
    fun setOverlayColor(color: Int) {
        paint.color = color
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw full-screen red rectangle
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }
}
