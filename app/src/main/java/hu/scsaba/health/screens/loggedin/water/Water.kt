package hu.scsaba.health.screens.loggedin.water

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.RemoveCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import hu.scsaba.health.R
import hu.scsaba.health.navigation.navArgs
import hu.scsaba.health.ui.composables.ContentCardWithAction
import hu.scsaba.health.ui.composables.NavBase
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
@Composable
fun Water(backStackEntry: NavBackStackEntry, navigate: navArgs){

    val myViewModel: WaterViewModel = viewModel("water",
        HiltViewModelFactory(LocalContext.current,backStackEntry)
    )
    
    val waterIntake by myViewModel.waterIntake.asLiveData().observeAsState()
    val serviceState by myViewModel.serviceState.asLiveData().observeAsState(initial = false)

    NavBase(title = stringResource(id = R.string.water_intake)) {
        ContentCardWithAction(text = stringResource(id = R.string.add)) {
            Row(Modifier.wrapContentSize()) {
                Icon(imageVector = Icons.Rounded.RemoveCircle, contentDescription = "", modifier = Modifier
                    .size(43.dp)
                    //   .weight(1f)
                    //.padding(horizontal = 5.dp)
                    .clickable {
                        myViewModel.decrementWaterCount()
                    },
                    tint = MaterialTheme.colors.error
                )
                Spacer(modifier = Modifier.padding(horizontal = 12.dp))
                Icon(imageVector = Icons.Rounded.AddCircle, contentDescription = "", modifier = Modifier
                    .size(43.dp)
                    //  .weight(1f)

                    .clickable {
                        myViewModel.incrementWaterCount()
                    },
                    tint = MaterialTheme.colors.secondary
                )
            }
        }

        ContentCardWithAction(text = stringResource(id = R.string.notification)) {
            Switch(checked = serviceState, onCheckedChange = {
                when(it){
                    true -> {
                        myViewModel.startWaterForegroundService()
                            }
                    false->{
                        myViewModel.stopWaterForegroundService()
                    }
                }
            })
        }

        ContentCardWithAction(text = stringResource(id = R.string.drinks_today)) {
            if(waterIntake is WaterViewModel.WaterState.Loading){
                CircularProgressIndicator()
            }else{
                Row() {
                    Text(text = (waterIntake as WaterViewModel.WaterState.Success).waterEntity.count.toString(),fontSize = 22.sp)
                    Spacer(modifier = Modifier.padding(horizontal = 3.dp))

                    Icon(tint = Color.Blue,imageVector = Icons.Outlined.LocalDrink, contentDescription = "", modifier = Modifier
                        .size(32.dp)
                        .alpha(0.6f))

                    Text(text = " x 250ml",fontSize = 20.sp, modifier = Modifier.alpha(0.35f))

                }
            }
        }
        WaterChartViewBinding(
            dataSet = myViewModel.waterIntakeHistory,
            colors = MaterialTheme.colors
        ){
            myViewModel.observeWaterIntakeHistory()
        }
    }
}
