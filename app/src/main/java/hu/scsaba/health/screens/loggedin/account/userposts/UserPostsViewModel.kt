package hu.scsaba.health.screens.loggedin.account.userposts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.scsaba.health.model.Repository
import hu.scsaba.health.model.entities.post.Comment
import hu.scsaba.health.model.entities.post.Post
import hu.scsaba.health.screens.loggedin.feed.CommentState
import hu.scsaba.health.screens.loggedin.feed.FeedPager
import hu.scsaba.health.screens.loggedin.feed.FeedViewModel
import hu.scsaba.health.utils.helper.ResultWrapper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class UserPostsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel(){

    private val _openComments = MutableStateFlow<CommentState>(CommentState.Loading)
    val openComments: StateFlow<CommentState> = _openComments

    private val _userPostsState = MutableStateFlow<UserPostsState>(UserPostsState.Loading)
    val userPostsState: StateFlow<UserPostsState> = _userPostsState

    private var openCommentsJob : Job = Job()

    fun getPosts(uid : String) {
        _userPostsState.value = UserPostsState.Ready(
            Pager(PagingConfig(pageSize = 3, initialLoadSize = 3)) {
                UserPostsPager(repository, uid)
            }.flow
        )
    }

    fun observeCurrentlyOpenCommentSection(postId: String){
        openCommentsJob.cancel()
        getOpenCommentSection(postId)
        openCommentsJob.invokeOnCompletion {
            _openComments.value = CommentState.Loading
        }
    }
    private fun getOpenCommentSection(postId: String){
        openCommentsJob = viewModelScope.launch(Dispatchers.IO) {
            repository.getOpenCommentSection(postId).collect{ result ->
                when(result){
                    is ResultWrapper.Success -> {
                        if(!result.value.isNullOrEmpty()){
                            _openComments.value = result.value.let { CommentState.Success(it) }
                        }else{
                            _openComments.value = result.value.let { CommentState.Success(mutableListOf()) }
                        }
                    }
                    is ResultWrapper.Failure -> _openComments.value = CommentState.Failure(result.errorMessageToUser ?: "❌")
                }
            }
        }
    }

    fun postComment(text : String, postAuthorId : String, postId : String){
        viewModelScope.launch(Dispatchers.IO) {
            val uid = async { repository.getCurrentUserId() }
            uid.await().let { repository.postComment(text,it,postAuthorId, postId) }
        }
    }

    sealed class UserPostsState {
        data class Ready(val result : Flow<PagingData<Post>>) : UserPostsState()
        object Loading: UserPostsState()
    }
}

