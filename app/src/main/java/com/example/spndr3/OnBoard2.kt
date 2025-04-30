package com.example.spndr3

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.AnimationUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class OnBoard2 : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private val CURRENCY_PREF_KEY = "selected_currency"
    private val DEFAULT_CURRENCY = "Rs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_board2)

        sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)

        // Find views by ID
        val logoImageView = findViewById<ImageView>(R.id.imageViewLogo)
        val descriptionTextView = findViewById<TextView>(R.id.textViewDescription)
        val continueButton = findViewById<MaterialButton>(R.id.buttonContinue)
        val currencyButton = findViewById<MaterialButton>(R.id.buttonCurrency)

        // Load animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.logo_fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.text_slide_up)
        val bounce = AnimationUtils.loadAnimation(this, R.anim.button_bounce)

        // Start animations
        logoImageView.startAnimation(fadeIn)
        descriptionTextView.startAnimation(slideUp)
        continueButton.startAnimation(bounce)
        currencyButton.startAnimation(bounce)

        // Set current currency text
        val currentCurrency = sharedPreferences.getString(CURRENCY_PREF_KEY, DEFAULT_CURRENCY)
        currencyButton.text = "Currency ($currentCurrency)"

        currencyButton.setOnClickListener {
            showCurrencySelectionDialog()
        }

        continueButton.setOnClickListener {
            val intent = Intent(this, PinActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showCurrencySelectionDialog() {
        val currencies = arrayOf("Rs", "$", "€", "£", "¥")
        val currentCurrency = sharedPreferences.getString(CURRENCY_PREF_KEY, DEFAULT_CURRENCY)
        val currentIndex = currencies.indexOf(currentCurrency)

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Currency")
            .setSingleChoiceItems(currencies, currentIndex) { dialog, which ->
                val selectedCurrency = currencies[which]
                sharedPreferences.edit().putString(CURRENCY_PREF_KEY, selectedCurrency).apply()
                findViewById<MaterialButton>(R.id.buttonCurrency).text = "Currency ($selectedCurrency)"
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
