package com.app.budgetapp.db.dao

import androidx.room.*
import com.app.budgetapp.model.Fund

@Dao
interface FundsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFund(fund:Fund?):Long

    @Update
    fun updateFund(fund:Fund?):Int

    @Query("SELECT * FROM fund WHERE categoryId=:catId AND month=:mMonth AND year=:mYear")
    fun getFund(catId:Int?,mMonth:Int,mYear:Int):Fund?

    @Query("SELECT * FROM fund WHERE categoryId=:catId")
    fun getFund(catId:Int?):List<Fund>

    @Query("SELECT SUM(initialBudget) FROM fund WHERE month=:month and year=:year")
    fun getTotal(month:Int?,year:Int?):Double

    @Query("SELECT SUM(initialBudget) FROM fund WHERE year=:year")
    fun getTotalOfYear(year:Int):Double

    @Query("SELECT SUM(initialBudget) FROM fund WHERE month=:month")
    fun getTotalOfMonth(month:Int):Double

    @Query("DELETE FROM fund where categoryId =:catId")
    fun deleteFunds(catId: Int?): Int

    @Query("SELECT SUM(initialBudget)- SUM(remainingBudget) FROM fund WHERE year=:year")
    fun getTotalExpense(year:Int):Double

    fun upsert(fund:Fund?):Int{
        var id = insertFund(fund)
        if(id==-1L){
            id = updateFund(fund).toLong()
        }

        return id.toInt()
    }
}