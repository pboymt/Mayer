package icu.pboymt.mayer

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import icu.pboymt.mayer.ui.MainActivity

class MayerMonitorService : Service() {


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PING -> broadcastPong()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("MayerMonitorService", "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 创建前台通知
        createNotificationChannel()
        val notification = createNotification()
        startForeground(1, notification)
        // 注册广播接收器
        val filter = IntentFilter()
        filter.addAction(ACTION_PING)
        filter.addAction(ACTION_TOGGLE_OVERLAY)
        filter.addAction(ACTION_UPDATE_OVERLAY)
        registerReceiver(receiver, filter)
        broadcastPong()
        instance = this
        Log.d("MayerMonitorService", "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MayerMonitorService", "onDestroy")
//        hideFloatingWindow()
        unregisterReceiver(receiver)
        broadcastPong(false)
        instance = null
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        val chan = NotificationChannel(
            DEFAULT_CHANNEL_ID,
            "Mayer Monitor Service", NotificationManager.IMPORTANCE_DEFAULT
        )
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        chan.description = "Mayer Monitor Service"
        chan.enableLights(false)
        val service = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
    }

    /**
     * 创建通知
     */
    private fun createNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
            .setOngoing(true)
            .setContentTitle("Mayer Monitor Service")
            .setContentText("Mayer Monitor Service is running")
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentIntent)
            .setChannelId(DEFAULT_CHANNEL_ID)
            .setVibrate(longArrayOf(0))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }


    fun broadcastPong(running: Boolean = true) {
        Log.d("MayerMonitorService", "send broadcastPong: $running")
        val result = Intent(ACTION_PONG)
        result.putExtra(EXTRA_MAYER_MONITOR_RUNNING, running)
        sendBroadcast(result)
    }

    companion object {
        const val DEFAULT_CHANNEL_ID = "icu.pboymt.mayer.MayerMonitorService"
        const val ACTION_PING = "icu.pboymt.mayer.ACTION_PING"
        const val ACTION_PONG = "icu.pboymt.mayer.ACTION_PONG"
        const val ACTION_TOGGLE_OVERLAY = "icu.pboymt.mayer.ACTION_TOGGLE_OVERLAY"
        const val ACTION_UPDATE_OVERLAY = "icu.pboymt.mayer.ACTION_UPDATE_OVERLAY"
        const val EXTRA_MAYER_MONITOR_RUNNING = "icu.pboymt.mayer.MAYER_MONITOR_RUNNING"

        // a instance of MayerMonitorService
        var instance: MayerMonitorService? = null

    }


}
