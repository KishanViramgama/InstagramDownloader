package com.app.instancedownload.service

import android.app.Service
import android.content.ClipboardManager
import android.content.ClipboardManager.OnPrimaryClipChangedListener
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.app.instancedownload.R
import com.app.instancedownload.util.Constant
import com.app.instancedownload.util.Constant.ACTION_START
import com.app.instancedownload.util.FindData

class GetAppService : Service() {

    private lateinit var findData: FindData
    private lateinit var mClipboardManager: ClipboardManager

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        mClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener)
        findData = FindData(
            applicationContext
        )

    }

    private var mOnPrimaryClipChangedListener = OnPrimaryClipChangedListener {
        try {
            val clip = mClipboardManager.primaryClip!!
            val string = clip.getItemAt(0).text.toString()
            findData.data(string) { linkList, message, isData ->
                if (isData) {
                    if (linkList.size != 0) {
                        Constant.downloadArray.clear()
                        Constant.downloadArray.addAll(linkList)
                        val intent = Intent(applicationContext, DownloadService::class.java)
                        intent.action = ACTION_START
                        startService(intent)
                    } else {
                        Toast.makeText(
                            this@GetAppService,
                            resources.getString(R.string.no_data_found),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@GetAppService, message, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this@GetAppService, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        stopSelf()
        stopForeground(false)
        mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener)
        super.onDestroy()
    }
}