package hu.scsaba.health

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import dagger.hilt.android.HiltAndroidApp
import hu.scsaba.health.utils.helper.Constants.BREAK_NOTIFICATION_ID
import hu.scsaba.health.utils.helper.Constants.WATER_NOTIFICATION_ID

@HiltAndroidApp
class HealthApplication : Application(){

    companion object {
        lateinit var instance: HealthApplication private set
    }
    object Strings {
        fun get(@StringRes stringRes: Int): String {
            return instance.getString(stringRes)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createBreakNotificationChannel()
        instance = this
    }

    private fun createBreakNotificationChannel() {
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.break_notif_name)
            val descriptionText = getString(R.string.break_notif_desctiption)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(BREAK_NOTIFICATION_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}