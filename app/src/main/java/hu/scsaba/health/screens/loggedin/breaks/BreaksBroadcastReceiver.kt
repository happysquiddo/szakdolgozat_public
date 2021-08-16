package hu.scsaba.health.screens.loggedin.breaks

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import hu.scsaba.health.HealthApplication
import hu.scsaba.health.MainActivity
import hu.scsaba.health.R
import hu.scsaba.health.model.Repository
import hu.scsaba.health.utils.helper.Constants
import hu.scsaba.health.utils.helper.Constants.BREAK_INTENT_EXTRA
import hu.scsaba.health.utils.helper.Constants.BREAK_INTENT_EXTRA_BUNDLE
import hu.scsaba.health.utils.helper.Constants.BREAK_NOTIFICATION_ID
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class BreaksBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository : Repository
    private var broadcastJob = Job()
    private val scope = CoroutineScope(Dispatchers.IO + broadcastJob)

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let{
            val notificationManager : NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val breakState = intent!!.getBundleExtra(BREAK_INTENT_EXTRA).let { bundle ->
                bundle!!.getParcelable<BreakStates>(BREAK_INTENT_EXTRA_BUNDLE)
            }
            val notification = buildNotifications(it)
            when (breakState) {
                BreakStates.BREAK -> {
                    scope.launch {
                        val interval = this.async { repository.getBreakInterval().first() }
                        repository.setNextReminder(interval.await())
                    }

                    val randomAdvice = listOf(
                        HealthApplication.Strings.get(R.string.break_notif_break_1),
                        HealthApplication.Strings.get(R.string.break_notif_break_2),
                        HealthApplication.Strings.get(R.string.break_notif_break_3),
                        )[Random.nextInt(0,2)]

                    notification.setContentText(randomAdvice)
                }
                BreakStates.OFF -> {
                    notification.setContentText(HealthApplication.Strings.get(R.string.break_notif_stopped))
                    stopWorking()
                }
            }
            notificationManager.notify(BREAK_NOTIFICATION_ID.hashCode(),notification.build())
        }
    }

    private fun stopWorking(){
        scope.launch {
                repository.stopWorking()
            }.invokeOnCompletion {
                scope.cancel()
            }
    }

    private fun buildNotifications(context : Context) : NotificationCompat.Builder {
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, BREAK_NOTIFICATION_ID)
            .setSmallIcon(R.drawable.ic_menu_add)
            .setContentIntent(pendingIntent)
            .setOngoing(false)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setAutoCancel(true)
            .setUsesChronometer(true)

    }
}