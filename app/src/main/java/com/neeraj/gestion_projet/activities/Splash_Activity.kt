package com.neeraj.gestion_projet.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.TextView
import com.neeraj.gestion_projet.R

class Splash_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val typeFace: Typeface = Typeface.createFromAsset(assets,"PlayfairDisplaySC-Bold.ttf")
        findViewById<TextView>(R.id.tv_app_name).typeface=typeFace

        Handler().postDelayed({
                              startActivity(Intent(this, IntroActivity::class.java))
            finish()
        },2500)
    }
}