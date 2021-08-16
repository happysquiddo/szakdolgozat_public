package hu.scsaba.health.screens.loggedin.workouts.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.scsaba.health.model.Repository
import hu.scsaba.health.utils.helper.ResultWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import hu.scsaba.health.model.entities.workout.WorkoutEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@HiltViewModel
class WorkoutListViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _workoutList = MutableStateFlow<WorkoutListState>(WorkoutListState.Loading)
    val workoutList: StateFlow<WorkoutListState> = _workoutList

    private val _uid = MutableStateFlow<String>("")
    val uid: StateFlow<String> = _uid

    private val _workoutShareState = MutableStateFlow<WorkoutShareState>(WorkoutShareState.IDLE)
    val workoutShareState: StateFlow<WorkoutShareState> = _workoutShareState

    init {
        getCurrentUserId()
        getWorkoutList()
    }

    fun postWorkout(workoutEntity: WorkoutEntity, postText : String){
        viewModelScope.launch {
            _workoutShareState.value = WorkoutShareState.LOADING
            withContext(Dispatchers.IO){
                when(repository.postWorkout(workoutEntity, postText)){
                    is ResultWrapper.Success -> _workoutShareState.value = WorkoutShareState.SUCCESS
                    is ResultWrapper.Failure -> _workoutShareState.value = WorkoutShareState.FAILURE
                }
            }
        }
    }

    private fun getWorkoutList(){
        viewModelScope.launch {
            when(val result = repository.getWorkoutList().first()){
                is ResultWrapper.Success -> {
                    if (!result.value!!.isEmpty) {
                        val tempList = mutableListOf<WorkoutEntity>()
                        for (snapshot in result.value.documents) tempList.add(
                            snapshot.toObject(WorkoutEntity::class.java)!!
                        )
                        _workoutList.value = WorkoutListState.Success(tempList)
                    }else{ _workoutList.value = WorkoutListState.Success(listOf())}
                }
                is ResultWrapper.Failure -> _workoutList.value = WorkoutListState.Failure("")
            }
        }
    }

    private fun getCurrentUserId(){
        viewModelScope.launch {
            _uid.value = repository.getCurrentUserId()
        }
    }
    enum class WorkoutShareState {
        LOADING, SUCCESS, FAILURE, IDLE
    }
    sealed class WorkoutListState {
        data class Success(val result : List<WorkoutEntity>) : WorkoutListState()
        data class Failure(val message : String): WorkoutListState()
        object Loading: WorkoutListState()
    }
}

