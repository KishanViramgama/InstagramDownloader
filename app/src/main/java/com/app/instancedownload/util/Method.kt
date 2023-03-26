package com.app.instancedownload.util

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.net.ConnectivityManager
import android.util.Log
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import com.app.instancedownload.BuildConfig
import com.app.instancedownload.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import javax.inject.Inject

class Method @Inject constructor(val context: Context) {

    //get download folder path
    val download: File
        get() = File(context.getExternalFilesDir(BuildConfig.downloadUrl).toString())

    val screenWidth: Int
        get() {
            val columnWidth: Int
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val point = Point()
            point.x = display.width
            point.y = display.height
            columnWidth = point.x
            return columnWidth
        }

    fun share(activity: Activity, link: String, type: String) {
        val contentUri = FileProvider.getUriForFile(
            activity, BuildConfig.APPLICATION_ID + ".fileprovider", File(link)
        )
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        if (type == "image") {
            shareIntent.type = "image/*"
        } else {
            shareIntent.type = "video/*"
        }
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        shareIntent.clipData = ClipData.newRawUri("", contentUri)

        shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        shareIntent.putExtra(
            Intent.EXTRA_TEXT, context.resources.getString(R.string.play_more_app)
        )
        activity.startActivity(
            Intent.createChooser(
                shareIntent, activity.resources.getString(R.string.share_to)
            )
        )
    }


    /**
     * This function used to implement interstitial ad easily
     */
    fun onClick(
        activity: Activity,
        position: Int,
        type: String,
        data: String,
        backResult: (position: Int, type: String, data: String) -> Unit
    ) {
        backResult.invoke(position, type, data)
    }

    /**
     * This function used to implement banner ad easily
     */
    fun bannerAd(constraintLayout: ConstraintLayout) {
        constraintLayout.gone()
    }

    fun alertBox(activity: Activity, message: String) {
        try {
            if (!activity.isFinishing) {
                val builder = MaterialAlertDialogBuilder(activity, R.style.DialogTitleTextStyle)
                builder.setMessage(message)
                builder.setPositiveButton(
                    context.resources.getString(R.string.ok)
                ) { _: DialogInterface?, _: Int -> }
                val alertDialog = builder.create()
                alertDialog.show()
            }
        } catch (e: Exception) {
            Log.d("error", e.toString())
        }
    }

    //Check dark mode or not
    private val isDarkMode: Boolean
        get() {
            return when (context.resources.configuration.uiMode - Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> // Night mode is not active, we're using the light theme
                    false
                Configuration.UI_MODE_NIGHT_YES -> // Night mode is active, we're using dark theme
                    true
                else -> false
            }
        }

    fun webViewText(): String {
        return if (isDarkMode) {
            Constant.webViewTextNight
        } else {
            Constant.webViewTextDay
        }
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

}