package edu.ignaciodetoro.androidtest

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : Fragment() {

    // Splash Screen duration.
    private val splashTimeOut = 2000L // 2 seconds

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ActionBar hidden.
        val actionBar = (activity as AppCompatActivity).supportActionBar!!
        actionBar.hide()

        // Scale and Rotation icon animation.
        val imageView = view.findViewById<ImageView>(R.id.splash_image)
        imageView.setImageResource(R.mipmap.ic_launcher_foreground)
        val scale = ObjectAnimator.ofPropertyValuesHolder(
            imageView,
            PropertyValuesHolder.ofFloat("scaleX", 0.1f, 2f),
            PropertyValuesHolder.ofFloat("scaleY", 0.1f, 2f)
        )
        scale.duration = 500
        scale.interpolator = AccelerateDecelerateInterpolator()

        // Transparency animation.
        val fadeOut = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0f)
        fadeOut.duration = 100
        fadeOut.interpolator = LinearInterpolator()
        fadeOut.startDelay = 1800

        // Sound added
        MediaPlayer.create(context, R.raw.ball).start()

        // Animations combined.
        val transition = AnimatorSet()
        transition.playTogether(scale, fadeOut)
        transition.start()

        Handler(Looper.getMainLooper()).postDelayed({
            actionBar.show()
            // Destroy fragment
            parentFragmentManager.beginTransaction().remove(this).commit()
        }, splashTimeOut)
    }
}