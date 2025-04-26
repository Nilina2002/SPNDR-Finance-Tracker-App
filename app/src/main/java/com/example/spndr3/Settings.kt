package com.example.spndr3

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*

class Settings : AppCompatActivity() {
    private val fileName = "transactions_backup.json"
    private lateinit var transactionDao: TransactionDao
    private lateinit var sharedPreferences: SharedPreferences
    private val CURRENCY_PREF_KEY = "selected_currency"
    private val DEFAULT_CURRENCY = "Rs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val db = AppDatabase.getDatabase(this)
        transactionDao = db.transactionDao()
        sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

        val backupBtn = findViewById<Button>(R.id.btn_backup)
        val restoreBtn = findViewById<Button>(R.id.btn_restore)
        val currencyBtn = findViewById<MaterialButton>(R.id.btn_currency)

        backupBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                backupData()
            }
        }

        restoreBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                restoreData()
            }
        }

        currencyBtn.setOnClickListener {
            showCurrencySelectionDialog()
        }

        // Set current currency text
        val currentCurrency = sharedPreferences.getString(CURRENCY_PREF_KEY, DEFAULT_CURRENCY)
        currencyBtn.text = "Currency ($currentCurrency)"
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
                findViewById<MaterialButton>(R.id.btn_currency).text = "Currency ($selectedCurrency)"
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun backupData() {
        try {
            val transactions = transactionDao.getAll()
            val json = Gson().toJson(transactions)

            openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }

            runOnUiThread {
                Toast.makeText(this, "Backup successful!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun restoreData() {
        try {
            // Use openFileInput instead of File to be consistent with backup
            openFileInput(fileName).use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }
                
                if (json.isEmpty()) {
                    runOnUiThread {
                        Toast.makeText(this, "Backup file is empty", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val type = object : TypeToken<List<Transaction>>() {}.type
                val transactions: List<Transaction> = Gson().fromJson(json, type)

                if (transactions.isEmpty()) {
                    runOnUiThread {
                        Toast.makeText(this, "No valid transactions found in backup", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                // Clear existing data before restoring
                transactionDao.deleteAll()
                // Insert the restored transactions
                transactionDao.insertAll(*transactions.toTypedArray())

                runOnUiThread {
                    Toast.makeText(this, "Restore successful!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: FileNotFoundException) {
            runOnUiThread {
                Toast.makeText(this, "No backup found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
