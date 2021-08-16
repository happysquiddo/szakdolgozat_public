package hu.scsaba.health.screens.loggedin.water

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import hu.scsaba.health.model.Repository
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class WaterBroadcastReceiver : BroadcastReceiver(){

    @Inject
    lateinit var repository : Repository
    private var broadcastJob = Job()
    private val scope = CoroutineScope(Dispatchers.IO + broadcastJob)

    override fun onReceive(context: Context?, intent: Intent?) {
        scope.launch {
            repository.incrementWaterCount()
        }.invokeOnCompletion {
            scope.cancel()
        }
    }
}