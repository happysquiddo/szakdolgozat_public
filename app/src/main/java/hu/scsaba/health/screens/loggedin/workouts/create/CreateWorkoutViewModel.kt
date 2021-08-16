package hu.scsaba.health.screens.loggedin.workouts.create

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.scsaba.health.model.Repository
import hu.scsaba.health.model.entities.workout.ExerciseEntity
import hu.scsaba.health.model.entities.workout.WorkoutEntity
import hu.scsaba.health.utils.helper.ResultWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CreateWorkoutViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val workoutName : MutableLiveData<String> = MutableLiveData("")

    private val _exerciseList = MutableStateFlow<SnapshotStateList<ExerciseEntity>>(mutableStateListOf())
    val exerciseList: StateFlow<SnapshotStateList<ExerciseEntity>> = _exerciseList

    private val _saveWorkoutState = MutableStateFlow<SaveWorkoutState>(SaveWorkoutState.Idle)
    val saveWorkoutState: StateFlow<SaveWorkoutState> = _saveWorkoutState

    suspend fun saveWorkout(workoutEntity: WorkoutEntity){
        _saveWorkoutState.value = SaveWorkoutState.Loading
           repository.saveWorkout(workoutEntity).run {
               when(this){
                   is ResultWrapper.Success -> _saveWorkoutState.value = SaveWorkoutState.Success
                   is ResultWrapper.Failure -> _saveWorkoutState.value = SaveWorkoutState.Failure(this.errorMessageToUser!!)
               }
           }
    }

    fun addExercise(exercise : ExerciseEntity){
        _exerciseList.value.add(exercise)
    }

    sealed class SaveWorkoutState {
        object Idle : SaveWorkoutState()
        object Success : SaveWorkoutState()
        data class Failure(val message : String): SaveWorkoutState()
        object Loading: SaveWorkoutState()
    }
}

