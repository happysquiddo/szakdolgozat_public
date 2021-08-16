package hu.scsaba.health.screens.loggedin.account.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Save
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import hu.scsaba.health.HealthApplication
import hu.scsaba.health.R
import hu.scsaba.health.model.entities.user.User
import hu.scsaba.health.ui.composables.*
import kotlinx.coroutines.launch

@Composable
fun EditProfile(backStackEntry: NavBackStackEntry, navigateBack : () -> Unit){
    val myViewModel: EditProfileViewModel = viewModel("editProfile",
        HiltViewModelFactory(LocalContext.current,backStackEntry)
    )

    val user by myViewModel.user.asLiveData().observeAsState(EditProfileViewModel.EditProfileState.Loading)

    when(user){
        is EditProfileViewModel.EditProfileState.Loading -> Loading()
        is EditProfileViewModel.EditProfileState.Failure -> Failure(text = (user as EditProfileViewModel.EditProfileState.Failure).message)
        is EditProfileViewModel.EditProfileState.Success ->
            Success(
                user = (user as EditProfileViewModel.EditProfileState.Success).result,
                viewModel = myViewModel,
                navigateBack = navigateBack
            )
    }
}

@Composable
private fun Success(user : User, viewModel : EditProfileViewModel, navigateBack : () -> Unit){

    val weightValue = remember(user) { mutableStateOf(user.weightInKg?:0)}
    val heightValue = remember(user) { mutableStateOf(user.heightInCm?:0)}
    val ageValue = remember(user) { mutableStateOf(user.age?:0)}
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val saveState by viewModel.saveState.asLiveData().observeAsState(EditProfileViewModel.EditProfileSaveState.Idle)
    var loading by remember{ mutableStateOf(false)}
    DisposableEffect(key1 = saveState) {
        when(saveState){
            is EditProfileViewModel.EditProfileSaveState.Success -> {
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(HealthApplication.Strings.get(R.string.success))
                    navigateBack()
                }
            }
            is EditProfileViewModel.EditProfileSaveState.Failure -> coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar((saveState as EditProfileViewModel.EditProfileSaveState.Failure).message)
            }
            is EditProfileViewModel.EditProfileSaveState.Loading -> loading = true
        }
        onDispose {
            loading = false
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { SnackbarHost(
            hostState = scaffoldState.snackbarHostState,
            modifier = Modifier.padding(10.dp).offset(y= (-60).dp)
        ) }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(horizontal = 10.dp, vertical = 30.dp)
        ) {
            Column(Modifier.weight(1f)) {
                EditIntegerAttribute(text = stringResource(id = R.string.user_weight), valueState = weightValue, label = "kg")
                EditIntegerAttribute(text = stringResource(id = R.string.user_height), valueState = heightValue, label = "cm")
                EditIntegerAttribute(text = stringResource(id = R.string.age), valueState = ageValue, label = "")
            }
            ButtonWithIcon(loading = loading,icon = Icons.Rounded.Save, text = stringResource(id = R.string.save), modifier = Modifier.fillMaxWidth()) {
                viewModel.save(weightValue.value, heightValue.value, ageValue.value)
            }
        }
    }
}

