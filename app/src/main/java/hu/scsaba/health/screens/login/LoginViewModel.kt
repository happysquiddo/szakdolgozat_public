package hu.scsaba.health.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.scsaba.health.model.Repository
import hu.scsaba.health.screens.signup.SignUpViewModel
import hu.scsaba.health.utils.helper.ResultWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginState>(LoginState.Loading)
    val uiState: StateFlow<LoginState> = _uiState

    fun login(
        email: String,
        password: String,
    ){
        viewModelScope.launch {
            repository.loginWithPassword(email, password).collect { response ->
                when (response) {
                    is ResultWrapper.Failure -> _uiState.value = LoginState.Failure(response.errorMessageToUser!!)
                    is ResultWrapper.Success<AuthResult> -> _uiState.value = LoginState.Success
                }
            }
        }
    }
    fun resetState(){
        _uiState.value = LoginState.Loading
    }

    sealed class LoginState {
        object Success : LoginState()
        data class Failure(val message : String): LoginState()
        object Loading: LoginState()
    }

}