package com.redscreenfilter

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.airbnb.lottie.LottieAnimationView

class SplashActivity : AppCompatActivity() {

    private val TAG = "SplashActivity"
    private var hasNavigated = false
    private val splashDuration = 5200L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_splash)

        val lottieView = findViewById<LottieAnimationView>(R.id.lottie_splash)
        val backdrop = findViewById<View>(R.id.splash_backdrop)

        lottieView.setAnimation(R.raw.splash_wave)
        lottieView.repeatCount = 1
        lottieView.speed = 1f
        lottieView.setFailureListener { throwable ->
            Log.e(TAG, "Failed to load splash Lottie animation", throwable)
        }

        lottieView.playAnimation()
        backdrop.alpha = 0f
        backdrop.animate().alpha(1f).setDuration(600L).start()

        Handler(Looper.getMainLooper()).postDelayed({ launchMain() }, splashDuration)
    }

    private fun launchMain() {
        if (hasNavigated) return
        hasNavigated = true
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
