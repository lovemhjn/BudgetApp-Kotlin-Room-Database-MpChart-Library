package com.app.budgetapp.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.CalendarView
import android.widget.Toast
import androidx.room.Room
import com.app.budgetapp.db.RoomDb
import java.util.*

fun Activity.showToast(msg:String,duration:Int= Toast.LENGTH_SHORT){
    runOnUiThread {
        Toast.makeText(this, msg, duration).show()
    }
}

fun Context.showToast(msg:String,duration:Int= Toast.LENGTH_SHORT){

    Toast.makeText(this,msg,duration).show()
}

fun Context.goToActivity(activity:Class<out Activity>){
    val intent= Intent(this,activity)
    startActivity(intent)

}

fun Context.clearAllGoToActivity(activity:Class<out Activity>){
    val intent=Intent(this,activity)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
}

fun Context.Db():RoomDb{
    return RoomDb.getDatabase(this)
}

fun Context.getMonth():Int{
    return Calendar.getInstance().get(Calendar.MONTH)
}

fun Context.getYear():Int{
    return Calendar.getInstance().get(Calendar.YEAR)
}