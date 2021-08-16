package hu.scsaba.health.screens.loggedin.account.progress

import androidx.compose.foundation.layout.Box
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import hu.scsaba.health.R
import hu.scsaba.health.model.entities.progress.ProgressEntity
import hu.scsaba.health.navigation.navArgs
import hu.scsaba.health.ui.composables.Failure
import hu.scsaba.health.ui.composables.Loading
import hu.scsaba.health.ui.composables.NavBase
import java.util.*

@Composable
fun Progress(backStackEntry: NavBackStackEntry, uid : String, navigate : navArgs){

    val myViewModel: ProgressViewModel = viewModel("progress",
        HiltViewModelFactory(LocalContext.current,backStackEntry)
    )
    val progressState by myViewModel.progressState.asLiveData().observeAsState(initial = ProgressViewModel.ProgressState.Loading)

    DisposableEffect(Unit) {
        myViewModel.getProgress(uid)
        onDispose {  }
    }

    when(progressState){
        is ProgressViewModel.ProgressState.Loading -> Loading()
        is ProgressViewModel.ProgressState.Failure -> Failure()
        is ProgressViewModel.ProgressState.Success ->
            Success((progressState as ProgressViewModel.ProgressState.Success).result)
    }

}

@Composable
private fun Success(
    progress : List<ProgressEntity>
){
    NavBase(title = stringResource(id = R.string.progress).capitalize(Locale.ROOT), modifier = Modifier) {
        Box {
            ChartViewBinding(dataSet = progress, colors = MaterialTheme.colors)
        }
    }
}