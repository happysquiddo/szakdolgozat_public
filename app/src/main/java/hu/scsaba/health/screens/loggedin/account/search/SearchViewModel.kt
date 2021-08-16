package hu.scsaba.health.screens.loggedin.account.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.toObjects
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.scsaba.health.model.Repository
import hu.scsaba.health.model.entities.user.User
import hu.scsaba.health.screens.loggedin.account.AccountViewModel
import hu.scsaba.health.screens.loggedin.feed.CommentState
import hu.scsaba.health.utils.helper.ResultWrapper
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel(){

    private val _searchState = MutableStateFlow<SearchUserState>(SearchUserState.Idle)
    val searchState: StateFlow<SearchUserState> = _searchState

    private var searchJob : Job? = Job()

    fun searchUsers(searchTerm : String){
        _searchState.value = SearchUserState.Loading
        searchJob?.cancel()
        searchJob = viewModelScope.launch{
            delay(200)
            repository.searchUsers(searchTerm).collect { result ->
                when(result){
                    is ResultWrapper.Success -> {
                        _searchState.value = result.value?.let {
                            SearchUserState.Success(it.toObjects(User::class.java))
                        }!!
                    }
                    is ResultWrapper.Failure -> _searchState.value =
                        SearchUserState.Failure(result.errorMessageToUser!!)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }

    fun setIdleState(){
        _searchState.value = SearchUserState.Idle
        searchJob?.cancel()
    }

    sealed class SearchUserState {
        data class Success(val result : List<User>) : SearchUserState()
        data class Failure(val message : String): SearchUserState()
        object Loading: SearchUserState()
        object Idle : SearchUserState()
    }
}