package hu.scsaba.health.screens.loggedin.account.userposts

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.paging.compose.collectAsLazyPagingItems
import hu.scsaba.health.navigation.navArgs
import hu.scsaba.health.screens.loggedin.feed.CommentState
import hu.scsaba.health.screens.loggedin.feed.PostList
import hu.scsaba.health.ui.composables.Loading
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@Composable
fun UserPosts(backStackEntry: NavBackStackEntry, uid : String, navigate : navArgs){

    val myViewModel: UserPostsViewModel = viewModel("userposts",
        HiltViewModelFactory(LocalContext.current,backStackEntry)
    )

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val userPostsState by myViewModel.userPostsState.asLiveData().observeAsState(UserPostsViewModel.UserPostsState.Loading)
    val openComments by myViewModel.openComments.asLiveData().observeAsState(initial = CommentState.Loading)

    DisposableEffect(Unit) {
        myViewModel.getPosts(uid)
        onDispose {  }
    }

    when(userPostsState){
        is UserPostsViewModel.UserPostsState.Loading -> Loading()
        is UserPostsViewModel.UserPostsState.Ready -> {
            PostList(
                lazyListState = lazyListState,
                coroutineScope = coroutineScope,
                navigate = navigate,
                lazyItems = (userPostsState as UserPostsViewModel.UserPostsState.Ready).result.collectAsLazyPagingItems(),
                openComments = openComments,
                observeCurrentlyOpenCommentSection = { postId ->
                    myViewModel.observeCurrentlyOpenCommentSection(postId)
                },
                postComment = { text, postAuthorId, postId ->
                    myViewModel.postComment(text, postAuthorId, postId)
                }
            )
        }
    }

}