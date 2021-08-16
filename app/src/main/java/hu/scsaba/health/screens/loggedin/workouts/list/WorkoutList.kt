package hu.scsaba.health.screens.loggedin.workouts.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import hu.scsaba.health.HealthApplication
import hu.scsaba.health.R
import hu.scsaba.health.model.entities.workout.WorkoutEntity
import hu.scsaba.health.screens.loggedin.workouts.AnimatedListVisibility
import hu.scsaba.health.ui.composables.*


@ExperimentalAnimationApi
@Composable
fun WorkoutList(backStackEntry: NavBackStackEntry, navigateToWorkout: (String, String) -> Unit){

    val myViewModel: WorkoutListViewModel = viewModel("workoutList",
        HiltViewModelFactory(LocalContext.current,backStackEntry)
    )
    val lazyListState = rememberLazyListState()
    val workoutShareState by myViewModel.workoutShareState.asLiveData().observeAsState(WorkoutListViewModel.WorkoutShareState.IDLE)
    val uid by myViewModel.uid.asLiveData().observeAsState("")
    val workoutList by myViewModel.workoutList.asLiveData().observeAsState(WorkoutListViewModel.WorkoutListState.Loading)

    NavBase(title = stringResource(id = R.string.workouts)) {
        Crossfade(targetState = workoutList) {
            when(it){
                is WorkoutListViewModel.WorkoutListState.Loading -> Loading()
                is WorkoutListViewModel.WorkoutListState.Failure -> Failure()
                is WorkoutListViewModel.WorkoutListState.Success -> {
                    if((workoutList as WorkoutListViewModel.WorkoutListState.Success).result.isEmpty()){
                        EmptyResultAnimation()
                    }else Success(
                        uid = uid,
                        workoutList = (workoutList as WorkoutListViewModel.WorkoutListState.Success).result,
                        lazyListState = lazyListState,
                        workoutShareState = workoutShareState,
                        postWorkout = { workoutEntity, text ->
                            myViewModel.postWorkout(workoutEntity , text)
                        },
                        navigateToWorkout = navigateToWorkout
                    )
                }
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun Success(
    uid : String,
    workoutList : List<WorkoutEntity>,
    lazyListState : LazyListState,
    workoutShareState: WorkoutListViewModel.WorkoutShareState,
    postWorkout: (WorkoutEntity, String) -> Unit,
    navigateToWorkout: (String, String) -> Unit
){
    var initialLoadForAnimationDone by remember{ mutableStateOf(false)}
    var currentlySharing by remember{ mutableStateOf(-1)}
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        state = lazyListState,
    ) {
        if(workoutList.isNotEmpty()) {
            itemsIndexed(workoutList){ index, item ->
                var shared by remember{ mutableStateOf(false)}
                val shareState by remember(workoutShareState) {
                    derivedStateOf {
                        if (currentlySharing == index){
                            if(workoutShareState == WorkoutListViewModel.WorkoutShareState.SUCCESS){
                                shared = true
                            }
                            workoutShareState
                        }
                        else if(shared){
                            WorkoutListViewModel.WorkoutShareState.SUCCESS
                        }
                        else{
                            WorkoutListViewModel.WorkoutShareState.IDLE
                        }
                    }
                }

                val visibilityIndex by remember(lazyListState){
                    derivedStateOf {
                        if(lazyListState.firstVisibleItemIndex > 0 || initialLoadForAnimationDone){
                            initialLoadForAnimationDone = true
                            1
                        }else index
                    }
                }

                var postText by remember {
                    mutableStateOf(TextFieldValue(HealthApplication.Strings.get(R.string.check_out_workout)))
                }
                var textInputVisible by remember { mutableStateOf(false) }

                AnimatedListVisibility(listIndexFromOne = visibilityIndex+1){
                    Column() {
                        ContentCardWithAction(
                            text = item.name,
                            subText = item.rounds.toString() + " " + stringResource(R.string.rounds),
                            fontSize = 17.sp,
                            bottomSpacer = 0.dp
                        ) {
                            Spacer(modifier = Modifier.width(15.dp))
                            when(shareState){
                                WorkoutListViewModel.WorkoutShareState.LOADING ->{
                                    CircularProgressIndicator()
                                }
                                WorkoutListViewModel.WorkoutShareState.SUCCESS ->{
                                    SuccessAnimation()
                                }
                                else -> {
                                    SmallIcon(size = 35.dp, icon = Icons.Rounded.Share) {
                                        textInputVisible = !textInputVisible
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(25.dp))
                            SmallIcon(size = 40.dp, icon = Icons.Rounded.PlayCircle) {
                                navigateToWorkout(workoutList[index].name, uid)
                            }
                        }
                        AnimatedVisibility(visible = textInputVisible) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                                MyTextField(
                                    value = postText,
                                    onValueChanged = {
                                        postText = it
                                    },
                                    label = stringResource(id = R.string.about_post),
                                    icon = Icons.Filled.PostAdd,
                                    modifier = Modifier
                                        .weight(1f)
                                        .align(Alignment.Top)
                                        .wrapContentSize(Alignment.TopCenter),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                    singleLine = false
                                )
                                IconButton(onClick = {
                                    postWorkout(
                                        workoutList[index],
                                        postText.text
                                    )
                                    currentlySharing = index
                                    textInputVisible = false
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
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun RowScope.SmallIcon(size : Dp, icon : ImageVector, onClick : () -> Unit){
    Icon(
        imageVector = icon,
        contentDescription = "",
        modifier = Modifier
            .clickable {
                onClick()
            }
            .size(size)
            .align(Alignment.CenterVertically),
        tint = MaterialTheme.colors.primary
    )
}
