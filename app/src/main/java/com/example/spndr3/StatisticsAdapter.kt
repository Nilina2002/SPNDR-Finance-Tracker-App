package com.example.spndr3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StatisticsAdapter(private val groupedData: Map<String, List<Transaction>>) :
    RecyclerView.Adapter<StatisticsAdapter.MonthlyViewHolder>() {

    private val months = groupedData.keys.toList()

    class MonthlyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val monthTitle: TextView = view.findViewById(R.id.monthTitle)
        val categorySummary: TextView = view.findViewById(R.id.categorySummary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthlyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_statistics_month, parent, false)
        return MonthlyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthlyViewHolder, position: Int) {
        val month = months[position]
        val transactions = groupedData[month]!!

        val categoryMap = transactions.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val summaryText = categoryMap.entries.joinToString("\n") { (category, amount) ->
            "$category: Rs %.2f".format(amount)
        }

        holder.monthTitle.text = month
        holder.categorySummary.text = summaryText
    }

    override fun getItemCount(): Int = months.size
}
