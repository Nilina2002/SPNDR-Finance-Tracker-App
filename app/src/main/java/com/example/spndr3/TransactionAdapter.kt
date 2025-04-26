package com.example.spndr3

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private var transactions: List<Transaction>) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
    private lateinit var sharedPreferences: SharedPreferences
    private val CURRENCY_PREF_KEY = "selected_currency"
    private val DEFAULT_CURRENCY = "Rs"

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val label: TextView = itemView.findViewById(R.id.label)
        val amount: TextView = itemView.findViewById(R.id.amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_layout, parent, false)
        sharedPreferences = parent.context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return TransactionViewHolder(view)
    }

//    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
//        val transaction = transactions[position]
//        holder.label.text = transaction.label
//
//        val currency = sharedPreferences.getString(CURRENCY_PREF_KEY, DEFAULT_CURRENCY)
//        holder.amount.text = "$currency ${transaction.amount}"
//    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.label.text = transaction.label

        val currency = sharedPreferences.getString(CURRENCY_PREF_KEY, DEFAULT_CURRENCY)
        holder.amount.text = "$currency ${transaction.amount}"

        // Set color: red for expenses, green for income
        val context = holder.amount.context
        if (transaction.amount < 0) {
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.red))
        } else {
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.green))
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun setData(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}