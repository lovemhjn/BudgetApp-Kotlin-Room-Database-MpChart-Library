package com.app.budgetapp.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPrefs(context:Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)


    fun <T> saveValue(key: String, value: T) {
        when (value) {
            null -> prefs.edit().remove(key).apply()
            is String -> prefs.edit().putString(key, value).apply()
            is Int -> prefs.edit().putInt(key, value).apply()
            is Long -> prefs.edit().putLong(key, value).apply()
            is Float -> prefs.edit().putFloat(key, value).apply()
            is Boolean -> prefs.edit().putBoolean(key, value).apply()
            else -> throw IllegalArgumentException("Not Supported Type for SharedPreferences.")
        }
    }

    fun  getInt(key:String):Int{
        return prefs.getInt(key,0)
    }

    fun  getString(key:String):String {
        return prefs.getString(key,"")!!
    }

    fun  getLong(key:String):Long{
        return prefs.getLong(key,0)
    }

    fun  getFloat(key:String):Float{
        return prefs.getFloat(key,0f)
    }

    fun getBoolean(key:String,defValue:Boolean):Boolean{
        return prefs.getBoolean(key,defValue)
    }

    fun removeKey(key:String){
        prefs.edit().remove(key).apply()
    }

    fun clear(){
        prefs.edit().clear().apply()
    }

}