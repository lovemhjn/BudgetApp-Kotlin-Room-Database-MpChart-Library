package com.app.budgetapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.budgetapp.R
import com.app.budgetapp.utils.goToActivity
import kotlinx.android.synthetic.main.activity_navigation.*

class NavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        btnBudgeting.setOnClickListener {
            goToActivity(MainActivity::class.java)
        }

        btnMoneySaving.setOnClickListener {
            goToActivity(PoliciesActivity::class.java)
        }
    }
}
