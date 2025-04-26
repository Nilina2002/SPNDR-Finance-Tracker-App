package com.example.spndr3

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.spndr3.R

class OnBoard1 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_board1)

        val mainLayout = findViewById<ConstraintLayout>(R.id.main)

        mainLayout.setOnClickListener {
            val intent = Intent(this, OnBoard2::class.java) // Replace with your next activity
            startActivity(intent)
        }
    }
}
