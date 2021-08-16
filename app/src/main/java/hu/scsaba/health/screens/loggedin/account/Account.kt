package hu.scsaba.health.screens.loggedin.account

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.popUpTo
import hu.scsaba.health.HealthApplication
import hu.scsaba.health.R
import hu.scsaba.health.model.entities.user.User
import hu.scsaba.health.navigation.navArgs
import hu.scsaba.health.ui.theme.HealthTheme
import hu.scsaba.health.utils.helper.Constants
import hu.scsaba.health.utils.helper.Constants.DESTINATION_ACCOUNT
import hu.scsaba.health.utils.helper.Constants.DESTINATION_EDIT
import hu.scsaba.health.utils.helper.Constants.DESTINATION_PROGRESS
import hu.scsaba.health.utils.helper.Constants.DESTINATION_SEARCH
import hu.scsaba.health.utils.helper.Constants.DESTINATION_USER_POSTS
import hu.scsaba.health.ui.composables.*
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun Account(backStackEntry: NavBackStackEntry, navigate: (String,builder: NavOptionsBuilder.() -> Unit) -> Unit){
    val localContext = LocalContext.current

    val myViewModel: AccountViewModel = viewModel("account",
        HiltViewModelFactory(localContext,backStackEntry)
    )

    val logOutState by myViewModel.logOutState.asLiveData().observeAsState(AccountViewModel.LogoutState.Loading)

    val user by myViewModel.user.asLiveData().observeAsState(AccountViewModel.UserState.Loading)

    when(user){
        is AccountViewModel.UserState.Loading -> Loading()
        is AccountViewModel.UserState.Failure -> Failure(text = (user as AccountViewModel.UserState.Failure).message)
        is AccountViewModel.UserState.Success ->
            Success(
                user = (user as AccountViewModel.UserState.Success).result,
                myViewModel = myViewModel,
                navigate = navigate,
                logoutState = logOutState
            )
    }

    DisposableEffect(key1 = Unit) {
        myViewModel.getUserData()
        onDispose {  }
    }
}

@Composable
private fun Success(
    user : User,
    myViewModel : AccountViewModel,
    navigate: navArgs,
    logoutState: AccountViewModel.LogoutState
){
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    DisposableEffect(key1 = logoutState) {
        when(logoutState){
            is AccountViewModel.LogoutState.Failure ->scope.launch{
                scaffoldState.snackbarHostState
                    .showSnackbar(HealthApplication.Strings.get(R.string.something_wrong))
            }
            is AccountViewModel.LogoutState.Success -> {
                navigate(Constants.DESTINATION_LOGIN) {
                    popUpTo(Constants.DESTINATION_HOME) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        onDispose {  }
    }

    Column(Modifier.padding(bottom = 56.dp)) {
        Scaffold(
            scaffoldState = scaffoldState,
            backgroundColor = Color.Transparent,
            snackbarHost = { SnackbarHost(
                hostState = scaffoldState.snackbarHostState,
                modifier = Modifier.absoluteOffset(y = (-45).dp)
            ) }) {
            Column() {
                Column(Modifier.weight(1f)) {
                    AccountDetails(user = user){
                        AccountContent({
                            CircleShapedClickableItem(
                                modifier=Modifier.weight(1f),
                                text = stringResource(id = R.string.edit).toUpperCase(Locale.ROOT),
                                icon = Icons.Rounded.Edit
                            ){
                                navigate(DESTINATION_EDIT){
                                    launchSingleTop = true
                                    popUpTo(DESTINATION_ACCOUNT){inclusive = false}
                                }
                            }
                            CircleShapedClickableItem(
                                modifier=Modifier.weight(1f),
                                text = stringResource(id = R.string.search).toUpperCase(Locale.ROOT),
                                icon = Icons.Rounded.Search
                            ){
                                navigate(DESTINATION_SEARCH){
                                    launchSingleTop = true
                                    popUpTo(DESTINATION_ACCOUNT){inclusive = false}
                                }
                            }
                            CircleShapedClickableItem(
                                modifier=Modifier.weight(1f),
                                text = stringResource(id = R.string.progress).toUpperCase(Locale.ROOT),
                                icon = Icons.Rounded.TrendingUp
                            ){
                                navigate("$DESTINATION_PROGRESS/${user.uid}"){
                                    popUpTo(DESTINATION_ACCOUNT){inclusive = false}
                                    launchSingleTop = true
                                }
                            }
                        }){
                            CircleShapedClickableItem(
                                modifier=Modifier.weight(1f),
                                text = stringResource(id = R.string.posts).toUpperCase(Locale.ROOT),
                                icon = Icons.Rounded.Share
                            ){
                                navigate("$DESTINATION_USER_POSTS/${user.uid}"){
                                    popUpTo(DESTINATION_ACCOUNT){inclusive = false}
                                    launchSingleTop = true
                                }
                            }
                            CircleShapedClickableItem(
                                modifier=Modifier.weight(1f),
                                text = stringResource(id = R.string.logout).toUpperCase(Locale.ROOT),
                                icon = Icons.Rounded.ExitToApp,
                                backgroundColor = MaterialTheme.colors.secondaryVariant
                            ){ myViewModel.logOut() }
                            Spacer(
                                Modifier
                                    .size(87.dp)
                                    .weight(1f))
                        }
                    }
                }
            }
        }
    }

}


@Composable
fun ColumnScope.AccountDetails(modifier: Modifier = Modifier,
                                       user : User,
                                       content : @Composable () -> Unit
){
    Column(modifier = Modifier
        .background(color = MaterialTheme.colors.secondary)
        .weight(1f)
        .fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        Box(){CircleAvatar(text = user.username, fontSize = 26.sp)}
        Spacer(modifier = Modifier.height(6.dp))
        AccountDetailsRow(title = stringResource(id = R.string.login_username), text = user.username)
        AccountDetailsRow(title = stringResource(id = R.string.login_email), text = user.email)
        user.age?.let {
            AccountDetailsRow(title = stringResource(id = R.string.age), text = it.toString())
        }
        user.weightInKg?.let {
            AccountDetailsRow(title = stringResource(id = R.string.user_weight), text = "$it kg")
        }
        user.heightInCm?.let {
            AccountDetailsRow(title = stringResource(id = R.string.user_height) , text = "$it cm")
        }
        Spacer(modifier = Modifier.height(6.dp))


    }
    content()
}


@Composable
fun ColumnScope.AccountContent(
    firstRow : @Composable RowScope.() -> Unit,
    secondRow : @Composable RowScope.() -> Unit,
){
    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .background(color = MaterialTheme.colors.background)
            .weight(2f)
    ) {
        Spacer(modifier = Modifier.height(25.dp))
        Row(
            Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .background(Color.Transparent), verticalAlignment = Alignment.CenterVertically) {
            firstRow()
        }
        Spacer(modifier = Modifier.height(25.dp))
        Row(
            Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .background(
                    Color.Transparent
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            secondRow()
        }
    }
}

@Composable
private fun AccountDetailsRow(title : String, text : String, modifier: Modifier = Modifier){
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onSecondary,
            style = MaterialTheme.typography.subtitle1,
            modifier = modifier
                .animateContentSize()
                //.padding(3.dp)
                .align(Alignment.CenterStart))
        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onSecondary,
            style = MaterialTheme.typography.subtitle1,
            modifier = modifier
                .animateContentSize()
                // .padding(3.dp)
                .align(Alignment.CenterEnd))
    }
}

@Composable
fun CircleShapedClickableItem(
    text : String,
    icon : ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor : Color = MaterialTheme.colors.surface,
    onClick : () -> Unit
){
    Card(
        backgroundColor = backgroundColor,
        shape = CircleShape,
        elevation = 9.dp,
        modifier = modifier
            .wrapContentSize(Alignment.Center)
            .clip(shape = CircleShape)
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(10.dp)
                .size(87.dp)
        ) {
            Icon(imageVector = icon, contentDescription = "")
            Spacer(modifier = Modifier.height(5.dp))
            Text(text = text, style = MaterialTheme.typography.subtitle1, fontSize = 13.sp)
        }
    }
}

@Preview
@Composable
fun Preview(){
    HealthTheme {

    }
}