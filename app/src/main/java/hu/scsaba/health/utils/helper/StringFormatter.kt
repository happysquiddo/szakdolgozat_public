package hu.scsaba.health.utils.helper

import android.annotation.SuppressLint
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun formatTime(timeLeftUntilBreakInMillis: Long) : String {
    return String.format("%2d : %02d : %02d",
        TimeUnit.MILLISECONDS.toHours(timeLeftUntilBreakInMillis),
        TimeUnit.MILLISECONDS.toMinutes(timeLeftUntilBreakInMillis) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeLeftUntilBreakInMillis)),
        TimeUnit.MILLISECONDS.toSeconds(timeLeftUntilBreakInMillis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeLeftUntilBreakInMillis))
    )
}
@SuppressLint("SimpleDateFormat")
fun timestampToDateTime(timestamp: Timestamp): String {
    return SimpleDateFormat("yyyy.MM.dd HH:mm").format(Date(timestamp.toDate().time))
}