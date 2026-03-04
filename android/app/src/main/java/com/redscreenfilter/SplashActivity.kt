package com.redscreenfilter

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import android.view.ViewGroup
import com.airbnb.lottie.LottieAnimationView

class SplashActivity : AppCompatActivity() {

    private val TAG = "SplashActivity"
    private var hasNavigated = false
    private val splashDuration = 5200L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            SplashComposeScreen(
                onLottieError = { throwable ->
                    Log.e(TAG, "Failed to load splash Lottie animation", throwable)
                }
            )
        }

        Handler(Looper.getMainLooper()).postDelayed({ launchMain() }, splashDuration)
    }

    @Composable
    private fun SplashComposeScreen(onLottieError: (Throwable) -> Unit) {
        var visible by remember { mutableStateOf(false) }
        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(durationMillis = 600),
            label = "splashAlpha"
        )

        LaunchedEffect(Unit) {
            visible = true
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF050608).copy(alpha = alpha),
                            Color(0xFF14161E).copy(alpha = alpha),
                            Color(0xFF1E212E).copy(alpha = alpha)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { context ->
                    LottieAnimationView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setAnimation(R.raw.splash_wave)
                        repeatCount = 1
                        speed = 1f
                        setFailureListener { throwable -> 
                            Log.e(TAG, "Lottie animation error", throwable)
                            onLottieError(throwable) 
                        }
                        playAnimation()
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )
        }
    }

    private fun launchMain() {
        if (hasNavigated) return
        hasNavigated = true
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
