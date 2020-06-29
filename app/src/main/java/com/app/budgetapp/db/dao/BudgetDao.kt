package com.app.budgetapp.db.dao

import androidx.room.*
import com.app.budgetapp.model.Budget

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBudget(budget: Budget):Long


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBudget(budgetList: List<Budget>)

    @Update
    suspend fun updateBudget(budget: Budget?):Int

    @Query("SELECT * from budget")
    fun getBudgetList(): List<Budget>

    @Query("SELECT * from budget WHERE id!=:budgetId")
    fun getBudgetList(budgetId:Int): List<Budget>

    @Query("SELECT * from budget WHERE id=:budgetId")
    fun getBudget(budgetId:Int): Budget

    @Delete
    fun deleteBudget(budget: Budget): Int

    @Query("DELETE from budget")
    fun deleteBudget()

}