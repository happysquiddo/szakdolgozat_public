package hu.scsaba.health.screens.loggedin.account.search

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NavigateNext
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.popUpTo
import hu.scsaba.health.R
import hu.scsaba.health.navigation.navArgs
import hu.scsaba.health.screens.loggedin.workouts.AnimatedListVisibility
import hu.scsaba.health.utils.helper.Constants.DESTINATION_PROFILE
import hu.scsaba.health.utils.helper.Constants.DESTINATION_SEARCH
import hu.scsaba.health.ui.composables.CircleAvatar
import hu.scsaba.health.ui.composables.Failure
import hu.scsaba.health.ui.composables.Loading
import java.util.*

@ExperimentalAnimationApi
@Composable
fun Search(backStackEntry: NavBackStackEntry, navigate : navArgs){

    val myViewModel: SearchViewModel = viewModel("search",
        HiltViewModelFactory(LocalContext.current,backStackEntry)
    )

    val lazyListState = rememberLazyListState()
    val searchState by myViewModel.searchState.asLiveData()
        .observeAsState(SearchViewModel.SearchUserState.Idle)

    UserSearch(
        lazyListState = lazyListState,
        searchState = searchState,
        onUserClick = { uid ->
            navigate("$DESTINATION_PROFILE/${uid}"){
                popUpTo(DESTINATION_SEARCH){inclusive = false}
                launchSingleTop = true
            }
        },
        onBlankUserInput = {
            myViewModel.setIdleState()
        }
    ) { searchTerm ->
        myViewModel.searchUsers(searchTerm)
    }
}

@ExperimentalAnimationApi
@Composable
private fun UserSearch(
    lazyListState: LazyListState,
    searchState : SearchViewModel.SearchUserState,
    onUserClick: (uid : String) -> Unit,
    onBlankUserInput : () -> Unit,
    onSearchInput: (searchTerm : String) -> Unit,
){
    var textFieldValue by rememberSaveable{mutableStateOf("")}

    LazyColumn(state = lazyListState, contentPadding = PaddingValues(3.dp)) {
        item { 
            InputRow(
                textFieldValue = textFieldValue,
            ) {
                textFieldValue = it
                if(textFieldValue.isNotBlank())onSearchInput(textFieldValue)
                else onBlankUserInput()
            }
        }
        when(searchState){
            is SearchViewModel.SearchUserState.Idle -> item {
                Failure(text = stringResource(id = R.string.start_typing), emoji = "")
            }
            is SearchViewModel.SearchUserState.Loading -> item{ Loading()}
            is SearchViewModel.SearchUserState.Failure -> item{ Failure() }
            is SearchViewModel.SearchUserState.Success -> {
                if(searchState.result.isNotEmpty()){
                    itemsIndexed(searchState.result){ index,user ->
                        AnimatedListVisibility(listIndexFromOne = index+1) {
                            UserResultRow(text = user.username) {
                                onUserClick(user.uid)
                            }
                        }
                    }
                }else{
                    item{ Failure(text = stringResource(id = R.string.no_match).toUpperCase(Locale.ROOT)) }
                }
            }
        }
    }
}

@Composable
private fun InputRow(textFieldValue: String, onValueChange : (String) -> Unit){
    OutlinedTextField(
        value = textFieldValue,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = {Text(text = stringResource(id = R.string.search_username))}
        )
}

@Composable
private fun UserResultRow(text : String, onClick : () -> Unit){
    Card(
        backgroundColor = MaterialTheme.colors.surface,
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.large)
            .clickable { onClick() }
            .padding(vertical = 3.dp)){
        Row(Modifier.padding(start = 5.dp,end = 5.dp, top = 10.dp,bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            CircleAvatar(
                padding = 12.dp,
                text = text,
                fontSize = 13.sp,
                modifier = Modifier
                .weight(1f))
            Text(text = text, textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(2f), fontWeight = FontWeight(500), fontSize = 18.sp, letterSpacing = 0.9.sp)
            Icon(imageVector = Icons.Rounded.NavigateNext, contentDescription = "",
                tint = MaterialTheme.colors.onSurface)
        }
    }
}