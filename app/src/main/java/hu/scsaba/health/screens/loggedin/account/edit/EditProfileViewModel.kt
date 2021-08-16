package hu.scsaba.health.screens.loggedin.account.edit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.scsaba.health.model.Repository
import hu.scsaba.health.model.entities.user.User
import hu.scsaba.health.utils.helper.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel(){

    private val _user = MutableStateFlow<EditProfileState>(EditProfileState.Loading)
    val user: StateFlow<EditProfileState> = _user

    private val _saveState = MutableStateFlow<EditProfileSaveState>(EditProfileSaveState.Idle)
    val saveState: StateFlow<EditProfileSaveState> = _saveState

    init {
        getUser()
    }

    private fun getUser(){
        viewModelScope.launch {
            when(val result = repository.getUserData()){
                is ResultWrapper.Success -> {
                    _user.value = EditProfileState.Success(result.value!!.toObject(User::class.java)!!)
                }
                is ResultWrapper.Failure -> _user.value = EditProfileState.Failure(result.errorMessageToUser!!)
            }
        }
    }
    fun save(weight : Int, height : Int, age : Int){
        _saveState.value = EditProfileSaveState.Loading
        viewModelScope.launch {
            delay(500)
            when(val result = repository.editUserData(weight, height, age)){
                is ResultWrapper.Success -> _saveState.value = EditProfileSaveState.Success
                is ResultWrapper.Failure -> _saveState.value = EditProfileSaveState.Failure(result.errorMessageToUser!!)
            }
        }
    }

    sealed class EditProfileState {
        data class Success(val result : User) : EditProfileState()
        data class Failure(val message : String): EditProfileState()
        object Loading: EditProfileState()
    }
    sealed class EditProfileSaveState {
        object Success : EditProfileSaveState()
        data class Failure(val message : String): EditProfileSaveState()
        object Loading: EditProfileSaveState()
        object Idle: EditProfileSaveState()
    }

}