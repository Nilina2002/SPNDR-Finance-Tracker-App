package com.example.spndr3

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    val label:String,
    val amount:Double,
    @PrimaryKey(autoGenerate = true) val id: Int,
    val category: String,
    val date: String
    )
