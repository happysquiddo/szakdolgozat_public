package hu.scsaba.health.screens.loggedin.breaks

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.scsaba.health.model.Repository
import hu.scsaba.health.utils.helper.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class BreaksViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel(){

    private val _breakState = MutableStateFlow(false)
    val breakState: StateFlow<Boolean> = _breakState

    private val _timeLeftUntilBreak = MutableStateFlow(0L)
    val timeLeftUntilBreak: StateFlow<Long> = _timeLeftUntilBreak

    private val _durationInHours = MutableStateFlow(0)
    val durationInHours: StateFlow<Int> = _durationInHours

    private val _interval = MutableStateFlow(0)
    val interval: StateFlow<Int> = _interval

    private var startTime = 0L

    private var job: Job? = null

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                observeBreakState()
            }
        }
    }

    private suspend fun observeBreakState(){
        repository.observeBreakState().collect { state ->
            _breakState.value = state
            if(state)job = viewModelScope.launch {
                getBreakData()
                observeTimeLeftUntilBreak()
            }
        }
    }

    fun startWorking(durationInHours : Int, breakIntervalInMinutes : Int, startTime : Long){
        job?.cancel()
        job = viewModelScope.launch {
            repository.startWorking(durationInHours, breakIntervalInMinutes, startTime)
            getBreakData()
            observeTimeLeftUntilBreak()
        }
    }

    private suspend fun getBreakData(){
        _durationInHours.value = repository.getBreakDurationInHours().first()
        _interval.value = repository.getBreakInterval().first()
    }

    fun stopWorking(){
        job?.cancel()
        viewModelScope.launch {
            repository.stopWorking()
        }
    }

    private suspend fun observeTimeLeftUntilBreak(){
        startTime = withContext(Dispatchers.IO){
            repository.observeBreakStartTime().first()
        }

        _breakState.collect {
            while (_breakState.value){
                val currentTime = (SystemClock.elapsedRealtime() - startTime)
                val timeLeftInMillis = (_interval.value - (currentTime % _interval.value))
                _timeLeftUntilBreak.value = timeLeftInMillis

                delay(1000)
            }
        }
    }
}