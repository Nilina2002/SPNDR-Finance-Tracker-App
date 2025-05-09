package com.example.spndr3

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Transaction::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "transactions"
                ).build()
                INSTANCE = instance
                instance
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "transactions"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

