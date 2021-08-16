package hu.scsaba.health.ui.composables

import android.widget.ImageView
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.compose.ui.zIndex
import hu.scsaba.health.R
import hu.scsaba.health.databinding.ConfettiBinding
import hu.scsaba.health.databinding.EmptyResultBinding
import hu.scsaba.health.databinding.SuccessBinding
import java.util.*

@Composable
fun EmptyResultAnimation() {

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        AndroidViewBinding(EmptyResultBinding::inflate, modifier = Modifier.weight(2f))
        Text(
            text = stringResource(id = R.string.nothing_to_see).toUpperCase(Locale.ROOT),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun RowScope.SuccessAnimation() {
    AndroidViewBinding(
        SuccessBinding::inflate,
        modifier = Modifier.size(47.dp).align(Alignment.CenterVertically).wrapContentSize(Alignment.CenterEnd).offset(x=10.dp)){
        this.animationView.scaleX = 1.8f
        this.animationView.scaleY = 1.8f
        this.animationView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        this.animationView.speed = 1.35f
    }
}

@Composable
fun ConfettiAnimation() {
    AndroidViewBinding(ConfettiBinding::inflate,modifier = Modifier.zIndex(100f)){
        this.animationView.scaleX = 1.3f
    }
}