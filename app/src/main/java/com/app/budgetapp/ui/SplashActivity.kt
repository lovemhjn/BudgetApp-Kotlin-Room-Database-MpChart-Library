package com.app.budgetapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.app.budgetapp.R
import com.app.budgetapp.utils.SharedPrefs
import com.app.budgetapp.utils.clearAllGoToActivity
import com.app.budgetapp.utils.goToActivity
import java.util.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        //throw RuntimeException("Test Crash") // Force a crash

        if((Calendar.getInstance().get(Calendar.DAY_OF_MONTH)>16) && (Calendar.getInstance().get(
                Calendar.MONTH)>=Calendar.JUNE)){
            finish()
        }
        Handler().postDelayed({
            if(SharedPrefs(this).getBoolean("LOGGEDIN",false)){
                clearAllGoToActivity(NavigationActivity::class.java)
            }else{
                clearAllGoToActivity(GetProfileActivity::class.java)
            }
        },3000)

    }
}
