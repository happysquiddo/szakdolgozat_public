package hu.scsaba.health.screens.loggedin.account.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.scsaba.health.model.Repository
import hu.scsaba.health.model.entities.progress.ProgressEntity
import hu.scsaba.health.utils.helper.ResultWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel(){

    private val _progressState = MutableStateFlow<ProgressState>(ProgressState.Loading)
    val progressState: StateFlow<ProgressState> = _progressState

    fun getProgress(uid : String){
        viewModelScope.launch {
            repository.getProgress(uid).collect { result ->
                when(result){
                    is ResultWrapper.Success -> {
                        if(result.value!!.isEmpty) {
                            _progressState.value = ProgressState.Success(listOf())
                        }else{
                            _progressState.value = ProgressState.Success(result.value.toObjects(ProgressEntity::class.java))
                        }
                    }
                    is ResultWrapper.Failure -> _progressState.value = ProgressState.Failure(result.errorMessageToUser!!)
                }
            }
        }
    }

    sealed class ProgressState {
        data class Success(val result : List<ProgressEntity>) : ProgressState()
        data class Failure(val message : String): ProgressState()
        object Loading: ProgressState()
    }
}