package com.redscreenfilter.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val RsfTypography = Typography(
	headlineLarge = TextStyle(
		fontSize = 32.sp,
		lineHeight = 40.sp,
		fontWeight = FontWeight.Bold,
		letterSpacing = 0.sp
	),
	headlineMedium = TextStyle(
		fontSize = 28.sp,
		lineHeight = 34.sp,
		fontWeight = FontWeight.SemiBold,
		letterSpacing = 0.sp
	),
	titleLarge = TextStyle(
		fontSize = 22.sp,
		lineHeight = 28.sp,
		fontWeight = FontWeight.SemiBold,
		letterSpacing = 0.sp
	),
	titleMedium = TextStyle(
		fontSize = 16.sp,
		lineHeight = 22.sp,
		fontWeight = FontWeight.Medium,
		letterSpacing = 0.15.sp
	),
	bodyLarge = TextStyle(
		fontSize = 16.sp,
		lineHeight = 24.sp,
		fontWeight = FontWeight.Normal,
		letterSpacing = 0.2.sp
	),
	bodyMedium = TextStyle(
		fontSize = 14.sp,
		lineHeight = 20.sp,
		fontWeight = FontWeight.Normal,
		letterSpacing = 0.2.sp
	),
	labelLarge = TextStyle(
		fontSize = 14.sp,
		lineHeight = 20.sp,
		fontWeight = FontWeight.SemiBold,
		letterSpacing = 0.1.sp
	),
	labelMedium = TextStyle(
		fontSize = 12.sp,
		lineHeight = 16.sp,
		fontWeight = FontWeight.Medium,
		letterSpacing = 0.3.sp
	)
)
