package hu.scsaba.health.screens.loggedin.feed

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.rounded.Comment
import androidx.compose.material.icons.rounded.Send
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.popUpTo
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import hu.scsaba.health.HealthApplication
import hu.scsaba.health.R
import hu.scsaba.health.model.entities.post.Comment
import hu.scsaba.health.model.entities.post.Post
import hu.scsaba.health.navigation.navArgs
import hu.scsaba.health.ui.composables.*
import hu.scsaba.health.ui.theme.HealthTheme
import hu.scsaba.health.utils.helper.Constants
import hu.scsaba.health.utils.helper.timestampToDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@Composable
fun Feed(backStackEntry: NavBackStackEntry, navigate: navArgs){

    val myViewModel: FeedViewModel = viewModel("feed",
        HiltViewModelFactory(LocalContext.current,backStackEntry)
    )
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val lazyItems: LazyPagingItems<Post> = myViewModel.lazyPosts.collectAsLazyPagingItems()
    val openComments by myViewModel.openComments.asLiveData().observeAsState(CommentState.Loading)


    NavBase(title = stringResource(id = R.string.feed)) {
        PostList(
            lazyListState = lazyListState,
            coroutineScope = coroutineScope,
            navigate = navigate,
            lazyItems = lazyItems,
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
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun PostList(
    lazyListState: LazyListState,
    coroutineScope: CoroutineScope,
    navigate: navArgs,
    observeCurrentlyOpenCommentSection : (postId : String)->Unit,
    postComment : (text : String, postAuthorId : String, postId : String) -> Unit,
    lazyItems: LazyPagingItems<Post>,
    openComments : CommentState,
){
    var commentSectionExpandedId by remember{ mutableStateOf("")}

    SwipeRefresh(
        state = rememberSwipeRefreshState(lazyItems.loadState.refresh is LoadState.Loading),
        onRefresh = { lazyItems.refresh() }
    ){
        LazyColumn(state = lazyListState, contentPadding = PaddingValues(bottom = 50.dp)) {

            if(lazyItems.itemCount == 0)item { EmptyResultAnimation() }
            else itemsIndexed(lazyItems){ index, item ->
                if(item != null){
                    Post(
                        username = item.username,
                        date = timestampToDateTime(item.date!!),
                        numberOfComments = { open ->
                                           if(open && openComments is CommentState.Success){
                                               openComments.result.size.toString()
                                           }else HealthApplication.Strings.get(R.string.comments )
                        },
                        commentClick = {
                            observeCurrentlyOpenCommentSection(item.postId)
                            commentSectionExpandedId = if(commentSectionExpandedId == item.postId){
                                ""
                            }else item.postId
                        },
                        isCommentSectionExpanded = {commentSectionExpandedId == item.postId},
                        onUserClick = {
                            navigate("${Constants.DESTINATION_PROFILE}/${item.authorId}"){
                                popUpTo(Constants.DESTINATION_FEED){inclusive = false}
                                launchSingleTop = true
                            }
                        },
                        comments = openComments,
                        scrollToOpenedCommentSection = {
                            coroutineScope.launch {
                                delay(250)
                                lazyListState.animateScrollToItem(index)
                            }
                        },
                        content = { item.DisplayContent(navigate) },
                        onSendComment = { text ->
                            postComment(text, item.authorId, item.postId)
                        }
                    )
                }else{
                    Failure(text = stringResource(id = R.string.empty), emoji = "🔍")
                }
                Spacer(modifier = Modifier.height(15.dp))
            }
            lazyItems.apply {
                when {
                    loadState.append is LoadState.Loading -> {
                        item {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }
                    loadState.refresh is LoadState.Error -> {
                        val e = lazyItems.loadState.refresh as LoadState.Error
                        item {
                            ErrorPagingItem(
                                message = e.error.message!!,
                                modifier = Modifier.fillParentMaxSize(),
                                onClickRetry = { retry() }
                            )
                        }
                    }
                    loadState.append is LoadState.Error -> {
                        val e = lazyItems.loadState.append as LoadState.Error
                        item {
                            ErrorPagingItem(
                                message = e.error.message!!,
                                modifier = Modifier.fillParentMaxSize(),
                                onClickRetry = { retry() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun Post(
    username : String,
    date : String,
    numberOfComments : (Boolean) -> String,
    commentClick : () -> Unit,
    isCommentSectionExpanded : () -> Boolean,
    onUserClick : () -> Unit,
    onSendComment: (String, ) -> Unit,
    comments: CommentState,
    scrollToOpenedCommentSection : ()->Unit,
    content : @Composable () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.surface, MaterialTheme.shapes.medium)
            .padding(top = 9.dp, start = 9.dp, end = 9.dp)
            .clip(MaterialTheme.shapes.medium)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CircleAvatar(text = username, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(3.dp))
            Column(Modifier.weight(5f), verticalArrangement = Arrangement.Center) {
                Text(text = username,
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 3.dp,top = 0.dp,bottom = 3.dp)
                        .clickable { onUserClick() }
                )
                Text(text = date, style = MaterialTheme.typography.subtitle1)

            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        content()
        Divider(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        commentClick()
                        scrollToOpenedCommentSection()
                    }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Comment,
                    contentDescription = "",
                    modifier = Modifier.size(21.dp)
                )
                Text(
                    text = numberOfComments(isCommentSectionExpanded()),
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .align(Alignment.CenterVertically),
                    color = MaterialTheme.colors.onSurface
                )
            }
        }
        AnimatedVisibility(visible = isCommentSectionExpanded()) {
            CommentSection(if(isCommentSectionExpanded()) comments else null){ text ->
                onSendComment(text)
            }
        }
    }
}

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@Composable
fun CommentSection(
    comments: CommentState?,
    onSendComment : (String) -> Unit
) {
    var writingCommentState by remember { mutableStateOf(TextFieldValue("")) }

    Card( elevation = 0.dp) {
        Column(
            Modifier
                .background(Color.Transparent)
                .padding(horizontal = 6.dp, vertical = 3.dp)) {

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                MyTextField(
                    value = writingCommentState,
                    onValueChanged = {
                        writingCommentState = it
                    },
                    label = stringResource(id = R.string.write_comment),
                    icon = Icons.Filled.AddComment,
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.Top),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = false
                )
                IconButton(onClick = {
                    onSendComment(writingCommentState.text)
                    writingCommentState = TextFieldValue("")
                                     },
                    modifier = Modifier.align(Alignment.Bottom)) {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "",
                        tint = MaterialTheme.colors.secondary,
                        modifier = Modifier
                            .align(Alignment.Bottom)
                            .size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            comments?.let{
                when(comments){
                    is CommentState.Failure -> Failure(text = comments.message)
                    is CommentState.Success -> comments.result.forEach{ comment ->
                        SingleComment(comment = comment)
                    }
                }
            }
        }
    }
}

@Composable
fun SingleComment(comment : Comment){
    Card(
        elevation = 5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 9.dp)
    ) {
        Column() {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 7.dp)) {
                Text(text = comment.username,
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                )
                Text(text = " • " + timestampToDateTime(comment.date!!),
                    style = MaterialTheme.typography.subtitle1,
                )
            }
            Row() {
                Text(text = comment.text,
                    style = MaterialTheme.typography.body1,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 7.dp,end = 7.dp,top = 4.dp, bottom = 9.dp)
                )
            }
        }
    }
}

@Composable
fun ErrorPagingItem(
    message: String,
    modifier: Modifier = Modifier,
    onClickRetry: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Failure(text = message)

        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = onClickRetry, modifier = Modifier.padding(bottom = 10.dp)) {
            Text(text = stringResource(id = R.string.try_again))
        }
    }
}

@Preview
@Composable
private fun Preview(){
    HealthTheme {
       // Post()
    }
}