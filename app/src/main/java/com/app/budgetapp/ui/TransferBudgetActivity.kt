package com.app.budgetapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.app.budgetapp.R
import com.app.budgetapp.db.RoomDb
import com.app.budgetapp.model.Budget
import com.app.budgetapp.model.Fund
import com.app.budgetapp.utils.*
import kotlinx.android.synthetic.main.activity_transfer_budget.*
import kotlin.math.max

class TransferBudgetActivity : AppCompatActivity() {

    private val budgetList by lazy { ArrayList<Budget>() }
    var catIndex = 0
    var transferFrom: Fund? = null
    var transferTo: Fund? = null
    var catId = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_budget)
        initData()
        setSpinner()
        btnTransfer.setOnClickListener {
            Coroutines.main {
                makeTransfer()
            }
        }

        imgHome.setOnClickListener {
            clearAllGoToActivity(MainActivity::class.java)
        }
    }

    private fun initData() {
        catId = intent.getIntExtra("BUDGET_ID", 0)
        transferFrom = RoomDb.getDatabase(this).fundsDao().getFund(catId, getMonth(), getYear())
        tvFrom.text = Db().budgetDao().getBudget(catId).category
    }

    private fun makeTransfer() {
        transferTo = Db().fundsDao().getFund(budgetList[catIndex].id, getMonth(), getYear())
        if (transferTo == null) {
            val newFund = Fund(budgetList[catIndex].id, 0.0, 0.0)
            val id = RoomDb.getDatabase(this).fundsDao().insertFund(newFund)
            transferTo = Db().fundsDao().getFund(budgetList[catIndex].id, getMonth(), getYear())

        }

        if (transferFrom != null) {
            if (!TextUtils.isEmpty(etAmount.text)) {
                val maxAmt = transferFrom?.remainingBudget
                val amt = etAmount.text.toString().toDouble()
                if (amt <= maxAmt!!) {
                    transferFrom?.remainingBudget = transferFrom?.remainingBudget?.minus(amt)
                    transferFrom?.initialBudget = transferFrom?.initialBudget?.minus(amt)
                    transferTo?.remainingBudget = transferTo?.remainingBudget?.plus(amt)
                    transferTo?.initialBudget = transferTo?.initialBudget?.plus(amt)

                    RoomDb.getDatabase(this).fundsDao().updateFund(transferFrom)
                    RoomDb.getDatabase(this).fundsDao().updateFund(transferTo)
                    showToast("Budget amount has been transferred successfully.")
                    finish()

                } else {
                    showToast("Please enter the amount less than or equals to the balance amount of head.")
                }
            } else {
                showToast("Please enter the amount you want to transfer.")
            }
        } else {
            showToast("Please enter the amount less than or equals to the balance amount of head.")

        }
    }

    private fun setSpinner() {
        budgetList.addAll(RoomDb.getDatabase(this).budgetDao().getBudgetList(catId))
        val categoryArr = arrayOfNulls<String>(budgetList.size)
        var index = 0;
        budgetList.forEach {
            categoryArr[index++] = it.category
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categoryArr)
        categorySpinner.adapter = adapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                catIndex = position
            }

        }
    }
}
