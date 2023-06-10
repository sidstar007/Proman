package com.management.proman.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.management.proman.R

class IntroActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val tvAppName = findViewById<TextView>(R.id.tv_app_name_intro)
        val typeface: Typeface = Typeface.createFromAsset(assets, "Preparatori-Bold.ttf")
        tvAppName.typeface = typeface

        val signupBtn = findViewById<Button>(R.id.btn_sign_up_intro)
        signupBtn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        val signinBtn = findViewById<Button>(R.id.btn_sign_in_intro)
        signinBtn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}