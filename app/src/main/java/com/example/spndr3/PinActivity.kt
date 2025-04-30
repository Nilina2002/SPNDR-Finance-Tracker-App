package com.example.spndr3

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class PinActivity : AppCompatActivity() {
    private lateinit var pinInput: TextInputEditText
    private lateinit var pinInputLayout: TextInputLayout
    private lateinit var continueButton: MaterialButton
    private lateinit var sharedPreferences: SharedPreferences
    private val PIN_PREF_KEY = "user_pin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)

        // Initialize views
        pinInput = findViewById(R.id.pinInput)
        pinInputLayout = findViewById(R.id.pinInputLayout)
        continueButton = findViewById(R.id.buttonContinue)

        continueButton.setOnClickListener {
            val pin = pinInput.text.toString()
            
            if (pin.length != 4) {
                pinInputLayout.error = "PIN must be 4 digits"
                return@setOnClickListener
            }

            // Save the PIN
            sharedPreferences.edit().putString(PIN_PREF_KEY, pin).apply()

            // Navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
} 