package com.example.spndr3

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.spndr3.R

class OnBoard1 : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private val PIN_PREF_KEY = "user_pin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_board1)

        sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val mainLayout = findViewById<ConstraintLayout>(R.id.main)

        mainLayout.setOnClickListener {
            val savedPin = sharedPreferences.getString(PIN_PREF_KEY, "")
            
            if (savedPin.isNullOrEmpty()) {
                // No PIN exists, proceed with onboarding
                val intent = Intent(this, OnBoard2::class.java)
                startActivity(intent)
            } else {
                // PIN exists, show verification screen
                val intent = Intent(this, VerifyPinActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
