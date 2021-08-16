package hu.scsaba.health.screens.loggedin.account.profile

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
class ProfileViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _user = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val user: StateFlow<ProfileState> = _user

    fun getUserData(uid : String) {
        viewModelScope.launch {
            when (val result = repository.getUserData(uid)) {
                is ResultWrapper.Success -> {
                    _user.value = ProfileState.Success(result.value!!.toObject(User::class.java)!!)
                }
                is ResultWrapper.Failure -> _user.value =
                    ProfileState.Failure(result.errorMessageToUser!!)
            }
        }
    }

    sealed class ProfileState{
        data class Success(val result : User) : ProfileState()
        data class Failure(val message : String): ProfileState()
        object Loading: ProfileState()
    }
}

