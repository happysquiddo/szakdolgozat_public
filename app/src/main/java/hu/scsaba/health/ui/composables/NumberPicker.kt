package hu.scsaba.health.ui.composables

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import hu.scsaba.health.R
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun EditIntegerAttribute(
    text : String,
    valueState : MutableState<Int>,
    label : String = "",
    intRange : IntRange = IntRange(0,250)
){
    Row( modifier = Modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.large)
        .background(MaterialTheme.colors.surface)
        .padding(vertical = 10.dp, horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = text)
        NumberPicker(
            state = valueState,
            range = intRange,
            textStyle = TextStyle(fontFamily = FontFamily(Font(R.font.eczar_medium, FontWeight.Medium))),
            optionalLabel = label,
        )
    }
    Spacer(modifier = Modifier.height(25.dp))
}

/**
 * https://gist.github.com/vganin/a9a84653a9f48a2d669910fbd48e32d5
 * */
@Composable
fun NumberPicker(
    state: MutableState<Int>,
    modifier: Modifier = Modifier,
    range: IntRange? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    optionalLabel : String = ""
) {
    val coroutineScope = rememberCoroutineScope()
    val numbersColumnHeight = 50.dp
    val halvedNumbersColumnHeight = numbersColumnHeight / 2
    val halvedNumbersColumnHeightPx = with(LocalDensity.current) { halvedNumbersColumnHeight.toPx() }

    fun animatedStateValue(offset: Float): Int = state.value - (offset / halvedNumbersColumnHeightPx).toInt()

    val animatedOffset = remember { Animatable(0f) }.apply {
        if (range != null) {
            val offsetRange = remember(state.value, range) {
                val value = state.value
                val first = -(range.last - value) * halvedNumbersColumnHeightPx
                val last = -(range.first - value) * halvedNumbersColumnHeightPx
                first..last
            }
            updateBounds(offsetRange.start, offsetRange.endInclusive)
        }
    }
    val coercedAnimatedOffset = animatedOffset.value % halvedNumbersColumnHeightPx
    val animatedStateValue = animatedStateValue(animatedOffset.value)

    Column(
        modifier = modifier
            .wrapContentSize()
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { deltaY ->
                    coroutineScope.launch {
                        animatedOffset.snapTo(animatedOffset.value + deltaY)
                    }
                },
                onDragStopped = { velocity ->
                    coroutineScope.launch {
                        val endValue = animatedOffset.fling(
                            initialVelocity = velocity,
                            animationSpec = exponentialDecay(frictionMultiplier = 4f),
                            adjustTarget = { target ->
                                val coercedTarget = target % halvedNumbersColumnHeightPx
                                val coercedAnchors = listOf(
                                    -halvedNumbersColumnHeightPx,
                                    0f,
                                    halvedNumbersColumnHeightPx
                                )
                                val coercedPoint =
                                    coercedAnchors.minByOrNull { abs(it - coercedTarget) }!!
                                val base =
                                    halvedNumbersColumnHeightPx * (target / halvedNumbersColumnHeightPx).toInt()
                                coercedPoint + base
                            }
                        ).endState.value

                        state.value = animatedStateValue(endValue)
                        animatedOffset.snapTo(0f)
                    }
                }
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        val spacing = 10.dp

        Spacer(modifier = Modifier.height(spacing))

        Box(modifier = Modifier
            .align(Alignment.CenterHorizontally)) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(x = 0, y = coercedAnimatedOffset.roundToInt()) }
                    .align(Alignment.Center)
            ) {
                val baseLabelModifier = Modifier.align(Alignment.Center)
                ProvideTextStyle(textStyle) {
                    Label(
                        text = (animatedStateValue - 1).toString(),
                        modifier = baseLabelModifier
                            .offset(y = -halvedNumbersColumnHeight)
                            .alpha((coercedAnimatedOffset.coerceAtLeast(halvedNumbersColumnHeightPx / 8) / halvedNumbersColumnHeightPx))
                        //.alpha(((coercedAnimatedOffset+(halvedNumbersColumnHeightPx/12)) / (halvedNumbersColumnHeightPx+(halvedNumbersColumnHeightPx/12))))
                    )
                    Label(
                        text = animatedStateValue.toString(),
                        modifier = baseLabelModifier
                            .alpha((1 - abs(coercedAnimatedOffset) / halvedNumbersColumnHeightPx).coerceAtLeast(1f/8f))
                    )
                    Label(
                        text = (animatedStateValue + 1).toString(),
                        modifier = baseLabelModifier
                            .offset(y = halvedNumbersColumnHeight)
                            .alpha((((-coercedAnimatedOffset).coerceAtLeast(halvedNumbersColumnHeightPx / 8) / halvedNumbersColumnHeightPx)))
                    )
                }
            }
            Box(modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(y = (-1).dp, x = 5.dp)) {
                Text(text = optionalLabel, modifier = Modifier
                    .offset(x = 26.dp)
                    .align(Alignment.CenterEnd)
                    .zIndex(10f))
            }
        }


        Spacer(modifier = Modifier.height(spacing))
    }
}

@Composable
private fun Label(text: String, modifier: Modifier) {
    DisableSelection{
        Text(
            text = text,
            fontSize = 22.sp,
            modifier = modifier.pointerInput(Unit) {
                detectTapGestures()
            }
        )
    }

}

private suspend fun Animatable<Float, AnimationVector1D>.fling(
    initialVelocity: Float,
    animationSpec: DecayAnimationSpec<Float>,
    adjustTarget: ((Float) -> Float)?,
    block: (Animatable<Float, AnimationVector1D>.() -> Unit)? = null,
): AnimationResult<Float, AnimationVector1D> {
    val targetValue = animationSpec.calculateTargetValue(value, initialVelocity)
    val adjustedTarget = adjustTarget?.invoke(targetValue)

    return if (adjustedTarget != null) {
        animateTo(
            targetValue = adjustedTarget,
            initialVelocity = initialVelocity,
            block = block
        )
    } else {
        animateDecay(
            initialVelocity = initialVelocity,
            animationSpec = animationSpec,
            block = block,
        )
    }
}
@Preview
@Composable
fun PreviewNumberPicker() {
    Box(modifier = Modifier.fillMaxSize()) {
        NumberPicker(
            state = mutableStateOf(9),
            range = 0..10,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}