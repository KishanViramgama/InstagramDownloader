package com.app.instancedownload.util

import java.io.File

object Constant {

    var webViewTextDay = "#8b8b8b;"
    var webViewTextNight = "#FFFFFF;"

    var isDownload = true

    var imageArray: MutableList<File> = ArrayList()
    var videoArray: MutableList<File> = ArrayList()
    var downloadArray = ArrayList<String>()

    const val ACTION_SERVICE_START = "com.action.serviceStart"
    const val ACTION_SERVICE_STOP = "com.action.serviceStop"
    const val ACTION_START = "com.download.action.START"
    const val ACTION_STOP = "com.download.action.STOP"

}