package hu.scsaba.health.screens.login

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import androidx.navigation.compose.popUpTo
import hu.scsaba.health.R
import hu.scsaba.health.utils.helper.*
import hu.scsaba.health.utils.helper.Constants.DESTINATION_LOGIN
import hu.scsaba.health.utils.helper.Constants.DESTINATION_SIGNUP
import hu.scsaba.health.ui.composables.DividerWithText
import hu.scsaba.health.ui.composables.MyTextField
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@Composable
fun Login(backStackEntry: NavBackStackEntry, navController: NavController) {

    val localLifecycleOwner = LocalLifecycleOwner.current
    val localContext = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val myViewModel: LoginViewModel = viewModel("login",
        HiltViewModelFactory(localContext,backStackEntry)
    )

    val emailState = remember { mutableStateOf(TextFieldValue("")) }
    val passwordState = remember { mutableStateOf(TextFieldValue("")) }

    val errorEmail = remember{ mutableStateOf(false)}
    val errorPassword = remember{ mutableStateOf(false)}

    var loading by rememberSaveable { mutableStateOf(false)}

    Surface(
        contentColor = MaterialTheme.colors.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()) {
        Scaffold(
            scaffoldState = scaffoldState
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                Spacer(modifier = Modifier.padding(15.dp))
                MyTextField(
                    value = emailState.value,
                    onValueChanged = {
                        emailState.value = it
                    },
                    label = stringResource(id = R.string.login_email),
                    icon = Icons.Filled.Email,
                    modifier = modifier,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = errorEmail.value,
                    errorMessage = stringResource(id = R.string.textfield_error_email),
                    findError = {errorEmail.value = !emailState.value.text.isValidEmail()}
                    )

                Spacer(modifier = Modifier.padding(5.dp))

                MyTextField(
                    value = passwordState.value,
                    onValueChanged = {
                        passwordState.value = it
                    },
                    label = stringResource(id = R.string.login_password),
                    icon = Icons.Filled.Lock,
                    modifier = modifier,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = errorPassword.value,
                    errorMessage = stringResource(id = R.string.textfield_error_password),
                    findError = {errorPassword.value = !passwordState.value.text.isValidPassword()}
                    )
                Spacer(modifier = Modifier.padding(vertical = 12.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(disabledBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.6f)
                        .compositeOver(MaterialTheme.colors.background)),
                    enabled = !loading,
                    elevation = ButtonDefaults.elevation(5.dp),
                    onClick = {
                        errorEmail.value = !emailState.value.text.isValidEmail()
                        errorPassword.value = !passwordState.value.text.isValidPassword()

                        if(!errorEmail.value && !errorPassword.value){

                            myViewModel.uiState.asLiveData().observe(localLifecycleOwner) { result ->
                                when (result) {
                                    is LoginViewModel.LoginState.Failure -> scope.launch {
                                        loading = false
                                        scaffoldState.snackbarHostState.showSnackbar(result.message)
                                    }
                                    is LoginViewModel.LoginState.Loading -> loading = true
                                    is LoginViewModel.LoginState.Success -> {
                                        navController.navigate(Constants.DESTINATION_HOME) {
                                            popUpTo(Constants.DESTINATION_LOGIN) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                                myViewModel.resetState()
                            }
                            myViewModel.login(email = emailState.value.text, password = passwordState.value.text)
                        }
                    },
                    modifier = modifier.background(color = Color.Black,shape = MaterialTheme.shapes.medium),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    if(loading) CircularProgressIndicator(color = MaterialTheme.colors.onPrimary, modifier = Modifier.size(20.dp))
                    else Text(text = stringResource(id = R.string.login))
                }
                DividerWithText(text = stringResource(id = R.string.login_not_registered))

                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary, contentColor = MaterialTheme.colors.onSecondary),
                    elevation = ButtonDefaults.elevation(1.dp),
                    onClick = {
                              navController.navigate(route = DESTINATION_SIGNUP){
                                  popUpTo(DESTINATION_LOGIN){inclusive = false}
                                  launchSingleTop = true
                              }
                    },
                    modifier = modifier,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.signup))
                }
            }
        }
    }
}