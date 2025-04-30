package com.example.spndr3

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Calendar
import android.media.MediaPlayer



class AddTransactionActivity : AppCompatActivity() {
    private lateinit var addTransactionBtn: MaterialButton
    private lateinit var labelInput : TextInputEditText
    private lateinit var amountInput : TextInputEditText
    private lateinit var labelLayout : TextInputLayout
    private lateinit var amountLayout : TextInputLayout
    private lateinit var closeBtn : ImageButton
    private lateinit var categoryInput : TextInputEditText
    private lateinit var dateInput : TextInputEditText



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_transaction)

        //defining ids from the xml file
        addTransactionBtn = findViewById(R.id.addTransactionBtn)
        labelInput = findViewById(R.id.labelInput)
        amountInput = findViewById(R.id.amountInput)
        labelLayout = findViewById(R.id.labelLayout)
        amountLayout = findViewById(R.id.amountLayout)
        closeBtn = findViewById(R.id.closeBtn)
        categoryInput = findViewById(R.id.categoryInput)
        dateInput = findViewById(R.id.dateInput)



        //check if fields are empty or not
        labelInput.addTextChangedListener {
            if(it!!.isNotEmpty())
                labelLayout.error = null
        }

        amountInput.addTextChangedListener {
            if(it!!.isNotEmpty())
                amountLayout.error = null
        }



        addTransactionBtn.setOnClickListener {
            try {
                val label = labelInput.text.toString()
                val amount = amountInput.text.toString().toDoubleOrNull()
                val category = categoryInput.text.toString()
                val date = dateInput.text.toString()

                if(label.isEmpty())
                    labelLayout.error = "Please enter a valid label"

                else if(amount == null)
                    amountLayout.error = "Please enter a valid amount"

                else {
                    val transaction = Transaction(label ,amount ,0 , category, date)
                    insert(transaction)
                }
            } catch (e: Exception) {
                // Handle any unexpected errors
                android.widget.Toast.makeText(this, "An error occurred: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }

            closeBtn.setOnClickListener {
                finish()
            }
        }
        val dateInput = findViewById<TextInputEditText>(R.id.dateInput)

        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                dateInput.setText(formattedDate)
            }, year, month, day)

            datePickerDialog.show()
        }



    }
    private fun insert(transaction: Transaction) {
        try {
            val db = Room.databaseBuilder(this, AppDatabase::class.java, "transactions").build()

            GlobalScope.launch {
                try {
                    db.transactionDao().insertAll(transaction)

                    // Back to main thread to play sound and finish activity
                    runOnUiThread {
                        try {
                            val mediaPlayer = MediaPlayer.create(this@AddTransactionActivity, R.raw.success_sound)
                            if (mediaPlayer != null) {
                                mediaPlayer.start()
                                // Optional: release the media player after playback3
                                mediaPlayer.setOnCompletionListener {
                                    it.release()
                                }
                            } else {
                                android.widget.Toast.makeText(this@AddTransactionActivity, "Added Successfully", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            finish()
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(this@AddTransactionActivity, "Error playing sound: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        android.widget.Toast.makeText(this@AddTransactionActivity, "Error saving transaction: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Database error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}