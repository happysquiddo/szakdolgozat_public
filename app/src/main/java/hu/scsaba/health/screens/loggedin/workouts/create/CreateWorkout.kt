package hu.scsaba.health.screens.loggedin.workouts.create

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.popUpTo
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.buttons
import com.vanpra.composematerialdialogs.input
import hu.scsaba.health.HealthApplication
import hu.scsaba.health.R
import hu.scsaba.health.model.entities.workout.ExerciseEntity
import hu.scsaba.health.model.entities.workout.WorkoutEntity
import hu.scsaba.health.navigation.navArgs
import hu.scsaba.health.utils.helper.Constants.DESTINATION_CREATE_WORKOUT
import hu.scsaba.health.utils.helper.Constants.DESTINATION_WORKOUT_LIST
import hu.scsaba.health.ui.composables.*
import hu.scsaba.health.utils.helper.isValidWorkoutName
import kotlinx.coroutines.launch

//@SuppressLint("RememberReturnType")
@ExperimentalAnimationApi
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CreateWorkout(backStackEntry: NavBackStackEntry, navigate: navArgs, navigateBack : () -> Unit) {

    val myViewModel: CreateWorkoutViewModel = viewModel("createWorkout",
        HiltViewModelFactory(LocalContext.current,backStackEntry)
    )


    val exerciseList by myViewModel.exerciseList.asLiveData().observeAsState()
    //single exercise
    var exerciseName by remember { mutableStateOf("") }
    val secondState = remember{ mutableStateOf(0)}
    val minuteState = remember{ mutableStateOf(0)}
    //workout
    val workoutName by myViewModel.workoutName.observeAsState("")
    //var workoutName by remember{ mutableStateOf("")}
    val durationBetweenExercises = remember{ mutableStateOf(0)}
    val durationBetweenRounds = remember{ mutableStateOf(0)}
    val rounds = remember{ mutableStateOf(1)}

    var isAddingExercise by remember{ mutableStateOf(false)}
    var isSettingsOpen by remember{ mutableStateOf(false)}
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val saveWorkoutState by myViewModel.saveWorkoutState.asLiveData().observeAsState(
        CreateWorkoutViewModel.SaveWorkoutState.Idle)



    val dialog = remember {
        MaterialDialog(onCloseRequest = {
            if(workoutName.isValidWorkoutName())it.hide()
        })
    }
    dialog.build {
        this.enablePositiveButton()
        this.isAutoDismiss()
        input(
            label = stringResource(id = R.string.name_your_workout),
            hint = stringResource(id = R.string.minimum_3_characters),
            isTextValid = { it.isValidWorkoutName() }
        ) { inputString ->
            myViewModel.workoutName.value = inputString
        }
        buttons {
            positiveButton("Ok")
            negativeButton(res = R.string.cancel){
                if(workoutName.isValidWorkoutName()){
                    hide()
                }else{
                    navigateBack()
                }
            }
        }
    }
    if(workoutName.length < 3) dialog.show()

    NavBase(title = stringResource(id = R.string.create_workout), modifier = Modifier.padding(4.dp)) {

        Column(modifier = Modifier.animateContentSize()) {
            Box(Modifier.fillMaxWidth()) {
                Text(text = workoutName, textAlign = TextAlign.Center, modifier = Modifier.align(
                    Alignment.Center))
                Icon(imageVector = Icons.Default.Edit, contentDescription = "", modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.CenterEnd)
                    .clickable {
                        coroutineScope.launch {
                            dialog.show()
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.padding(vertical = 5.dp))
            Row() {
                ButtonWithIcon(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AddCircleOutline,
                    text = stringResource(id = R.string.add_exercise)
                ) {
                    isAddingExercise = true
                    isSettingsOpen = false
                }

                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                ButtonWithIcon(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Settings,
                    text = stringResource(id = R.string.settings)
                ) {
                    isAddingExercise = false
                    isSettingsOpen = true
                }
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                ButtonWithIcon(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Save,
                    text = stringResource(id = R.string.save),
                    loading = saveWorkoutState is CreateWorkoutViewModel.SaveWorkoutState.Loading
                ) {
                    if(exerciseList!!.isNotEmpty()){
                        coroutineScope.launch {
                            val job = coroutineScope.launch {
                                myViewModel.saveWorkout(
                                    WorkoutEntity(
                                    name = workoutName,
                                    exerciseList = exerciseList!!.toList(),
                                    rounds = rounds.value,
                                    durationBetweenExercises = durationBetweenExercises.value,
                                    durationBetweenRounds = durationBetweenRounds.value
                                )
                                )
                            }
                            job.join()
                            if (saveWorkoutState is CreateWorkoutViewModel.SaveWorkoutState.Failure){
                                coroutineScope.launch {
                                    it.snackbarHostState.showSnackbar(
                                            (saveWorkoutState as CreateWorkoutViewModel.SaveWorkoutState.Failure).message)
                                }
                            }else if(saveWorkoutState is CreateWorkoutViewModel.SaveWorkoutState.Success){
                                navigate(DESTINATION_WORKOUT_LIST){
                                    popUpTo(DESTINATION_CREATE_WORKOUT) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }

                    }else coroutineScope.launch {
                        it.snackbarHostState.showSnackbar(HealthApplication.Strings.get(R.string.no_exercises))
                    }
                }
            }
            Box() {
                androidx.compose.animation.AnimatedVisibility(visible = isAddingExercise,
                    enter = expandVertically(expandFrom = Alignment.Top),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top),
                    modifier = Modifier.zIndex(100f)) {
                    AddExerciseRow(
                        save = {
                            if(secondState.value > 0 || minuteState.value > 0){
                                myViewModel.addExercise(ExerciseEntity(exerciseName, minuteState.value,secondState.value))
                                isAddingExercise = false
                            }else{
                                coroutineScope.launch {
                                    it.snackbarHostState.showSnackbar(HealthApplication.Strings.get(R.string.too_short))
                                }
                            }
                        },
                        exerciseName = exerciseName,
                        onNameChange = { if(it.length<=15)exerciseName = it } ,
                        secondState = secondState,
                        minuteState = minuteState,
                        scrollToBottom = {
                            exerciseList?.let{
                                if( it.isNotEmpty()){
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(it.lastIndex)
                                    }
                                }
                            }

                        }
                    )
                }
                androidx.compose.animation.AnimatedVisibility(visible = isSettingsOpen,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(100f)) {
                    IntervalSettingsRow(
                        save = {
                            isSettingsOpen = false
                        },
                        durationBetweenExercises,
                        durationBetweenRounds,
                        rounds
                    )
                }
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    state = listState,
                ) {
                    exerciseList?.let { list ->

                        itemsIndexed(list){ index, item ->
                            androidx.compose.animation.AnimatedVisibility(visible = true,
                                enter = fadeIn() + expandHorizontally(animationSpec = spring(
                                    Spring.DampingRatioLowBouncy,
                                    1200f)),
                                initiallyVisible = false,
                                modifier = Modifier.fillMaxWidth()) {
                                ContentCardWithAction(text = item.name, fontSize = 17.sp) {
                                    //Row(Modifier.align(Alignment.Center)) {
                                        Text(text = "${item.durationMinutes} minutes", modifier = Modifier.align(CenterVertically))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = "${item.durationSeconds} seconds", modifier = Modifier.align(CenterVertically))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "", modifier = Modifier
                                            .clickable {
                                                list.removeAt(index)
                                            }
                                            .size(30.dp)
                                            .align(Alignment.CenterVertically),
                                            tint = MaterialTheme.colors.error)
                                    //}
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntervalSettingsRow(
    save :() -> Unit,
    intervalBetweenExercises : MutableState<Int>,
    intervalBetweenRounds : MutableState<Int>,
    numberOfRounds : MutableState<Int>
){
    Column() {
        SettingsRow(Modifier.padding(start = 16.dp)) {
            Text(text = stringResource(id = R.string.interval_exercises), textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(2f), fontWeight = FontWeight(500), fontSize = 17.sp, letterSpacing = 0.8.sp)
            NumberPicker(state = intervalBetweenExercises,
                range = IntRange(0,50),
                textStyle = TextStyle(fontFamily = FontFamily(Font(R.font.eczar_medium, FontWeight.Medium))),
                optionalLabel = "s",
                modifier = Modifier.weight(1f)
            )

        }
        Spacer(modifier = Modifier.height(2.dp))
        SettingsRow(modifier = Modifier.padding(start = 16.dp)) {
            Text(text = stringResource(id = R.string.interval_rounds), textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(2f), fontWeight = FontWeight(500), fontSize = 17.sp, letterSpacing = 0.8.sp)
            NumberPicker(state = intervalBetweenRounds,
                range = IntRange(0,50),
                textStyle = TextStyle(fontFamily = FontFamily(Font(R.font.eczar_medium, FontWeight.Medium))),
                optionalLabel = "s",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        SettingsRow(Modifier.padding(start = 16.dp)) {
            Text(text = stringResource(id = R.string.number_of_rounds), textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(2f), fontWeight = FontWeight(500), fontSize = 17.sp, letterSpacing = 0.8.sp)
            NumberPicker(
                state = numberOfRounds,
                range = IntRange(1,50),
                textStyle = TextStyle(fontFamily = FontFamily(Font(R.font.eczar_medium, FontWeight.Medium))),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        ButtonWithIcon(icon = Icons.Default.Check, text = stringResource(id = R.string.save)) {
            save()
        }
    }
}

@Composable
private fun AddExerciseRow(save :() -> Unit,
                           exerciseName : String,
                           onNameChange: (String) -> Unit,
                           secondState : MutableState<Int>,
                           minuteState : MutableState<Int>,
                           scrollToBottom : () -> Unit){
        SettingsRow {
            OutlinedTextField(
                value = exerciseName,
                onValueChange = onNameChange,
                label = { Text(stringResource(id = R.string.name)) },
                modifier = Modifier
                    .weight(2f)
                    .align(Alignment.CenterVertically)
            )
            NumberPicker(range = IntRange(0,60),
                textStyle = TextStyle(fontFamily = FontFamily(Font(R.font.eczar_medium, FontWeight.Medium))),
                optionalLabel = "m",
                state = minuteState,
                modifier = Modifier.weight(1f))
            NumberPicker(range = IntRange(0,60),
                textStyle = TextStyle(fontFamily = FontFamily(Font(R.font.eczar_medium, FontWeight.Medium))),
                optionalLabel = "s",
                state = secondState,
                modifier = Modifier.weight(1f)
            )
            Icon(imageVector = Icons.Default.Check, contentDescription = "", modifier = Modifier
                .clickable {
                    scrollToBottom.invoke()
                    save.invoke()
                }
                .padding(5.dp)
                .size(30.dp),
                tint = MaterialTheme.colors.primary)
        }


}