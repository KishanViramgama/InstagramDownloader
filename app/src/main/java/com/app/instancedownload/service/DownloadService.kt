package com.app.instancedownload.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.app.instancedownload.BuildConfig
import com.app.instancedownload.R
import com.app.instancedownload.util.Constant
import com.app.instancedownload.util.Constant.ACTION_START
import com.app.instancedownload.util.Constant.ACTION_STOP
import com.app.instancedownload.util.Constant.isDownload
import com.app.instancedownload.util.LiveDataType
import com.app.instancedownload.util.Type
import dagger.hilt.android.AndroidEntryPoint
import io.github.lizhangqu.coreprogress.ProgressHelper
import io.github.lizhangqu.coreprogress.ProgressUIListener
import okhttp3.*
import okio.Okio
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class DownloadService : Service() {

    private var position = 0
    private val chanelId = 105
    private lateinit var client: OkHttpClient
    private lateinit var remoteViews: RemoteViews
    private lateinit var notificationChanelId: String
    private lateinit var builder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var mutableLiveData: MutableLiveData<LiveDataType<String>>

    @Inject
    lateinit var mutableList: MutableLiveData<LiveDataType<String>>

    companion object {
        private const val CANCEL_TAG = "c_tag"
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        notificationChanelId = resources.getString(R.string.notificationChanelDownload)

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        builder = NotificationCompat.Builder(this, notificationChanelId)
        builder.setChannelId(notificationChanelId)
        builder.setSmallIcon(R.drawable.ic_stat_ic_notification)
        builder.setTicker(resources.getString(R.string.downloading))
        builder.setWhen(System.currentTimeMillis())
        builder.setOnlyAlertOnce(true)

        remoteViews = RemoteViews(packageName, R.layout.my_custom_notification)
        remoteViews.setTextViewText(R.id.nf_title, getString(R.string.app_name))
        remoteViews.setProgressBar(R.id.progress, 100, 0, false)
        remoteViews.setTextViewText(
            R.id.nf_percentage,
            resources.getString(R.string.downloading) + " " + "(0%)"
        )

        val intentClose = Intent(this, DownloadService::class.java)
        intentClose.action = ACTION_STOP
        val intentFlagType: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE // or only use FLAG_MUTABLE >> if it needs to be used with inline replies or bubbles.
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val closeIntent = PendingIntent.getService(this, 0, intentClose, intentFlagType)
        remoteViews.setOnClickPendingIntent(R.id.nf_close, closeIntent)
        builder.setCustomContentView(remoteViews)

        val mChannel: NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence =
                resources.getString(R.string.app_name) // The user-visible name of the channel.
            mChannel = NotificationChannel(
                notificationChanelId,
                name,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(mChannel)
        }

        startForeground(chanelId, builder.build())

    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(false)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        try {
            if (intent.action != null && intent.action == ACTION_START) {
                isDownload = false
                init(Constant.downloadArray[position])
            }
            if (intent.action != null && intent.action == ACTION_STOP) {
                for (call in client.dispatcher().runningCalls()) {
                    if (call.request().tag() == CANCEL_TAG) call.cancel()
                }
                isDownload = true
                stopForeground(false)
                stopSelf()
            }
        } catch (e: Exception) {
            Log.d("error", e.toString())
            stopForeground(false)
            stopSelf()
        }
        return START_STICKY
    }

    private fun init(downloadUrl: String) {
        val iconsStoragePath =
            applicationContext.getExternalFilesDir(BuildConfig.downloadUrl).toString()

        //Pattern for showing milliseconds in the time "SSS"
        @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss:SSS")

        //Using Calendar class
        val cal = Calendar.getInstance()
        val s = sdf.format(cal.time)
        val string: String = if (downloadUrl.contains(".jpg") || downloadUrl.contains(".webp")) {
            "Image-$s.jpg"
        } else {
            "Image-$s.mp4"
        }
        Log.d("file_name", string)
        Thread {
            client = OkHttpClient()
            val builder = Request.Builder()
                .url(downloadUrl)
                .addHeader("Accept-Encoding", "identity")
                .get()
                .tag(CANCEL_TAG)
            val call = client.newCall(builder.build())
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("TAG", "=============onFailure===============")
                    e.printStackTrace()
                    Log.d("error_downloading", e.toString())
                    // Method.isDownload = true;
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    Log.d("TAG", "=============onResponse===============")
                    Log.d("TAG", "request headers:" + response.request().headers())
                    Log.d("TAG", "response headers:" + response.headers())
                    assert(response.body() != null)
                    val responseBody =
                        ProgressHelper.withProgress(response.body(), object : ProgressUIListener() {
                            //if you don't need this method, don't override this method. It isn't an abstract method, just an empty method.
                            override fun onUIProgressStart(totalBytes: Long) {
                                super.onUIProgressStart(totalBytes)
                                Log.d("TAG", "onUIProgressStart:$totalBytes")
                            }

                            override fun onUIProgressChanged(
                                numBytes: Long,
                                totalBytes: Long,
                                percent: Float,
                                speed: Float
                            ) {
                                updateNotification(1, (100 * percent).toInt())
                            }

                            //if you don't need this method, don't override this method. It isn't an abstract method, just an empty method.
                            override fun onUIProgressFinish() {
                                super.onUIProgressFinish()
                                if (string.contains(".jpg")) {
                                    Constant.imageArray.add(
                                        0, File(
                                            "$iconsStoragePath/$string"
                                        )
                                    )
                                } else {
                                    Constant.videoArray.add(
                                        0, File(
                                            "$iconsStoragePath/$string"
                                        )
                                    )
                                }
                                updateNotification(2, 0)
                            }
                        })
                    try {
                        val source = responseBody.source()
                        val outFile = File("$iconsStoragePath/$string")
                        val sink = Okio.buffer(Okio.sink(outFile))
                        source.readAll(sink)
                        sink.flush()
                        source.close()
                    } catch (e: Exception) {
                        Log.d("show_data", e.toString())
                    }
                }
            })
        }.start()
    }

    private fun updateNotification(type: Int, progress: Int) {

        if (type == 1) {
            remoteViews.setTextViewText(R.id.nf_title, getString(R.string.app_name))
            remoteViews.setProgressBar(R.id.progress, 100, progress, false)
            remoteViews.setTextViewText(
                R.id.nf_percentage,
                resources.getString(R.string.downloading) + " " + "(" + progress + " %)"
            )
            notificationManager.notify(chanelId, builder.build())
        } else {
            if (Constant.downloadArray.size - 1 != position) {
                position++
                init(Constant.downloadArray[position])
            } else {
                Toast.makeText(
                    applicationContext,
                    resources.getString(R.string.downloading),
                    Toast.LENGTH_SHORT
                ).show()
                position = 0
                stopForeground(false)
                stopSelf()
                mutableLiveData.value = LiveDataType.callObserver(Type.ADAPTER, 0, "")
                isDownload = true
            }
        }
    }


}