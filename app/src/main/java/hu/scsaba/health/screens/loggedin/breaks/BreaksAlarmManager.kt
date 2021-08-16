package hu.scsaba.health.screens.loggedin.breaks

import android.app.AlarmManager
import android.app.PendingIntent.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import hu.scsaba.health.utils.helper.Constants.BREAK_INTENT_EXTRA
import hu.scsaba.health.utils.helper.Constants.BREAK_INTENT_EXTRA_BUNDLE

class BreaksAlarmManager constructor(
    private val context : Context
){

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val breakIntent = Intent(context, BreaksBroadcastReceiver::class.java).let { intent ->
        val bundle : Bundle = Bundle().apply {
            putParcelable(BREAK_INTENT_EXTRA_BUNDLE,BreakStates.BREAK)
        }
        intent.putExtra(BREAK_INTENT_EXTRA,bundle)
        return@let getBroadcast(context, 1, intent, FLAG_IMMUTABLE)
    }
    private val offIntent = Intent(context, BreaksBroadcastReceiver::class.java).let { intent ->
        val bundle : Bundle = Bundle().apply {
            putParcelable(BREAK_INTENT_EXTRA_BUNDLE,BreakStates.OFF)
        }
        intent.putExtra(BREAK_INTENT_EXTRA,bundle)
        return@let getBroadcast(context, 0, intent, FLAG_IMMUTABLE)
    }

    fun setNextReminder(interval : Int){
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + interval.toLong(),
            breakIntent
        )
    }

    fun startWorking(durationInHours : Int, breakIntervalInMinutes : Int){
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + breakIntervalInMinutes.toLong() * 60 * 1000,
            breakIntent
        )
        alarmManager.setExact(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + durationInHours.toLong() * 60 * 60 * 1000,
            offIntent
        )
    }

    fun stopWorking(){
        alarmManager.cancel(breakIntent)
        alarmManager.cancel(offIntent)
    }

}