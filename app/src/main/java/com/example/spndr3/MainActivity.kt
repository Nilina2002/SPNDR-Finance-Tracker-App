package com.example.spndr3

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private lateinit var deletedTransaction: Transaction
    private lateinit var transactions: List<Transaction>
    private lateinit var oldTransactions: List<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var balance: TextView
    private lateinit var budget: TextView
    private lateinit var expense: TextView
    private lateinit var viewModel: MainViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private val CURRENCY_PREF_KEY = "selected_currency"
    private val DEFAULT_CURRENCY = "Rs"

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val addBtn = findViewById<FloatingActionButton>(R.id.addBtn) // after setContentView
        recyclerView = findViewById(R.id.recyclerview)
        balance = findViewById(R.id.balance)
        budget = findViewById(R.id.budget)
        expense = findViewById(R.id.expense)

        // Transactions
        transactions = arrayListOf()

        transactionAdapter = TransactionAdapter(transactions)
        linearLayoutManager = LinearLayoutManager(this)

        db = Room.databaseBuilder(this, AppDatabase::class.java, "transactions").build()

        recyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = transactionAdapter
        }

        //swipe to remove
        val itemTouchHelper = object :ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteTransaction(transactions[viewHolder.adapterPosition] )
            }

        }

        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(recyclerView)

        addBtn.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_1 -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.item_2 -> {
                    val intent = Intent(this, Summary::class.java)
                    startActivity(intent)
                    true
                }
                R.id.item_3 -> {
                    val intent = Intent(this, Settings::class.java)
                    startActivity(intent)
                    true
                }
                R.id.item_4-> {
                    val intent = Intent(this, Statistics::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)

        val dao = AppDatabase.getInstance(application).transactionDao()
        val factory = MainViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        lifecycleScope.launch {
            viewModel.transactions.collectLatest { transactions ->
                transactionAdapter.setData(transactions)
                updateBalance(transactions)
            }
        }

        viewModel.loadTransactions()

        if (this::class != MainActivity::class) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    private fun fetchAll(){
        GlobalScope.launch {
            transactions = db.transactionDao().getAll()

            runOnUiThread(){
                updateDashboard()
                transactionAdapter.setData(transactions)
            }
        }
    }

    private fun updateDashboard() {
        val totalAmount = transactions.sumOf { it.amount }
        val budgetAmount = transactions.filter { it.amount > 0 }.sumOf { it.amount }
        val expenseAmount = totalAmount - budgetAmount

        val currency = sharedPreferences.getString(CURRENCY_PREF_KEY, DEFAULT_CURRENCY)

        balance.text = "$currency %.2f".format(totalAmount)
        budget.text = "$currency %.2f".format(budgetAmount)
        expense.text = "$currency %.2f".format(expenseAmount)

        if (totalAmount < 0) {
            showNegativeBalanceNotification(totalAmount)
        }
    }

    private fun updateBalance(transactions: List<Transaction>) {
        val balanceTextView = findViewById<TextView>(R.id.balance)
        val expenseTextView = findViewById<TextView>(R.id.expense)
        val budgetTextView = findViewById<TextView>(R.id.budget)

        val totalBalance = transactions.sumOf { it.amount }
        val expenses = transactions.filter { it.amount < 0 }.sumOf { -it.amount }
        val budget = 400.0 // This should be configurable in settings

        val currency = sharedPreferences.getString(CURRENCY_PREF_KEY, DEFAULT_CURRENCY)
        
        balanceTextView.text = "$currency $totalBalance"
        expenseTextView.text = "$currency $expenses"
        budgetTextView.text = "$currency $budget"

        if (totalBalance < 0) {
            showNegativeBalanceNotification(totalBalance)
        }
    }

    private fun undoDelete(){
        GlobalScope.launch {
            db.transactionDao().insertAll(deletedTransaction)

            transactions = oldTransactions
            runOnUiThread{
                transactionAdapter.setData(transactions)
                updateDashboard()
            }
        }
    }

    private fun showSnackBar(){
        val view = findViewById<View>(R.id.coordinator)
        val snackbar = Snackbar.make(view, "Transaction deleted!", Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo"){
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this, R.color.red))
            .setActionTextColor(ContextCompat.getColor(this, R.color.white))
            .show()

    }

    private fun deleteTransaction(transaction: Transaction){
        deletedTransaction = transaction
        oldTransactions = transactions

        GlobalScope.launch {
            db.transactionDao().delete(transaction)

            transactions = transactions.filter { it.id!=transaction.id }
            runOnUiThread{
                updateDashboard()
                transactionAdapter.setData(transactions)
                showSnackBar()
            }
        }
    }

    private fun showNegativeBalanceNotification(balance: Double) {
        val channelId = "balance_alert_channel"
        val notificationId = 1

        // Create the NotificationChannel (required for Android O+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Balance Alerts"
            val descriptionText = "Notifications for negative balance"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Use your own icon here
            .setContentTitle("Warning: Negative Balance!")
            .setContentText("Your total balance is negative: $balance")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Check permission before showing notification
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            with(NotificationManagerCompat.from(this)) {
                notify(notificationId, builder.build())
            }
        }
    }

    override fun onResume(){
        super.onResume()
        fetchAll()
    }
}