package com.redscreenfilter.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.View
import com.redscreenfilter.data.ColorVariant

/**
 * OverlayView - Custom View for Red Screen Overlay
 * 
 * Draws a full-screen red rectangle with configurable opacity and color variant.
 * Supports color blindness variants for accessibility.
 * Touch events pass through this view to underlying apps.
 */
class OverlayView(context: Context) : View(context) {
    
    private val TAG = "OverlayView"
    
    private val paint = Paint().apply {
        color = ColorVariant.RED_STANDARD.colorValue
        style = Paint.Style.FILL
        alpha = 128 // Default 50% opacity (128/255)
    }
    
    init {
        Log.d(TAG, "OverlayView created")
    }
    
    /**
     * Set overlay opacity
     * @param opacity Float between 0.0 (transparent) and 1.0 (opaque)
     */
    fun setOpacity(opacity: Float) {
        val clampedOpacity = opacity.coerceIn(0.0f, 1.0f)
        val alphaValue = (clampedOpacity * 255).toInt()
        paint.alpha = alphaValue
        Log.d(TAG, "setOpacity: opacity=$opacity, alpha=$alphaValue")
        invalidate() // Request redraw
    }
    
    /**
     * Set overlay color
     * @param color Integer color value (e.g., Color.RED, Color.argb(...))
     */
    fun setOverlayColor(color: Int) {
        paint.color = color
        Log.d(TAG, "setOverlayColor: color=$color")
        invalidate()
    }
    
    /**
     * Set overlay color variant (for accessibility/color blindness)
     * @param variant ColorVariant enum value
     */
    fun setColorVariant(variant: ColorVariant) {
        paint.color = variant.colorValue
        Log.d(TAG, "setColorVariant: variant=${variant.displayName}, color=${variant.colorValue}")
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw full-screen red rectangle
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        Log.d(TAG, "onDraw: width=$width, height=$height, alpha=${paint.alpha}")
    }
}
