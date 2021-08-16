package hu.scsaba.health.screens.loggedin.account.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.popUpTo
import hu.scsaba.health.HealthApplication
import hu.scsaba.health.R
import hu.scsaba.health.model.entities.user.User
import hu.scsaba.health.navigation.navArgs
import hu.scsaba.health.screens.loggedin.account.AccountContent
import hu.scsaba.health.screens.loggedin.account.AccountDetails
import hu.scsaba.health.screens.loggedin.account.AccountViewModel
import hu.scsaba.health.screens.loggedin.account.CircleShapedClickableItem
import hu.scsaba.health.ui.composables.Failure
import hu.scsaba.health.ui.composables.Loading
import hu.scsaba.health.utils.helper.Constants
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun Profile(backStackEntry: NavBackStackEntry,uid : String, navigate : navArgs){

    val localContext = LocalContext.current
    val myViewModel: ProfileViewModel = viewModel("profile",
        HiltViewModelFactory(localContext,backStackEntry)
    )

    val user by myViewModel.user.asLiveData().observeAsState(ProfileViewModel.ProfileState.Loading)

    DisposableEffect(Unit) {
        myViewModel.getUserData(uid)
        onDispose {  }
    }

    when(user){
        is ProfileViewModel.ProfileState.Loading -> Loading()

        is ProfileViewModel.ProfileState.Failure ->
            Failure(text = (user as ProfileViewModel.ProfileState.Failure).message)

        is ProfileViewModel.ProfileState.Success ->
            Success(
                user = (user as ProfileViewModel.ProfileState.Success).result,
                navigate = navigate,
            )
    }
}

@Composable
private fun Success(
    user : User,
    navigate: navArgs,
){
    val scaffoldState = rememberScaffoldState()

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
                                text = stringResource(id = R.string.posts).toUpperCase(Locale.ROOT),
                                icon = Icons.Rounded.Share
                            ){
                                navigate("${Constants.DESTINATION_USER_POSTS}/${user.uid}"){
                                    popUpTo(Constants.DESTINATION_PROFILE){inclusive = false}
                                    launchSingleTop = true
                                }
                            }
                            CircleShapedClickableItem(
                                modifier=Modifier.weight(1f),
                                text = stringResource(id = R.string.progress).toUpperCase(Locale.ROOT),
                                icon = Icons.Rounded.TrendingUp
                            ){
                                navigate("${Constants.DESTINATION_PROGRESS}/${user.uid}"){
                                    popUpTo(Constants.DESTINATION_PROFILE){inclusive = false}
                                    launchSingleTop = true
                                }
                            }
                            Spacer(Modifier.size(85.dp).weight(1f))
                        }){}
                    }
                }
            }
        }
    }
}