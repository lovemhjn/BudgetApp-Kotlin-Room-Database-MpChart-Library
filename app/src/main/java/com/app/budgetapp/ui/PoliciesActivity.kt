package com.app.budgetapp.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.budgetapp.R
import com.app.budgetapp.utils.clearAllGoToActivity
import kotlinx.android.synthetic.main.activity_policies.*

class PoliciesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_policies)
        setOnClickListener()
    }

    private fun setOnClickListener() {
        imgHome.setOnClickListener{
            clearAllGoToActivity(MainActivity::class.java)
        }

        link1.setOnClickListener {
            openWeb(link1.text.toString())
        }

        link2.setOnClickListener {
            openWeb(link2.text.toString())
        }

        link3.setOnClickListener {
            openWeb(link3.text.toString())
        }
        link4.setOnClickListener {
            openWeb(link4.text.toString())
        }

        link5.setOnClickListener {
            openWeb(link5.text.toString())
        }

        link6.setOnClickListener {
            openWeb(link6.text.toString())
        }

    }

    private fun openWeb(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}
