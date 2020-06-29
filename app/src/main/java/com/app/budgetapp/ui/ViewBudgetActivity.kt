package com.app.budgetapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.room.Room
import com.app.budgetapp.R
import com.app.budgetapp.db.RoomDb
import com.app.budgetapp.model.Expenses
import com.app.budgetapp.utils.Coroutines
import com.app.budgetapp.utils.clearAllGoToActivity
import com.app.budgetapp.utils.getMonth
import com.app.budgetapp.utils.getYear
import kotlinx.android.synthetic.main.activity_view_budget.*

class ViewBudgetActivity : AppCompatActivity(), ExpenseListAdapter.ExpenseEventListener {

    val list by lazy { ArrayList<Expenses>() }
    val adapter by lazy { ExpenseListAdapter(list,this) }
    var categoryId:Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_budget)
        categoryId = intent.getIntExtra("BUDGET_ID",0)
        initRecyclerView()

        imgHome.setOnClickListener{
            clearAllGoToActivity(MainActivity::class.java)
        }
    }

    private fun initRecyclerView() {
        list.addAll(RoomDb.getDatabase(this).expenseDao().getExpensesList(categoryId))
        rvExpenses.adapter = adapter
        if(list.isEmpty()){
            tvEmpty.visibility = View.VISIBLE
        }
    }

    override fun onDelete(position: Int) {
        val budget = RoomDb.getDatabase(this).fundsDao().getFund(categoryId,getMonth(),getYear())
        budget?.remainingBudget = budget?.remainingBudget?.plus( list[position].expense)
        Coroutines.main {
            RoomDb.getDatabase(this).fundsDao().updateFund(budget)
        }
        RoomDb.getDatabase(this).expenseDao().deleteExpense(list[position])
        list.removeAt(position)
        adapter.notifyDataSetChanged()
    }
}
