package com.dmytrodanylyk

import android.os.Looper
import android.util.Log

fun logd(message: String) = Log.d("Coroutine Recipes", message)

fun getThreadMessage() = " [Is main thread ${Looper.myLooper() == Looper.getMainLooper()}] "