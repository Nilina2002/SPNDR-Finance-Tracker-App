package com.example.spndr3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room

class Summary : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var foodAdapter: TransactionAdapter
    private lateinit var transportAdapter: TransactionAdapter
    private lateinit var billsAdapter: TransactionAdapter
    private lateinit var entertainmentAdapter: TransactionAdapter
    private lateinit var foodRV: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        // Initialize DB
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "transactions")
            .allowMainThreadQueries() // For simplicity; avoid in production
            .build()

        // RecyclerViews
        foodRV = findViewById(R.id.foodRecyclerView)
        val transportRV = findViewById<RecyclerView>(R.id.transportRecyclerView)
        val billsRV = findViewById<RecyclerView>(R.id.billsRecyclerView)
        val entertainmentRV = findViewById<RecyclerView>(R.id.entertainmentRecyclerView)

        // Adapters
        foodAdapter = TransactionAdapter(emptyList())
        transportAdapter = TransactionAdapter(emptyList())
        billsAdapter = TransactionAdapter(emptyList())
        entertainmentAdapter = TransactionAdapter(emptyList())

        setupRecycler(foodRV, foodAdapter)
        setupRecycler(transportRV, transportAdapter)
        setupRecycler(billsRV, billsAdapter)
        setupRecycler(entertainmentRV, entertainmentAdapter)

        // Load and categorize
        val allTransactions = db.transactionDao().getAll()
        foodAdapter.setData(allTransactions.filter { it.category == "Food" })
        transportAdapter.setData(allTransactions.filter { it.category == "Transport" })
        billsAdapter.setData(allTransactions.filter { it.category == "Bills" })
        entertainmentAdapter.setData(allTransactions.filter { it.category == "Entertainment" })
    }

    private fun setupRecycler(rv: RecyclerView, adapter: TransactionAdapter) {
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }
}
