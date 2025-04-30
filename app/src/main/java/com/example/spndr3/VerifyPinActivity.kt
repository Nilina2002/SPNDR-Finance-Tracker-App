package com.example.spndr3

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class VerifyPinActivity : AppCompatActivity() {
    private lateinit var pinInput: TextInputEditText
    private lateinit var pinInputLayout: TextInputLayout
    private lateinit var verifyButton: MaterialButton
    private lateinit var sharedPreferences: SharedPreferences
    private val PIN_PREF_KEY = "user_pin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_pin)

        sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)

        // Initialize views
        pinInput = findViewById(R.id.pinInput)
        pinInputLayout = findViewById(R.id.pinInputLayout)
        verifyButton = findViewById(R.id.buttonContinue)

        verifyButton.setOnClickListener {
            val enteredPin = pinInput.text.toString()
            val savedPin = sharedPreferences.getString(PIN_PREF_KEY, "")
            
            if (enteredPin.length != 4) {
                pinInputLayout.error = "PIN must be 4 digits"
                return@setOnClickListener
            }

            if (enteredPin == savedPin) {
                // Correct PIN, proceed to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Incorrect PIN
                pinInputLayout.error = getString(R.string.incorrect_pin)
                pinInput.text?.clear()
            }
        }
    }
} 