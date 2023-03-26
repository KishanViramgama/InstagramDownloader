package com.app.instancedownload.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.app.instancedownload.R
import com.app.instancedownload.util.Constant.ACTION_SERVICE_START
import com.app.instancedownload.util.Constant.ACTION_SERVICE_STOP
import com.app.instancedownload.util.LiveDataType
import com.app.instancedownload.util.Type
import javax.inject.Inject

class ActiveService : Service() {

    private val chanelId = 104
    private lateinit var remoteViews: RemoteViews
    private lateinit var notificationChanelId :String
    private lateinit var builder: NotificationCompat.Builder
    private lateinit var mNotificationManager: NotificationManager

    @Inject
    lateinit var mutableLiveData: MutableLiveData<LiveDataType<String>>

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        notificationChanelId = resources.getString(R.string.notificationChanelAction)

        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        builder = NotificationCompat.Builder(this, notificationChanelId)
        builder.setChannelId(notificationChanelId)
        builder.setSmallIcon(R.drawable.ic_stat_ic_notification)
        builder.setTicker(resources.getString(R.string.downloading))
        builder.setWhen(System.currentTimeMillis())
        builder.setOnlyAlertOnce(true)

        remoteViews = RemoteViews(packageName, R.layout.service_start_notification)

        val intentStop = Intent(this, ActiveService::class.java)
        intentStop.action = ACTION_SERVICE_STOP
        val intentFlagType: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE // or only use FLAG_MUTABLE >> if it needs to be used with inline replies or bubbles.
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val closeIntent = PendingIntent.getService(this, 0, intentStop, intentFlagType)
        remoteViews.setOnClickPendingIntent(R.id.button_stop, closeIntent)
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
            mNotificationManager.createNotificationChannel(mChannel)
        }

        startForeground(chanelId, builder.build())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        try {

            if (intent.action != null && intent.action == ACTION_SERVICE_START) {
                val intentServiceStart = Intent(applicationContext, GetAppService::class.java)
                startService(intentServiceStart)
            }

            if (intent.action != null && intent.action == ACTION_SERVICE_STOP) {
                stopForeground(false)
                stopSelf()
                mutableLiveData.value = LiveDataType.callObserver(Type.SERVICE, 0, "")
            }

        } catch (e: Exception) {
            stopForeground(false)
            stopSelf()
        }

        return START_STICKY
    }

}