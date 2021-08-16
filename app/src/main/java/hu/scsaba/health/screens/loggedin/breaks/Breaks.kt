package hu.scsaba.health.screens.loggedin.breaks

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import hu.scsaba.health.HealthApplication
import hu.scsaba.health.R
import hu.scsaba.health.navigation.navArgs
import hu.scsaba.health.ui.composables.AnimatedCircle
import hu.scsaba.health.ui.composables.AnimatedCircleText
import hu.scsaba.health.ui.composables.ContentCardWithAction
import hu.scsaba.health.ui.composables.NavBase
import hu.scsaba.health.utils.helper.formatTime

@ExperimentalAnimationApi
@Composable
fun Breaks(backStackEntry: NavBackStackEntry, navigate: navArgs){
    val myViewModel: BreaksViewModel = viewModel("breaks",
        HiltViewModelFactory(LocalContext.current,backStackEntry)
    )

    val timeLeftUntilBreakInMillis by myViewModel.timeLeftUntilBreak.asLiveData().observeAsState(0L)
    val remainingTime by remember{
        derivedStateOf {
            formatTime(timeLeftUntilBreakInMillis)
        }
    }
    val breakState by myViewModel.breakState.asLiveData().observeAsState(false)

    val activeDurationValueInHours by myViewModel.durationInHours.asLiveData().observeAsState(0)
    val activeIntervalValueInMillis by myViewModel.interval.asLiveData().observeAsState(0)

    val defaultIntervalValue = remember {10}
    val defaultDurationValue = remember {1}
    var selectedDurationValueInHours by remember { mutableStateOf(defaultDurationValue) }
    var selectedIntervalValueInMinutes by remember { mutableStateOf(defaultIntervalValue) }
    var selectedDurationIndex by remember { mutableStateOf(0) }
    var selectedIntervalIndex by remember { mutableStateOf(0) }
    val durationItems by remember {
        derivedStateOf {
            mutableListOf<String>().also { items ->
                for(i in defaultDurationValue-1..11)
                    items.add(
                        "${i+1} ${if (i==0)  HealthApplication.Strings.get(R.string.hour) 
                        else HealthApplication.Strings.get(R.string.hours)}"
                    )
            }
        }
    }
    val intervalItems by remember(selectedDurationValueInHours) {
        derivedStateOf {
            val maxSelectableInterval = selectedDurationValueInHours*60-1
            mutableListOf<String>().also { items ->
                for(i in defaultIntervalValue-1..maxSelectableInterval step 10) items.add(
                    if (i+1 < 60){
                        "${i+1} ${HealthApplication.Strings.get(R.string.minutes)}"
                    }else{
                        "${(i+1)/60} ${HealthApplication.Strings.get(R.string.hours)}" +
                                if ((i+1)%60 > 0) " ${(i+1)%60} minutes" else ""
                    }
                )
            }
        }
    }


    NavBase(title = stringResource(id = R.string.schedule_breaks)) {
        Column(modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()) {
            Box(Modifier.padding(16.dp)) {
                AnimatedCircle(
                    modifier = Modifier
                        .height(210.dp)
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    remainingTime = timeLeftUntilBreakInMillis,
                    selectedTime = activeIntervalValueInMillis.toLong(),
                    state = breakState
                )
                if(!breakState) AnimatedCircleText(text = stringResource(id = R.string.off), modifier = Modifier.align(
                    Alignment.Center) )
                else {
                    AnimatedCircleText(text = remainingTime, modifier = Modifier.align(
                        Alignment.Center) )
                    Text(text = stringResource(id = R.string.until_next_break), style = MaterialTheme.typography.h3, modifier = Modifier
                        .align(
                            Alignment.Center
                        )
                        .offset(y = 35.dp))
                }

            }

            ContentCardWithAction(text = stringResource(id = R.string.start)) {
                Switch(checked = breakState, onCheckedChange = {
                    when(it){
                        true -> {
                            myViewModel.startWorking(
                                selectedDurationValueInHours,
                                selectedIntervalValueInMinutes,
                                SystemClock.elapsedRealtime())
                        }
                        false->{
                            myViewModel.stopWorking()
                        }
                    }
                })
            }
            AnimatedVisibility(visible = !breakState) {
                Column() {
                    IntervalSelector (
                        stringResource(id = R.string.duration),
                        selectedDurationIndex,
                        durationItems,
                        { index -> selectedDurationIndex = index},
                        { value -> selectedDurationValueInHours = value + 1 }
                    )
                    IntervalSelector (
                        stringResource(id = R.string.break_interval),
                        selectedIntervalIndex,
                        intervalItems,
                        { index -> selectedIntervalIndex = index},
                        { value -> selectedIntervalValueInMinutes = (value+1)*10 }
                    )
                }
            }

            AnimatedVisibility(visible = breakState) {
                Column(
                    Modifier
                        .background(
                            color = MaterialTheme.colors.primary,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .clip(
                            RoundedCornerShape(2.dp)
                        )) {
                    ContentCardWithAction(text = stringResource(id = R.string.selected_work_duration,),backgroundColor = MaterialTheme.colors.primary) {
                        Text(text = formatTime(activeDurationValueInHours.toLong()*60*60*1000),style = MaterialTheme.typography.subtitle1, fontSize = 18.sp)
                    }
                    ContentCardWithAction(text = stringResource(id = R.string.selected_break_interval),backgroundColor = MaterialTheme.colors.primary) {
                        Text(text = formatTime(activeIntervalValueInMillis.toLong()),style = MaterialTheme.typography.subtitle1, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun IntervalSelector(
    text : String,
    selectedIndex : Int,
    items : List<String>,
    indexSelected : (Int) -> Unit,
    intervalSelected : (Int) -> Unit
){
    ContentCardWithAction(text = text) {
        var expanded by remember { mutableStateOf(false) }
        BreakDropDown(
            text = items[selectedIndex],
            items = items,
            expanded = { expanded },
            onExpand = { expanded = true },
            onClose = { expanded = false },
        ) { index, string ->
            intervalSelected(index)
            indexSelected(index)
            expanded = false
        }
    }
}

@Composable
private fun BreakDropDown(text : String,
                          items : List<String>,
                          expanded : () -> Boolean,
                          onExpand : () -> Unit,
                          onClose : () -> Unit,
                          onItemSelected : (index : Int, string : String)->Unit){

    Card(backgroundColor = MaterialTheme.colors.secondary, elevation = 5.dp,
        modifier = Modifier
            .wrapContentSize()
            .clickable(onClick = onExpand)
            .clip(shape = RoundedCornerShape(4.dp))) {
            Text(text, Modifier.padding(15.dp, 5.dp))
    }
    DropdownMenu(
        expanded = expanded(),
        onDismissRequest = onClose,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colors.surface
            )
    ) {
        items.forEachIndexed { index, s ->
            DropdownMenuItem(onClick = {onItemSelected(index,s)}) {
                Text(text = s)
            }
        }
    }
}

