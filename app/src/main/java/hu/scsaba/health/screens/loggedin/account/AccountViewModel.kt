package hu.scsaba.health.screens.loggedin.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.scsaba.health.model.Repository
import hu.scsaba.health.model.entities.user.User
import hu.scsaba.health.utils.helper.ResultWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel(){

    private val _logOutState = MutableStateFlow<LogoutState>(LogoutState.Loading)
    val logOutState: StateFlow<LogoutState> = _logOutState

    private val _user = MutableStateFlow<UserState>(UserState.Loading)
    val user: StateFlow<UserState> = _user

    fun logOut(){
        _logOutState.value = LogoutState.Loading
        viewModelScope.launch {
            try {
                repository.logOut()
                _logOutState.value = LogoutState.Success
            }catch (t : Throwable){
                _logOutState.value = LogoutState.Failure
            }
        }
    }

    fun getUserData(){
        viewModelScope.launch {
            when(val result = repository.getUserData()){
                is ResultWrapper.Success -> {
                    _user.value = UserState.Success(result.value!!.toObject(User::class.java)!!)
                }
                is ResultWrapper.Failure -> _user.value = UserState.Failure(result.errorMessageToUser!!)
            }
        }
    }

    sealed class LogoutState {
        object Success : LogoutState()
        object Failure: LogoutState()
        object Loading: LogoutState()
    }

    sealed class UserState{
        data class Success(val result : User) : UserState()
        data class Failure(val message : String): UserState()
        object Loading: UserState()
    }
}