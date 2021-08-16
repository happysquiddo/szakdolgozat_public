package hu.scsaba.health.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import hu.scsaba.health.R

@Composable
fun Attributions(){
    Column() {
        Text(text = stringResource(id = R.string.attributions) + ":")
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Sportbank Design on LottieFiles: https://lottiefiles.com/35875-confetti-on-transparent-background\n\n")
        Text(text = "Cooper Look Wai Hung Design on LottieFiles: https://lottiefiles.com/54092-success\n\n")
        Text(text = "Cooper Look Wai Hung Design on LottieFiles: https://lottiefiles.com/53041-empty-search-result\n\n")

    }
}