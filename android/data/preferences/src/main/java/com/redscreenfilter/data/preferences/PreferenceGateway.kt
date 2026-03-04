package com.redscreenfilter.data.preferences

interface PreferenceGateway {
    fun isOverlayEnabled(): Boolean
    fun setOverlayEnabled(enabled: Boolean)
    fun getOpacity(): Float
    fun setOpacity(opacity: Float)
    fun getColorVariant(): String
    fun setColorVariant(variant: String)
}
