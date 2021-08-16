package hu.scsaba.health.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.*
import hu.scsaba.health.HealthApplication
import hu.scsaba.health.R
import hu.scsaba.health.screens.login.Login
import hu.scsaba.health.screens.signup.SignUp
import hu.scsaba.health.screens.Splash
import hu.scsaba.health.screens.loggedin.account.Account
import hu.scsaba.health.screens.loggedin.account.edit.EditProfile
import hu.scsaba.health.screens.loggedin.account.profile.Profile
import hu.scsaba.health.screens.loggedin.account.progress.Progress
import hu.scsaba.health.screens.loggedin.account.search.Search
import hu.scsaba.health.screens.loggedin.account.userposts.UserPosts
import hu.scsaba.health.screens.loggedin.feed.Feed
import hu.scsaba.health.screens.loggedin.home.Home
import hu.scsaba.health.screens.loggedin.breaks.Breaks
import hu.scsaba.health.screens.loggedin.workouts.create.CreateWorkout
import hu.scsaba.health.screens.loggedin.workouts.list.WorkoutList
import hu.scsaba.health.screens.loggedin.workouts.Exercises
import hu.scsaba.health.screens.loggedin.water.Water
import hu.scsaba.health.screens.loggedin.workouts.workout.DoWorkout
import hu.scsaba.health.utils.helper.Constants
import hu.scsaba.health.utils.helper.Constants.DESTINATION_BREAKS
import hu.scsaba.health.utils.helper.Constants.DESTINATION_CREATE_WORKOUT
import hu.scsaba.health.utils.helper.Constants.DESTINATION_DO_WORKOUT
import hu.scsaba.health.utils.helper.Constants.DESTINATION_EDIT
import hu.scsaba.health.utils.helper.Constants.DESTINATION_EXERCISES
import hu.scsaba.health.utils.helper.Constants.DESTINATION_WORKOUT_LIST
import hu.scsaba.health.utils.helper.Constants.DESTINATION_LOGIN
import hu.scsaba.health.utils.helper.Constants.DESTINATION_PROFILE
import hu.scsaba.health.utils.helper.Constants.DESTINATION_PROGRESS
import hu.scsaba.health.utils.helper.Constants.DESTINATION_SEARCH
import hu.scsaba.health.utils.helper.Constants.DESTINATION_SIGNUP
import hu.scsaba.health.utils.helper.Constants.DESTINATION_SPLASH
import hu.scsaba.health.utils.helper.Constants.DESTINATION_USER_POSTS
import hu.scsaba.health.utils.helper.Constants.DESTINATION_WATER
import hu.scsaba.health.screens.Attributions
import hu.scsaba.health.utils.helper.Constants.DESTINATION_ATTRIBUTIONS
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
@Composable
fun Navigation() {

    val navController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()
    val doneLoading = rememberSaveable { mutableStateOf(false) }

    val uiState by mainViewModel.uiState.asLiveData().observeAsState()

    val bottomNavScreens = remember{
        listOf(
            NavScreens.Home{route, builder ->
                navController.navigate(route, builder)
            },
            NavScreens.Feed{route, builder ->
                navController.navigate(route, builder)
            },
            NavScreens.Account{route, builder ->
                navController.navigate(route, builder)
            }
        )
    }

    val screenMap = bottomNavScreens.associateBy { it.route }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.arguments?.getString(KEY_ROUTE)
    if(currentRoute in screenMap){
        mainViewModel.isLoggedIn()
    }

    if(!doneLoading.value) {
        when (uiState) {
            is MainViewModel.MainState.Success -> {
                navController.navigate(Constants.DESTINATION_HOME) {

                    popUpTo(Constants.DESTINATION_SPLASH){inclusive = true}
                    launchSingleTop = true
                }
                doneLoading.value = true
            }
            is MainViewModel.MainState.Failure -> {
                navController.navigate(Constants.DESTINATION_LOGIN) {
                    popUpTo(Constants.DESTINATION_SPLASH){inclusive = true}
                    launchSingleTop = true
                }
                doneLoading.value = true
            }
        }
    }


    val transitionState = remember { MutableTransitionState(doneLoading) }
    val transition = updateTransition(transitionState, label = "")
    val contentAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 650) }, label = ""
    ) {
        if (!it.value) 0f else 1f
    }
    val contentTopPadding by transition.animateDp(
        transitionSpec = { tween(durationMillis = 440) }, label = ""
    ) {
        if (!it.value) 220.dp else 0.dp
    }

    Surface(
        color = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.onBackground,
        modifier = Modifier.fillMaxSize()
    ) {


        Scaffold(
            bottomBar = {
                if(currentRoute in screenMap ){
                    BottomNavigation(
                        backgroundColor = MaterialTheme.colors.primary
                    ) {
                        bottomNavScreens.forEach { screen ->
                            BottomNavigationItem(
                                icon = { Icon(screen.icon,"") },
                                label = { Text(screen.title) },
                                selected = currentRoute == screen.route,
                                alwaysShowLabel = false,
                                onClick = {
                                    navController.navigate(screen.route){
                                        popUpTo(Constants.DESTINATION_HOME){inclusive = false}
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .alpha(contentAlpha)
                .padding(top = contentTopPadding)
                .fillMaxSize()
        ) {
            NavHost(navController = navController, startDestination = DESTINATION_SPLASH) {
                bottomNavScreens.forEach { composable(it.route, content = it.content) }

                composable(DESTINATION_SPLASH){ Splash() }

                //login / signup
                composable(DESTINATION_LOGIN) { backStackEntry ->
                    Login(backStackEntry, navController)
                }
                composable(DESTINATION_SIGNUP) { backStackEntry ->
                    SignUp(backStackEntry, navController)
                }

                //workout
                composable(DESTINATION_EXERCISES) { backStackEntry ->
                    Exercises(backStackEntry){route, builder ->
                        navController.navigate(route, builder)
                    }
                }
                composable(DESTINATION_WORKOUT_LIST) { backStackEntry ->
                    WorkoutList(backStackEntry){ workoutName, uid ->
                        navController.navigate("${DESTINATION_DO_WORKOUT}/${workoutName}/${uid}"){
                            popUpTo(DESTINATION_WORKOUT_LIST) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
                composable(DESTINATION_CREATE_WORKOUT) { backStackEntry ->
                    CreateWorkout(backStackEntry,
                        { route, builder ->
                            navController.navigate(route, builder)
                        },
                        {
                            navController.popBackStack()
                        }
                    )
                }
                composable(
                    "${DESTINATION_DO_WORKOUT}/{WorkoutName}/{uid}",
                    arguments = listOf(
                        navArgument("WorkoutName") { type = NavType.StringType },
                        navArgument("uid"){ type = NavType.StringType }
                    )
                ) { backStackEntry ->
                        DoWorkout(
                            backStackEntry,
                            backStackEntry.arguments!!.getString("WorkoutName")!!,
                            backStackEntry.arguments!!.getString("uid")!!
                        ){
                            navController.popBackStack()
                        }
                }

                //water
                composable(DESTINATION_WATER) { backStackEntry ->
                    Water(backStackEntry){route, builder ->
                        navController.navigate(route, builder)
                    }
                }

                //breaks
                composable(DESTINATION_BREAKS) { backStackEntry ->
                    Breaks(backStackEntry){route, builder ->
                        navController.navigate(route, builder)
                    }
                }

                //account
                composable(
                    "${DESTINATION_PROFILE}/{uid}",
                    arguments = listOf(navArgument("uid"){ type = NavType.StringType })
                ) { backStackEntry ->
                    Profile(
                        backStackEntry,
                        backStackEntry.arguments!!.getString("uid")!!
                    ){route, builder ->
                        navController.navigate(route, builder)
                    }
                }

                composable(
                    "${DESTINATION_USER_POSTS}/{uid}",
                    arguments = listOf(navArgument("uid"){ type = NavType.StringType })
                ) { backStackEntry ->
                    UserPosts(
                        backStackEntry,
                        backStackEntry.arguments!!.getString("uid")!!
                    ){route, builder ->
                        navController.navigate(route, builder)
                    }
                }

                composable(
                    "${DESTINATION_PROGRESS}/{uid}",
                    arguments = listOf(navArgument("uid"){ type = NavType.StringType })
                ) { backStackEntry ->
                    Progress(
                        backStackEntry,
                        backStackEntry.arguments!!.getString("uid")!!
                    ){route, builder ->
                        navController.navigate(route, builder)
                    }
                }

                composable(DESTINATION_SEARCH) { backStackEntry ->
                    Search(backStackEntry){route, builder ->
                        navController.navigate(route, builder)
                    }
                }
                composable(DESTINATION_EDIT) { backStackEntry ->
                    EditProfile(backStackEntry){
                        navController.popBackStack()
                    }
                }

                //attributions
                composable(DESTINATION_ATTRIBUTIONS) { _ ->
                    Attributions()
                }
            }
        }
    }
}

private sealed class NavScreens(val route: String, val title: String, val icon : ImageVector, val content: @Composable (backStackEntry: NavBackStackEntry) -> Unit) {
    class Home(navigate : navArgs): NavScreens(
        Constants.DESTINATION_HOME, HealthApplication.Strings.get(R.string.home), Icons.Outlined.Home,{ backStackEntry -> Home(backStackEntry, navigate) }
    )

    @ExperimentalCoroutinesApi
    @ExperimentalAnimationApi
    @ExperimentalFoundationApi
    class Feed(navigate : navArgs): NavScreens(
        Constants.DESTINATION_FEED, HealthApplication.Strings.get(R.string.feed), Icons.Outlined.Share,{ backStackEntry -> Feed(backStackEntry, navigate) }
    )

    class Account(navigate : navArgs): NavScreens(
        Constants.DESTINATION_ACCOUNT, HealthApplication.Strings.get(R.string.account), Icons.Outlined.AccountCircle,{ backStackEntry -> Account(backStackEntry, navigate) }
    )
}
typealias navArgs = (String,builder: NavOptionsBuilder.() -> Unit) -> Unit