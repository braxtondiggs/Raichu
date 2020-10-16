package com.cymbit.plastr

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val context = this
        val width = this.getWidth()
        Glide.with(this).asGif().load(R.drawable.loading).override(width/2, width/2).into(app_icon)

        val anim =  AnimationUtils.loadAnimation(this, R.anim.fade_in)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(context, MainActivity::class.java))
                    finish()
                }, 1000)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        app_icon.startAnimation(anim)
    }

    private fun getWidth(): Int {
        val displayMetrics = DisplayMetrics()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.display?.getRealMetrics(displayMetrics)
            displayMetrics.widthPixels
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }
}