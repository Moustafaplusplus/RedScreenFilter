package com.redscreenfilter.feature.settings.model

import com.redscreenfilter.core.model.ColorVariant

data class DisplaySettingsState(
    val isOverlayEnabled: Boolean,
    val opacity: Float,
    val opacityPercentage: Int,
    val colorVariant: ColorVariant
)
