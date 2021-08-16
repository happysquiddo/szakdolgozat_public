package hu.scsaba.health.screens.signup

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
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
import hu.scsaba.health.utils.helper.Constants.DESTINATION_HOME
import hu.scsaba.health.utils.helper.Constants.DESTINATION_LOGIN
import hu.scsaba.health.utils.helper.Constants.DESTINATION_SIGNUP
import hu.scsaba.health.ui.composables.ButtonWithIcon
import hu.scsaba.health.ui.composables.DividerWithText
import hu.scsaba.health.ui.composables.MyTextField
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@Composable
fun SignUp(backStackEntry: NavBackStackEntry, navController: NavController) {

    val localLifecycleOwner = LocalLifecycleOwner.current
    val localContext = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val myViewModel: SignUpViewModel = viewModel("signup",
        HiltViewModelFactory(localContext,backStackEntry)
    )

    val emailState = remember { mutableStateOf(TextFieldValue("")) }
    val usernameState = remember { mutableStateOf(TextFieldValue("")) }
    val passwordState = remember { mutableStateOf(TextFieldValue("")) }

    val errorEmail = remember{ mutableStateOf(false)}
    val errorPassword = remember{ mutableStateOf(false)}
    val errorUsername = remember{ mutableStateOf(false)}

    var loading by remember { mutableStateOf(false)}

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
                    findError = {errorEmail.value = !emailState.value.text.isValidEmail() }
                )

                Spacer(modifier = Modifier.padding(5.dp))

                MyTextField(
                    value = usernameState.value,
                    onValueChanged = {
                        usernameState.value = it
                    },
                    label = stringResource(id = R.string.login_username),
                    icon = Icons.Filled.AccountBox,
                    modifier = modifier,
                    isError = errorUsername.value,
                    errorMessage = stringResource(id = R.string.textfield_error_username),
                    findError = {errorUsername.value = !usernameState.value.text.isValidUsername() }
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
                    findError = {errorPassword.value = !passwordState.value.text.isValidPassword() }

                )
                Spacer(modifier = Modifier.padding(vertical = 12.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(disabledBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.6f)
                        .compositeOver(MaterialTheme.colors.background)),
                    enabled = !loading,
                    elevation = ButtonDefaults.elevation(5.dp),
                    onClick = {
                        errorEmail.value = !emailState.value.text.isValidEmail()
                        errorUsername.value = !usernameState.value.text.isValidUsername()
                        errorPassword.value = !passwordState.value.text.isValidPassword()

                        if(!errorEmail.value && !errorPassword.value && !errorUsername.value){

                            myViewModel.uiState.asLiveData().observe(localLifecycleOwner) { result ->
                                when (result) {
                                    is SignUpViewModel.SignUpState.Failure -> scope.launch {
                                        loading = false
                                        scaffoldState.snackbarHostState.showSnackbar(result.message)
                                    }
                                    is SignUpViewModel.SignUpState.Loading -> loading = true
                                    is SignUpViewModel.SignUpState.Success -> {
                                        navController.navigate(DESTINATION_HOME) {
                                            popUpTo(DESTINATION_LOGIN) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            }
                            myViewModel.signUp(email = emailState.value.text, password = passwordState.value.text,
                                username = usernameState.value.text)
                        }
                    },
                    modifier = modifier.background(color = Color.Black,shape = MaterialTheme.shapes.medium),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    if(loading) CircularProgressIndicator(color = MaterialTheme.colors.onPrimary, modifier = Modifier.size(20.dp))
                    else Text(text = stringResource(id = R.string.signup))
                }
                DividerWithText(text = stringResource(id = R.string.login_instead))

                ButtonWithIcon(
                    modifier = modifier,
                    icon = Icons.Default.ArrowBack,
                    text = stringResource(id = R.string.login)
                ){
                    navController.navigate(route = DESTINATION_LOGIN){
                        popUpTo(DESTINATION_SIGNUP){inclusive = true}
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}
