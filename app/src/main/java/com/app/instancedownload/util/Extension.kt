package com.app.instancedownload.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.WindowManager

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun Context.changeStatusBarColor(activity: Activity) {
    val window = activity.window
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = Color.TRANSPARENT
}

//Instagram application installation or not check
fun isAppInstalledInstagram(activity: Activity): Boolean {
    val packageName = "com.instagram.android"
    val mIntent = activity.packageManager.getLaunchIntentForPackage(packageName)
    return mIntent != null
}