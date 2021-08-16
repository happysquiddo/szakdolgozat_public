package hu.scsaba.health.screens.loggedin.workouts.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.scsaba.health.model.Repository
import hu.scsaba.health.model.entities.workout.ExerciseEntity
import hu.scsaba.health.model.entities.workout.WorkoutEntity
import hu.scsaba.health.utils.helper.ResultWrapper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class DoWorkoutViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel(){

    private val _isStarted = MutableStateFlow(WorkoutProgressState.STOPPED)
    val isStarted: StateFlow<WorkoutProgressState> = _isStarted

    private val _breakState = MutableStateFlow(false)
    val breakState: StateFlow<Boolean> = _breakState

    private val _timeState = MutableStateFlow(0L)
    val timeState: StateFlow<Long> = _timeState

    private val _totalRounds = MutableStateFlow(0)
    val totalRounds: StateFlow<Int> = _totalRounds

    private val _remainingRounds = MutableStateFlow(0)
    val remainingRounds: StateFlow<Int> = _remainingRounds

    private val _exercisesDoneIndex = MutableStateFlow<Int>(0)
    val exercisesDoneIndex: StateFlow<Int> = _exercisesDoneIndex

    private val _exerciseListState = MutableStateFlow<DoWorkoutState>(DoWorkoutState.Loading)
    val exerciseListState: StateFlow<DoWorkoutState> = _exerciseListState

    private val _saveProgressState = MutableStateFlow(SaveProgressState.LOADING)
    val saveProgressState: StateFlow<SaveProgressState> = _saveProgressState

    private var durationBetweenExercises : Int = 0
    private var durationBetweenRounds : Int = 0

    private var timerJob: Job? = null
    private var workoutJob: Job? = null

    fun getWorkout(workoutName : String, uid : String){
        viewModelScope.launch {
            when(val result = repository.getWorkout(workoutName, uid).first()){
                is ResultWrapper.Success -> {
                    val workoutEntity : WorkoutEntity = result.value!!.toObject(WorkoutEntity::class.java)!!
                    val initTime = workoutEntity.exerciseList!![0].durationMinutes*60*1000+
                            workoutEntity.exerciseList[0].durationSeconds*1000

                    _exerciseListState.value = DoWorkoutState.Success(workoutEntity.exerciseList)
                    _totalRounds.value = workoutEntity.rounds
                    _remainingRounds.value = totalRounds.value
                    _timeState.value = initTime.toLong()
                    durationBetweenExercises = workoutEntity.durationBetweenExercises
                    durationBetweenRounds = workoutEntity.durationBetweenRounds
                }
                is ResultWrapper.Failure -> DoWorkoutState.Failure("")
            }
        }
    }

    private suspend fun saveWorkoutProgress(){
        when(repository.saveWorkoutProgress()){
            is ResultWrapper.Success ->
                _saveProgressState.value = SaveProgressState.SUCCESS

            is ResultWrapper.Failure ->
                _saveProgressState.value = SaveProgressState.FAILURE
        }
    }

    private fun startTimer(time: Long) {
        stop(time)
        timerJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                if (timeState.value <= 0L) {
                    timerJob?.cancel()
                    return@launch
                }
                _timeState.value = (timeState.value - 1000).coerceAtLeast(0L)
                delay(1000)
            }
        }
    }

    fun startWorkout(){
        /**
         * Előfordulhat, hogy ez nem az első, hanem egy újraindítása
         * a számlálónak. Ilyenkor ugyanonnan kell folytatni a gyakorlatokat és a számlálót
         * is.
         * A további köröknél azonban a megszokott módon kell lekérdezni a gyakorlat hosszát,
         * így ezt az adott kör végén visszaállítjuk
         * */
        var justResumed = true
        _isStarted.value = WorkoutProgressState.STARTED
        workoutJob = viewModelScope.launch(Dispatchers.IO) {

            for (i in totalRounds.value - remainingRounds.value until totalRounds.value) {

                val remainingExercisesFromCurrent =
                    (exerciseListState.value as DoWorkoutState.Success).result.size - exercisesDoneIndex.value

                (exerciseListState.value as DoWorkoutState.Success).result
                    .takeLast(remainingExercisesFromCurrent).forEachIndexed  { index, exercise ->
                        if(justResumed) {
                            startTimer(timeState.value)
                        }
                        else {
                            startTimer((exercise.durationMinutes.toLong() * 60 * 1000) +
                                    (exercise.durationSeconds.toLong() * 1000))
                        }
                        timerJob?.join()

                        //körön belüli utolsó gyakorlatnál nem szükséges a gyakorlatok közti szünet
                        if(index < remainingExercisesFromCurrent-1 && !breakState.value){
                            _breakState.value = true
                            startTimer(durationBetweenExercises.toLong() * 1000)
                            timerJob?.join()
                        }
                        _breakState.value = false
                        justResumed = false
                        if(!breakState.value) _exercisesDoneIndex.value = _exercisesDoneIndex.value + 1
                    }

                _exercisesDoneIndex.value = 0
                _remainingRounds.value = _remainingRounds.value - 1

                //Utolsó kör után nem szükséges a szünet
                if(i != totalRounds.value-1&& !breakState.value){
                    startTimer(durationBetweenRounds.toLong() * 1000)
                    _breakState.value = true
                    timerJob?.join()
                }
                _breakState.value = false

            }
            _isStarted.value = WorkoutProgressState.FINISHED
            saveWorkoutProgress()
            workoutJob?.cancel()
            timerJob?.cancel()
        }
    }
    private fun setupInitTime(time: Long) {
        _timeState.value = time
    }
    private fun stop(time: Long) {
        timerJob?.cancel()
        setupInitTime(time)
    }
    fun pause(){
        workoutJob?.cancel()
        timerJob?.cancel()
        _isStarted.value = WorkoutProgressState.STOPPED
    }

    sealed class DoWorkoutState {
        data class Success(val result : List<ExerciseEntity>) : DoWorkoutState()
        data class Failure(val message : String): DoWorkoutState()
        object Loading: DoWorkoutState()
    }
    enum class SaveProgressState{
        LOADING, SUCCESS, FAILURE
    }
    enum class WorkoutProgressState{
        STOPPED,STARTED,FINISHED
    }
}


