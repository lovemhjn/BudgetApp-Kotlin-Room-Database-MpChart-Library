package com.app.budgetapp.ui

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.core.view.GravityCompat
import com.app.budgetapp.R
import com.app.budgetapp.db.RoomDb
import com.app.budgetapp.model.Budget
import com.app.budgetapp.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setOnClickListeners()

        saveInitialHeads()
        setData()
    }

    private fun setData() {
        val imageBytes = Base64.decode(SharedPrefs(this).getString("IMAGE"), Base64.DEFAULT)
        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        img.setImageBitmap(decodedImage)

       tvName.text = SharedPrefs(this).getString("NAME")
       tvEmail.text =  SharedPrefs(this).getString("EMAIL")
    }

    private fun saveInitialHeads() {
        if(SharedPrefs(this).getBoolean("INITIAL",true)) {
            val headList = arrayListOf(
                Budget("Food", null),
                Budget("Education", null),
                Budget("Medical", null),
                Budget("Travel", null),
                Budget("Clothes", null),
                Budget("Electronics", null),
                Budget("Entertainment", null),
                Budget("Utility Bills", null),
                Budget("Fuel Charges", null)
            )
            Coroutines.main {
                RoomDb.getDatabase(this).budgetDao().insertBudget(headList)
                SharedPrefs(this).saveValue("INITIAL",false)
                runOnUiThread {
                    initRecyclerView()
                }
            }
        }
    }

    private fun initRecyclerView() {
        val list  = RoomDb.getDatabase(this).budgetDao().getBudgetList() as ArrayList<Budget>
        if(!list.isNullOrEmpty()){
            imgBanner.visibility = View.GONE
            btnAddExpense.visibility = View.VISIBLE
        }else{
            imgBanner.visibility = View.VISIBLE
            btnAddExpense.visibility = View.GONE
        }
        rvBudget.adapter = BudgetListAdapter(list,object :BudgetListAdapter.BudgetListener{
            override fun deleteBudget() {
                tvTotal.text = "Total Budget: ${getString(R.string.currency)} ${Db().fundsDao().getTotal(getMonth(),getYear())}"
            }

        })
        tvTotal.text = "Total Budget: ${getString(R.string.currency)} ${Db().fundsDao().getTotal(getMonth(),getYear())}"
    }

    private fun setOnClickListeners() {
        btnAddExpense.setOnClickListener {
            drawer.closeDrawer(GravityCompat.START)
            goToActivity(AddExpenseActivity::class.java)
        }

        btnCreateBudget.setOnClickListener {
            drawer.closeDrawer(GravityCompat.START)
            goToActivity(CreateBudgetActivity::class.java)
        }

        toolBar.setNavigationOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }

        tvClear.setOnClickListener {
            RoomDb.getDatabase(this).expenseDao().deleteExpenses()
            RoomDb.getDatabase(this).budgetDao().deleteBudget()
            initRecyclerView()
        }

        btnAnalytics.setOnClickListener {
            drawer.closeDrawer(GravityCompat.START)
            goToActivity(AnalyticsActivity::class.java)
        }

        btnLogout.setOnClickListener {
            SharedPrefs(this).removeKey("LOGGEDIN")
            clearAllGoToActivity(GetProfileActivity::class.java)
        }

        btnWeb.setOnClickListener {
            drawer.closeDrawer(GravityCompat.START)
            goToActivity(PoliciesActivity::class.java)
        }
    }

    override fun onResume() {
        super.onResume()
        initRecyclerView()
    }

}
