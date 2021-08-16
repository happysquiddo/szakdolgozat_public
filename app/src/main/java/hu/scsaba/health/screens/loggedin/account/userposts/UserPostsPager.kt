package hu.scsaba.health.screens.loggedin.account.userposts

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import hu.scsaba.health.model.Repository
import hu.scsaba.health.model.entities.post.Post
import hu.scsaba.health.model.entities.post.typeconverter.convertToPosts
import hu.scsaba.health.utils.exception.CustomException
import hu.scsaba.health.utils.helper.ResultWrapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first

class UserPostsPager(
    private val repository: Repository,
    private val uid : String
) : PagingSource<DocumentSnapshot, Post>() {

    @ExperimentalCoroutinesApi
    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, Post> {
        return try {
            var postList : QuerySnapshot? = null
            repository.getUserPosts(lastVisible = params.key, pageSize = params.loadSize, uid = uid).first().also { response ->
                when (response) {
                    is ResultWrapper.Failure -> throw CustomException(response.errorMessageToUser!!)
                    is ResultWrapper.Success -> {
                        postList = response.value
                    }
                }
            }

            val nextKey = if (postList == null){
                null
            } else if(params.loadSize > postList!!.size()) {
                null
            }else{
                postList!!.documents[postList!!.size() - 1]
            }
            LoadResult.Page(
                data = postList.convertToPosts(),
                prevKey = null,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, Post>): DocumentSnapshot? {
        return null
    }
}