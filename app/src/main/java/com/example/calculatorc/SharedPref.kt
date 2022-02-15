package com.example.calculatorc

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit

object SharedPref {
    lateinit var sharedPreferences: SharedPreferences

    fun getInstanceDis(context: Context) {
        sharedPreferences = context.getSharedPreferences(
            "" +
                    "", MODE_PRIVATE
        )
    }

    var user: Boolean?
        get() = sharedPreferences.getBoolean("user", false)
        set(value) = sharedPreferences.edit {
            if (value != null) {
                this.putBoolean("user", value)
            }
        }
}