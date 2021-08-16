package hu.scsaba.health.screens.loggedin.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.scsaba.health.model.Repository
import hu.scsaba.health.model.entities.post.Comment
import hu.scsaba.health.model.entities.post.Post
import hu.scsaba.health.utils.helper.ResultWrapper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _openComments = MutableStateFlow<CommentState>(CommentState.Loading)
    val openComments: StateFlow<CommentState> = _openComments

    val lazyPosts = Pager(PagingConfig(pageSize = 2, initialLoadSize = 2)) {
        FeedPager(repository)
    }.flow

    private var openCommentsJob : Job = Job()

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

    sealed class FeedState {
        data class Success(val result : MutableList<Post>) : FeedState()
        data class Failure(val message : String): FeedState()
        object Loading: FeedState()
    }
}
sealed class CommentState {
    data class Success(val result : MutableList<Comment>) : CommentState()
    data class Failure(val message : String): CommentState()
    object Loading: CommentState()
}
