package hu.scsaba.health.screens.loggedin.water

import hu.scsaba.health.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.graphics.alpha
import dagger.hilt.android.AndroidEntryPoint
import hu.scsaba.health.MainActivity
import hu.scsaba.health.model.Repository
import hu.scsaba.health.utils.helper.Constants.DRINK_ADDED_BROADCAST_ACTION
import hu.scsaba.health.utils.helper.Constants.WATER_NOTIFICATION_ID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class WaterForegroundService: Service() {

    @Inject
    lateinit var repository : Repository
    @Inject
    lateinit var notificationManager : NotificationManager
    private val broadcastReceiver : WaterBroadcastReceiver = WaterBroadcastReceiver()
    private var broadcastJob = Job()
    private val scope = CoroutineScope(Dispatchers.IO + broadcastJob)

    override fun onCreate() {
        super.onCreate()
        val intentFilter = IntentFilter().also{
            it.addAction(DRINK_ADDED_BROADCAST_ACTION)
        }
        this.registerReceiver(broadcastReceiver, intentFilter)
    }

    @ExperimentalCoroutinesApi
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val incrementIntent = Intent(this, WaterBroadcastReceiver::class.java).apply {
            action = DRINK_ADDED_BROADCAST_ACTION
        }
        val incrementPendingIntent: PendingIntent =
            PendingIntent.getBroadcast(this, 0, incrementIntent, 0)

        val notification = NotificationCompat.Builder(this, WATER_NOTIFICATION_ID)
            .setContentTitle(getString(R.string.drinks_today))
            .setContentText("0 ${getString(R.string.glasses_of_water)}")
            .setSmallIcon(R.drawable.ic_menu_add)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(R.drawable.ic_menu_add, getString(R.string.add_water),
                incrementPendingIntent)
            .setColor(android.graphics.Color.parseColor("#008CC9"))
            .setOnlyAlertOnce(true)
            .setColorized(true)
        startForeground(WATER_NOTIFICATION_ID.hashCode(), notification.build())

        scope.launch {
            repository.observeWaterIntake().collect {
                notification.setContentText(it.count.toString() + " " + getString(R.string.glasses_of_water))
                notificationManager.notify(WATER_NOTIFICATION_ID.hashCode(), notification.build())
            }
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.water_notif_name)
            val descriptionText = getString(R.string.water_notif_desctiption)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(WATER_NOTIFICATION_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        this.unregisterReceiver(broadcastReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}