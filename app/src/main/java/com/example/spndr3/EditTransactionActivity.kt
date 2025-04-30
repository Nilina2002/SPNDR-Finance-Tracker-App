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

class EditTransactionActivity : AppCompatActivity() {
    private lateinit var updateTransactionBtn: MaterialButton
    private lateinit var labelInput: TextInputEditText
    private lateinit var amountInput: TextInputEditText
    private lateinit var labelLayout: TextInputLayout
    private lateinit var amountLayout: TextInputLayout
    private lateinit var closeBtn: ImageButton
    private lateinit var categoryInput: TextInputEditText
    private lateinit var dateInput: TextInputEditText
    private lateinit var db: AppDatabase
    private var transactionId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_transaction)

        // Get transaction ID from intent
        transactionId = intent.getIntExtra("transaction_id", -1)
        if (transactionId == -1) {
            finish()
            return
        }

        // Initialize database
        db = Room.databaseBuilder(this, AppDatabase::class.java, "transactions").build()

        // Initialize views
        updateTransactionBtn = findViewById(R.id.addTransactionBtn)
        labelInput = findViewById(R.id.labelInput)
        amountInput = findViewById(R.id.amountInput)
        labelLayout = findViewById(R.id.labelLayout)
        amountLayout = findViewById(R.id.amountLayout)
        closeBtn = findViewById(R.id.closeBtn)
        categoryInput = findViewById(R.id.categoryInput)
        dateInput = findViewById(R.id.dateInput)

        // Load existing transaction data
        GlobalScope.launch {
            val transaction = db.transactionDao().getAll().find { it.id == transactionId }
            runOnUiThread {
                if (transaction != null) {
                    labelInput.setText(transaction.label)
                    amountInput.setText(transaction.amount.toString())
                    categoryInput.setText(transaction.category)
                    dateInput.setText(transaction.date)
                }
            }
        }

        // Set up text change listeners
        labelInput.addTextChangedListener {
            if (it!!.isNotEmpty())
                labelLayout.error = null
        }

        amountInput.addTextChangedListener {
            if (it!!.isNotEmpty())
                amountLayout.error = null
        }

        // Set up date picker
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

        // Set up update button
        updateTransactionBtn.text = getString(R.string.update_transaction)
        updateTransactionBtn.setOnClickListener {
            try {
                val label = labelInput.text.toString()
                val amount = amountInput.text.toString().toDoubleOrNull()
                val category = categoryInput.text.toString()
                val date = dateInput.text.toString()

                if (label.isEmpty())
                    labelLayout.error = "Please enter a valid label"
                else if (amount == null)
                    amountLayout.error = "Please enter a valid amount"
                else {
                    val updatedTransaction = Transaction(label, amount, transactionId, category, date)
                    updateTransaction(updatedTransaction)
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(this, "An error occurred: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        // Set up close button
        closeBtn.setOnClickListener {
            finish()
        }
    }

    private fun updateTransaction(transaction: Transaction) {
        GlobalScope.launch {
            try {
                db.transactionDao().update(transaction)
                runOnUiThread {
                    android.widget.Toast.makeText(this@EditTransactionActivity, "Transaction updated successfully", android.widget.Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    android.widget.Toast.makeText(this@EditTransactionActivity, "Error updating transaction: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 