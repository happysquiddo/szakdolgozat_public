package hu.scsaba.health.screens.loggedin.workouts.workout

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import hu.scsaba.health.HealthApplication
import hu.scsaba.health.R
import hu.scsaba.health.model.entities.workout.ExerciseEntity
import hu.scsaba.health.ui.composables.*
import hu.scsaba.health.utils.helper.formatTime

@ExperimentalAnimationApi
@Composable
fun DoWorkout(backStackEntry: NavBackStackEntry,  workoutName : String,uid : String,navigateBack : () -> Unit){
    (LocalContext.current as Activity).window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    val myViewModel: DoWorkoutViewModel = viewModel(
        "doWorkout",
        HiltViewModelFactory(LocalContext.current,backStackEntry)
    )

    val isStarted = myViewModel.isStarted.asLiveData()
        .observeAsState(initial = DoWorkoutViewModel.WorkoutProgressState.STOPPED)
    
    val breakState by myViewModel.breakState.asLiveData()
        .observeAsState(initial = false)
    
    val timeState by myViewModel.timeState.asLiveData()
        .observeAsState(initial = 0L)
    
    val totalRounds by myViewModel.totalRounds.asLiveData()
        .observeAsState(initial = 0)
    
    val remainingRounds by myViewModel.remainingRounds.asLiveData()
        .observeAsState(initial = 0)
    
    val exerciseDoneIndex by myViewModel.exercisesDoneIndex.asLiveData()
        .observeAsState(initial = 0)
    
    val exerciseListState by myViewModel.exerciseListState.asLiveData()
        .observeAsState(initial = DoWorkoutViewModel.DoWorkoutState.Loading)
    
    val saveProgressState by myViewModel.saveProgressState.asLiveData()
        .observeAsState(initial = DoWorkoutViewModel.SaveProgressState.LOADING)

    val lazyListState = rememberLazyListState()
    val transitionState = remember { MutableTransitionState(isStarted) }
    val transition = updateTransition(transitionState, label = "")
    val playPauseButtonOffset by transition.animateDp(
        transitionSpec = { tween(durationMillis = 650) }, label = ""
    ) {
        if (it.value == DoWorkoutViewModel.WorkoutProgressState.STOPPED) 0.dp else (105).dp
    }
    val playPauseButtonSize by transition.animateDp(
        transitionSpec = { tween(durationMillis = 650) }, label = ""
    ) {
        if (it.value == DoWorkoutViewModel.WorkoutProgressState.STOPPED) 65.dp else 55.dp
    }

    DisposableEffect(Unit){
        myViewModel.getWorkout(workoutName, uid)
        onDispose {  }
    }

    if(saveProgressState == DoWorkoutViewModel.SaveProgressState.SUCCESS) ConfettiAnimation()

    NavBase(title = workoutName) {
            when(exerciseListState){
                is DoWorkoutViewModel.DoWorkoutState.Loading -> Loading()
                is DoWorkoutViewModel.DoWorkoutState.Failure -> Failure()
                is DoWorkoutViewModel.DoWorkoutState.Success -> {
                    Success(
                        myViewModel = myViewModel,
                        exercises = (exerciseListState as DoWorkoutViewModel.DoWorkoutState.Success).result,
                        success = isStarted.value == DoWorkoutViewModel.WorkoutProgressState.FINISHED,
                        breakState = breakState,
                        isStarted = isStarted,
                        timeState = timeState,
                        totalRounds = totalRounds,
                        remainingRounds = remainingRounds.coerceAtLeast(0),
                        exercisesDoneIndex = exerciseDoneIndex.coerceAtMost((exerciseListState as DoWorkoutViewModel.DoWorkoutState.Success).result.size-1),
                        lazyListState = lazyListState,
                        navigateBack = navigateBack,
                        playPauseButtonOffset = playPauseButtonOffset,
                        playPauseButtonSize = playPauseButtonSize,
                        saveProgressState = saveProgressState
                    )
                }
            }

        }

}

@ExperimentalAnimationApi
@Composable
private fun Success(
    myViewModel : DoWorkoutViewModel,
    exercises : List<ExerciseEntity>,
    success : Boolean,
    breakState : Boolean,
    isStarted : State<DoWorkoutViewModel.WorkoutProgressState>,
    saveProgressState: DoWorkoutViewModel.SaveProgressState,
    timeState : Long,
    totalRounds : Int,
    remainingRounds : Int,
    exercisesDoneIndex : Int,
    lazyListState: LazyListState,
    navigateBack: () -> Unit,
    playPauseButtonOffset : Dp,
    playPauseButtonSize : Dp
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
        //.verticalScroll(rememberScrollState())
        .fillMaxSize()) {

        DoWorkoutHeader(
            myViewModel = myViewModel,
            exercises = exercises,
            isStarted = isStarted,
            breakState = breakState,
            timeState = timeState,
            saveProgressState = saveProgressState,
            exercisesDoneIndex = exercisesDoneIndex,
            lazyListState = lazyListState,
            playPauseButtonOffset = playPauseButtonOffset,
            playPauseButtonSize = playPauseButtonSize
        )
        Spacer(modifier = Modifier.height(10.dp))

        Progress(
            totalRounds = totalRounds,
            remainingRounds = remainingRounds,
            exercisesDoneIndex = exercisesDoneIndex,
            exerciseCountPerRound = exercises.size,
        )

        Spacer(modifier = Modifier.height(10.dp))
        ExerciseProgressList(
            lazyListState = lazyListState,
            exercises = exercises,
            exercisesDoneIndex = exercisesDoneIndex,
            success = success
        )

    }
}

@Composable
private fun Progress(
    totalRounds : Int,
    remainingRounds : Int,
    exercisesDoneIndex : Int,
    exerciseCountPerRound : Int,
){
    val currentRound = totalRounds-remainingRounds
    Row(Modifier.fillMaxWidth()) {
        for(i in 0 until totalRounds){
            val currentProgress = when{
                currentRound < i -> 0f
                currentRound == i -> exercisesDoneIndex/exerciseCountPerRound.toFloat()
                currentRound > i -> 1f
                else -> 0f
            }
            LinearProgressIndicator(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp),
                progress = currentProgress
            )
        }
    }
}

@Composable
private fun DoWorkoutHeader(
    myViewModel: DoWorkoutViewModel,
    exercises: List<ExerciseEntity>,
    isStarted: State<DoWorkoutViewModel.WorkoutProgressState>,
    saveProgressState: DoWorkoutViewModel.SaveProgressState,
    breakState: Boolean,
    timeState: Long,
    exercisesDoneIndex: Int,
    lazyListState: LazyListState,
    playPauseButtonOffset: Dp,
    playPauseButtonSize: Dp
){
    LaunchedEffect(key1 = exercisesDoneIndex){
        lazyListState.animateScrollToItem(exercisesDoneIndex-1)
    }
    val currentExerciseText by remember(breakState,exercisesDoneIndex) {
        derivedStateOf {
            if(breakState) HealthApplication.Strings.get(R.string.workout_break)
            else exercises[exercisesDoneIndex].name
        }
    }
    Box(Modifier.padding(16.dp)) {
        AnimatedCircle(
            modifier = Modifier
                .height(225.dp)
                .align(Alignment.Center)
                .fillMaxWidth(),
            remainingTime = timeState,
            selectedTime = (exercises[exercisesDoneIndex].durationMinutes.toLong()*60*1000)
                    + (exercises[exercisesDoneIndex].durationSeconds.toLong()*1000) ,
            state = isStarted.value == DoWorkoutViewModel.WorkoutProgressState.STARTED && !breakState
        )
        Button(
            onClick = {
                when(isStarted.value){
                    DoWorkoutViewModel.WorkoutProgressState.STOPPED -> myViewModel.startWorkout()
                    DoWorkoutViewModel.WorkoutProgressState.STARTED -> myViewModel.pause()
                }
            },
            modifier = Modifier
                .absoluteOffset(y = playPauseButtonOffset)
                .align(Alignment.Center)
                .size(playPauseButtonSize)
                .clip(CircleShape)

        ) {
            Crossfade(targetState = isStarted.value) { isStarted ->
                when (isStarted) {
                    DoWorkoutViewModel.WorkoutProgressState.STARTED ->
                        Icon(Icons.Rounded.PauseCircle, "", Modifier.size(playPauseButtonSize))

                    DoWorkoutViewModel.WorkoutProgressState.STOPPED ->
                        Icon(Icons.Rounded.PlayCircle, "", Modifier.size(playPauseButtonSize))

                    DoWorkoutViewModel.WorkoutProgressState.FINISHED ->
                        Icon(Icons.Rounded.CheckCircle, "", Modifier.size(playPauseButtonSize))
                }
            }
        }
        Crossfade(
            targetState = isStarted.value,
            modifier = Modifier.align(Alignment.Center,),
            animationSpec = tween(delayMillis = 100)
        ) { isStarted ->
            when (isStarted) {
                DoWorkoutViewModel.WorkoutProgressState.STARTED -> {
                    AnimatedCircleText(
                        text = formatTime(timeState),
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Text(
                        text = currentExerciseText,
                        style = MaterialTheme.typography.h3,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = 35.dp)
                    )
                }
                DoWorkoutViewModel.WorkoutProgressState.FINISHED -> {
                    AnimatedCircleText(
                        text = stringResource(id = R.string.well_done),
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Crossfade(targetState = saveProgressState) { saveState ->
                        when(saveState){
                            DoWorkoutViewModel.SaveProgressState.LOADING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .offset(y = 35.dp)
                                )
                            }
                            DoWorkoutViewModel.SaveProgressState.SUCCESS ->{
                                Text(
                                    text = stringResource(id = R.string.progress_saved),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.h3,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .offset(y = 35.dp)
                                )
                            }
                            DoWorkoutViewModel.SaveProgressState.FAILURE ->{
                                Text(
                                    text = stringResource(id = R.string.something_wrong),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.h3,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .offset(y = 35.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun ExerciseProgressList(
    lazyListState: LazyListState,
    exercises : List<ExerciseEntity>,
    exercisesDoneIndex : Int,
    success : Boolean
){
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        state = lazyListState,
    ) {
        if(exercises.isNotEmpty()) {
            itemsIndexed(exercises){ index, item ->
                ContentCardWithAction(
                    text = item.name,
                    subText = String.format("%02d : %02d",item.durationMinutes,item.durationSeconds),
                    fontSize = 16.sp
                ) {
                    AnimatedVisibility(
                        modifier = Modifier
                            .fillMaxHeight(),
                        visible = exercisesDoneIndex > index || success,
                        enter = fadeIn(
                            initialAlpha = 0f,
                            animationSpec = tween(
                                durationMillis = 800,
                            ),
                        ),
                        exit = fadeOut()
                    ) {
                        Row() {
                            Divider(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(1.dp)
                            )
                            Spacer(modifier = Modifier.width(15.dp))
                            Icon(Icons.Rounded.CheckCircle, "", Modifier.size(40.dp))
                        }
                    }
                }
            }
        }
    }
}