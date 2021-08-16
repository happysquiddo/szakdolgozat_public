package hu.scsaba.health.screens.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.scsaba.health.model.Repository
import hu.scsaba.health.utils.helper.ResultWrapper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {


    private val _uiState = MutableStateFlow<SignUpState>(SignUpState.Loading)
    val uiState: StateFlow<SignUpState> = _uiState

    fun signUp(
        email: String,
        password: String,
        username: String,
    ){
        viewModelScope.launch {
            repository.signUpWithPassword(email, password, username).collect { response ->
                when (response) {
                    is ResultWrapper.Failure -> _uiState.value = SignUpState.Failure(response.errorMessageToUser!!)
                    is ResultWrapper.Success<AuthResult> -> _uiState.value = SignUpState.Success
                }
                _uiState.value = SignUpState.Loading
            }
        }
    }

    sealed class SignUpState {
        object Success : SignUpState()
        data class Failure(val message : String): SignUpState()
        object Loading: SignUpState()
    }
}