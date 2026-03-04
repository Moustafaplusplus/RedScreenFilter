package com.redscreenfilter.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.view.View
import com.redscreenfilter.core.model.ColorVariant

/**
 * OverlayView - Custom View for Red Screen Overlay
 * 
 * Draws a full-screen red rectangle with configurable opacity and color variant.
 * Supports color blindness variants for accessibility.
 * Touch events pass through this view to underlying apps.
 */
class OverlayView(context: Context) : View(context) {
    
    private val TAG = "OverlayView"
    
    private val redPaint = Paint().apply {
        style = Paint.Style.FILL
        color = ColorVariant.RED_STANDARD.colorValue
        alpha = 128
    }

    private val dimPaint = Paint().apply {
        style = Paint.Style.FILL
        color = 0xFF000000.toInt()
        alpha = 0
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
        redPaint.alpha = alphaValue
        Log.d(TAG, "setOpacity: opacity=$opacity, clampedOpacity=$clampedOpacity, alpha=$alphaValue, paintAlpha=${redPaint.alpha}")
        invalidate() // Request redraw
    }

    fun setDimOpacity(opacity: Float) {
        val clampedOpacity = opacity.coerceIn(0.0f, 1.0f)
        val alphaValue = (clampedOpacity * 255).toInt()
        dimPaint.alpha = alphaValue
        Log.d(TAG, "setDimOpacity: opacity=$opacity, clampedOpacity=$clampedOpacity, alpha=$alphaValue, paintAlpha=${dimPaint.alpha}")
        invalidate()
    }
    
    /**
     * Set overlay color
     * @param color Integer color value (e.g., Color.RED, Color.argb(...))
     */
    fun setOverlayColor(color: Int) {
        val currentAlpha = redPaint.alpha
        redPaint.color = color
        redPaint.alpha = currentAlpha
        Log.d(TAG, "setOverlayColor: color=$color, preservedAlpha=$currentAlpha")
        invalidate()
    }
    
    /**
     * Set overlay color variant (for accessibility/color blindness)
     * @param variant ColorVariant enum value
     */
    fun setColorVariant(variant: ColorVariant) {
        val currentAlpha = redPaint.alpha
        // Extract RGB and set alpha to 255 so paint.alpha controls transparency
        val colorWithFullAlpha = (variant.colorValue and 0x00FFFFFF) or 0xFF000000.toInt()
        redPaint.color = colorWithFullAlpha
        redPaint.alpha = currentAlpha
        Log.d(TAG, "setColorVariant: variant=${variant.displayName}, color=${String.format("0x%08X", colorWithFullAlpha)}, paintAlpha=${redPaint.alpha}")
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), redPaint)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)
        Log.d(TAG, "onDraw: width=$width, height=$height, redAlpha=${redPaint.alpha}, dimAlpha=${dimPaint.alpha}")
    }
}
