package hu.scsaba.health.screens.loggedin.water

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.scsaba.health.model.Repository
import hu.scsaba.health.model.entities.water.WaterEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class WaterViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _waterIntake = MutableStateFlow<WaterState>(WaterState.Loading)
    val waterIntake: StateFlow<WaterState> = _waterIntake

    private val _waterIntakeHistory = MutableLiveData<MutableMap<String,Int>>(mutableMapOf())
    val waterIntakeHistory: LiveData<MutableMap<String,Int>> = _waterIntakeHistory

    private val _serviceState = MutableStateFlow(false)
    val serviceState: StateFlow<Boolean> = _serviceState

    init {
        observeWaterIntake()
        observeServiceState()
    }

    private fun observeServiceState(){
        viewModelScope.launch {
            repository.observeServiceState().collect { state ->
                when(state){
                    true -> _serviceState.value = true
                    false -> _serviceState.value = false
                }
            }
        }
    }

    fun startWaterForegroundService(){
        viewModelScope.launch {
            repository.startWaterForegroundService()
        }
    }
    fun stopWaterForegroundService(){
        viewModelScope.launch {
            repository.stopWaterForegroundService()
        }
    }

    fun incrementWaterCount(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.incrementWaterCount()
        }
    }

    fun decrementWaterCount(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.decrementWaterCount()
        }
    }


    private fun observeWaterIntake(){
        viewModelScope.launch {
            repository.observeWaterIntake().collect { water ->
                _waterIntake.value = WaterState.Success(water)
            }
        }
    }

    fun observeWaterIntakeHistory(){
        viewModelScope.launch {
            repository.observeWaterIntakeHistory().collect { result ->
                _waterIntakeHistory += result
            }
        }
    }


    sealed class WaterState{
        object Loading : WaterState()
        data class Success(val waterEntity : WaterEntity) : WaterState()
    }
}

operator fun MutableLiveData<MutableMap<String,Int>>.plusAssign(waterHistory: List<WaterEntity>) {
    val value = this.value ?: mutableMapOf()
    waterHistory.forEach {
        value[it.date] = it.count.toInt()
    }
    this.value = value
}