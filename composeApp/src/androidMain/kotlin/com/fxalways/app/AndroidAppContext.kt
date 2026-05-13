package com.fxalways.app

import android.content.Context
import androidx.activity.ComponentActivity

object AndroidAppContext {
    lateinit var context: Context
        private set
    var activity: ComponentActivity? = null
        private set

    fun init(activity: ComponentActivity) {
        this.activity = activity
        this.context = activity.applicationContext
    }
}
