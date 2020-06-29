package com.app.budgetapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.app.budgetapp.db.dao.BudgetDao
import com.app.budgetapp.db.dao.ExpensesDao
import com.app.budgetapp.db.dao.FundsDao
import com.app.budgetapp.model.Budget
import com.app.budgetapp.model.Expenses
import com.app.budgetapp.model.Fund

@Database(entities = [Budget::class,Expenses::class,Fund::class], version = 1, exportSchema = false)
public abstract class RoomDb : RoomDatabase() {

    abstract fun budgetDao(): BudgetDao
    abstract fun expenseDao(): ExpensesDao
    abstract fun fundsDao():FundsDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: RoomDb? = null

        fun getDatabase(context: Context): RoomDb {
            val tempInstance =
                INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoomDb::class.java,
                    "database"
                ).fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}