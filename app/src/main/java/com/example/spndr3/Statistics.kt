package com.example.spndr3

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class Statistics : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StatisticsAdapter
    private lateinit var viewModel: StatisticsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        val dao = AppDatabase.getInstance(application).transactionDao()
        val factory = StatisticsViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[StatisticsViewModel::class.java]

        recyclerView = findViewById(R.id.statisticsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            viewModel.transactions.collectLatest { transactions ->
                val groupedData = transactions.groupBy {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val date = sdf.parse(it.date)
                    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date!!)
                }

                adapter = StatisticsAdapter(groupedData.toSortedMap(compareByDescending { it }))
                recyclerView.adapter = adapter
            }
        }

        viewModel.loadTransactions()
    }
}
