package com.redscreenfilter.service

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.redscreenfilter.core.model.ColorVariant

/**
 * OverlayView - Custom View for Red Screen Overlay
 * 
 * Draws a full-screen red rectangle with configurable opacity and color variant.
 * Supports color blindness variants for accessibility.
 * Touch events pass through this view to underlying apps.
 * Includes smooth fade transitions for opacity changes.
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
    
    private var redOpacityAnimator: ValueAnimator? = null
    private var dimOpacityAnimator: ValueAnimator? = null
    private val defaultAnimationDuration = 1000L // 1 second
    
    init {
        Log.d(TAG, "OverlayView created")
    }
    
    /**
     * Set overlay opacity with smooth fade animation
     * @param opacity Float between 0.0 (transparent) and 1.0 (opaque)
     * @param animate Whether to animate the transition (default: true)
     * @param duration Animation duration in milliseconds (default: 1000ms)
     */
    fun setOpacity(opacity: Float, animate: Boolean = true, duration: Long = defaultAnimationDuration) {
        if (animate) {
            setOpacityAnimated(opacity, duration)
        } else {
            setOpacityImmediate(opacity)
        }
    }
    
    /**
     * Set opacity immediately without animation
     */
    private fun setOpacityImmediate(opacity: Float) {
        val clampedOpacity = opacity.coerceIn(0.0f, 1.0f)
        val alphaValue = (clampedOpacity * 255).toInt()
        redPaint.alpha = alphaValue
        Log.d(TAG, "setOpacityImmediate: opacity=$opacity, alpha=$alphaValue")
        invalidate()
    }
    
    /**
     * Set opacity with smooth fade animation
     */
    private fun setOpacityAnimated(targetOpacity: Float, duration: Long) {
        val clampedTarget = targetOpacity.coerceIn(0.0f, 1.0f)
        val currentAlpha = redPaint.alpha
        val targetAlpha = (clampedTarget * 255).toInt()
        
        // Cancel any existing animation
        redOpacityAnimator?.cancel()
        
        // Create and start new animation
        redOpacityAnimator = ValueAnimator.ofInt(currentAlpha, targetAlpha).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                redPaint.alpha = animator.animatedValue as Int
                invalidate()
            }
            start()
        }
        
        Log.d(TAG, "setOpacityAnimated: from=$currentAlpha to=$targetAlpha over ${duration}ms")
    }

    /**
     * Set dim layer opacity with smooth fade animation
     * @param opacity Float between 0.0 (transparent) and 1.0 (opaque)
     * @param animate Whether to animate the transition (default: true)
     * @param duration Animation duration in milliseconds (default: 1000ms)
     */
    fun setDimOpacity(opacity: Float, animate: Boolean = true, duration: Long = defaultAnimationDuration) {
        if (animate) {
            setDimOpacityAnimated(opacity, duration)
        } else {
            setDimOpacityImmediate(opacity)
        }
    }
    
    /**
     * Set dim opacity immediately without animation
     */
    private fun setDimOpacityImmediate(opacity: Float) {
        val clampedOpacity = opacity.coerceIn(0.0f, 1.0f)
        val alphaValue = (clampedOpacity * 255).toInt()
        dimPaint.alpha = alphaValue
        Log.d(TAG, "setDimOpacityImmediate: opacity=$opacity, alpha=$alphaValue")
        invalidate()
    }
    
    /**
     * Set dim opacity with smooth fade animation
     */
    private fun setDimOpacityAnimated(targetOpacity: Float, duration: Long) {
        val clampedTarget = targetOpacity.coerceIn(0.0f, 1.0f)
        val currentAlpha = dimPaint.alpha
        val targetAlpha = (clampedTarget * 255).toInt()
        
        // Cancel any existing animation
        dimOpacityAnimator?.cancel()
        
        // Create and start new animation
        dimOpacityAnimator = ValueAnimator.ofInt(currentAlpha, targetAlpha).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                dimPaint.alpha = animator.animatedValue as Int
                invalidate()
            }
            start()
        }
        
        Log.d(TAG, "setDimOpacityAnimated: from=$currentAlpha to=$targetAlpha over ${duration}ms")
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
    }
}
