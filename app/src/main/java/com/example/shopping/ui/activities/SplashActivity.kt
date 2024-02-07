package com.example.shopping.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.shopping.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

  private lateinit var views: ActivitySplashBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    views = ActivitySplashBinding.inflate(layoutInflater)
    setContentView(views.root)

    @Suppress("DEPRECATION")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      window.insetsController?.hide(WindowInsets.Type.statusBars())
    } else {
      window.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
      )
    }

    @Suppress("DEPRECATION")
    Handler().postDelayed(
      {
        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
        finish()
      },
      2500)
  }
}