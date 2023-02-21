package edu.ignaciodetoro.androidtest

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    // Splash Screen duration.
    private val splashTimeOut = 2000L // 2 segundos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // ActionBar hidden.
        supportActionBar?.hide()

        // Scale and Rotation icon animation.
        val imageView = findViewById<ImageView>(R.id.splash_image)
        imageView.setImageResource(R.mipmap.ic_launcher_foreground)
        val scale = ObjectAnimator.ofPropertyValuesHolder(
            imageView,
            PropertyValuesHolder.ofFloat("scaleX", 0.1f, 2f),
            PropertyValuesHolder.ofFloat("scaleY", 0.1f, 2f)
        )
        scale.duration = 500
        scale.interpolator = AccelerateDecelerateInterpolator()
        val rotation = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 720f)
        rotation.duration = 700
        rotation.interpolator = LinearInterpolator()
        rotation.repeatCount = ObjectAnimator.INFINITE
        val set = AnimatorSet()
        set.playTogether(rotation, scale)

        // Transparency animation.
        val fadeOut = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0f)
        fadeOut.duration = 100
        fadeOut.interpolator = LinearInterpolator()
        fadeOut.startDelay = 1800

        // Animations combined.
        val transition = AnimatorSet()
        transition.playTogether(set, fadeOut)
        transition.start()

        // Launch MainActivity after animation.
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, splashTimeOut)
    }
}