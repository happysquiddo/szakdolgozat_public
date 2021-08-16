package hu.scsaba.health.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.scsaba.health.model.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainState>(MainState.Loading)
    val uiState: StateFlow<MainState> = _uiState

    init {
        isLoggedIn()
    }

    fun isLoggedIn() {
        viewModelScope.launch {
            if (repository.isLoggedIn()) {
                _uiState.value = MainState.Success
            } else {
                _uiState.value = MainState.Failure
            }
        }
    }


    sealed class MainState {
        object Success : MainState()
        object Failure: MainState()
        object Loading: MainState()
    }
}