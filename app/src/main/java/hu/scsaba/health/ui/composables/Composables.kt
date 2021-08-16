package hu.scsaba.health.ui.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.isFocused
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.scsaba.health.R
import hu.scsaba.health.ui.theme.HealthTheme
import java.util.*

@Composable
fun Loading(modifier: Modifier = Modifier){
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(id = R.string.loading), fontSize = 24.sp, style = MaterialTheme.typography.subtitle1)
        Spacer(modifier = Modifier.height(15.dp))
        CircularProgressIndicator()
    }

}
@Composable
fun Failure(modifier: Modifier = Modifier,text : String = stringResource(id = R.string.something_wrong), emoji : String = "\uD83D\uDC94"){
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = text, fontSize = 24.sp, style = MaterialTheme.typography.subtitle1)
        Spacer(modifier = Modifier.height(15.dp))
        Text(text = emoji, fontSize = 25.sp)
    }
}

@Composable
fun NavBase(title : String,
            modifier: Modifier = Modifier.padding(4.dp),
            appBarColor : Color = Color.Transparent,
            content : @Composable (ScaffoldState) -> Unit
){
    val scaffoldState = rememberScaffoldState()
    val verticalGradientBrush = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colors.primary,
                MaterialTheme.colors.secondary,
                MaterialTheme.colors.secondary.copy(alpha = 0.7f),
                MaterialTheme.colors.secondary.copy(alpha = 0.5f),
                MaterialTheme.colors.secondary.copy(alpha = 0.4f),
                MaterialTheme.colors.secondary.copy(alpha = 0.25f),
                MaterialTheme.colors.secondary.copy(alpha = 0.1f),
                MaterialTheme.colors.background,
                ),
        endY = 415f
        )

    Column(
        Modifier
            .fillMaxSize()
            .background(verticalGradientBrush)) {
        Scaffold(
            backgroundColor = Color.Transparent,
            snackbarHost = { SnackbarHost(
                hostState = scaffoldState.snackbarHostState)
                           },
            topBar = {
                TopAppBar(
                    elevation = 0.dp,
                    backgroundColor = appBarColor
                ) {
                    Text(modifier = Modifier.fillMaxWidth(), text = title.toUpperCase(Locale.ROOT),
                        color = MaterialTheme.colors.onPrimary,
                        textAlign = TextAlign.Center, style = MaterialTheme.typography.h1)
                }
            }
        ) {
            Column(modifier) {
                content(scaffoldState)
            }
        }
    }
}

@Composable
fun SettingsRow(modifier : Modifier = Modifier,content : @Composable RowScope.() -> Unit){
    Card(backgroundColor = MaterialTheme.colors.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ){
        Row(
            modifier
                .padding(4.dp)
            , verticalAlignment = Alignment.CenterVertically) {
            content()
        }
    }
}

@Composable
fun ContentCardWithAction(modifier: Modifier = Modifier,
                          text : String,
                          subText : String = "",
                          textAlign: TextAlign = TextAlign.Left,
                          fontSize: TextUnit = 21.sp,
                          elevation : Dp = 0.dp,
                          bottomSpacer : Dp = 9.dp,
                          backgroundColor : Color = MaterialTheme.colors.surface,
                          action : @Composable RowScope.()->Unit){
    Card(backgroundColor = backgroundColor, elevation = elevation,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(12.dp))
    ){
        Row(
            Modifier
                .padding(start = 20.dp, end = 20.dp, top = 21.dp, bottom = 21.dp)
                /*.height(IntrinsicSize.Max)*/, verticalAlignment = Alignment.CenterVertically) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                .weight(1f)) {
                Text(
                    text = text,
                    textAlign = textAlign,
                    /*modifier = Modifier
                        .weight(1f)*/
                    fontWeight = FontWeight(500),
                    fontSize = fontSize,
                    letterSpacing = 0.8.sp
                )
                if(subText.isNotBlank())Text(
                    text = subText,

                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.alpha(0.9f)
                )

            }
            action()
        }
    }
    Spacer(modifier = Modifier.padding(vertical = bottomSpacer))
}


@Composable
fun MyTextField(modifier : Modifier = Modifier,
                icon : ImageVector,
                value : TextFieldValue,
                onValueChanged : (TextFieldValue) -> Unit,
                label : String,
                keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                visualTransformation: VisualTransformation = VisualTransformation.None, isError : Boolean = false,
                errorMessage : String = "",
                findError : () -> Unit = {},
                singleLine : Boolean = true
)
{

    var firstFocusHappened = remember { mutableStateOf(false)}
    OutlinedTextField(
        isError = isError,
        value = value,
        onValueChange = {
                onValueChanged(it)
                if(isError) findError()
            },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            unfocusedBorderColor = LocalContentColor.current.copy(LocalContentAlpha.current).copy(
                ContentAlpha.high),
            unfocusedLabelColor = LocalContentColor.current.copy(LocalContentAlpha.current).copy(
                ContentAlpha.high)
        ),
        label = { Text(text = label) },
        leadingIcon = { Icon(icon,"",Modifier.size(20.dp)) },
        modifier = modifier.onFocusChanged { state ->
            if(state.isFocused){
                firstFocusHappened.value = true
            }
            if(state == FocusState.Inactive && firstFocusHappened.value) findError()
        },
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        singleLine = singleLine
    )
    if (isError){
        TextFieldError(message = errorMessage)
    }

}

@Composable
fun TextFieldError(message : String){
    Text(text = message, textAlign = TextAlign.End, fontSize = 11.sp,
        modifier = Modifier.padding(start = 16.dp), color = MaterialTheme.colors.error.copy(alpha = 0.65f))
}

@Composable
fun DividerWithText(text : String){
    Row(modifier = Modifier.padding(start = 2.dp, end = 2.dp, top = 22.dp, bottom = 22.dp)) {
        Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier
            .weight(1f)
            .padding(top = 7.dp))
        Text(text = text,
            modifier = Modifier
                .padding(start = 1.dp, end = 1.dp)
                .weight(1f),
            fontSize = 11.sp, textAlign = TextAlign.Center, letterSpacing = 1.sp)
        Divider(color = Color.LightGray, thickness = 1.dp,modifier = Modifier
            .weight(1f)
            .padding(top = 7.dp))
    }
}

@Composable
fun CircleAvatar(text : String,fontSize: TextUnit = 16.sp,padding : Dp = 15.dp, modifier: Modifier = Modifier){
    Column(modifier = modifier
        .wrapContentSize(Alignment.CenterStart)
        .clip(shape = CircleShape)
        .animateContentSize()
        .background(Color.Transparent)
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .background(color = MaterialTheme.colors.secondaryVariant)
                .clip(shape = CircleShape)
        ) {
            Text(
                text = text.take(2).toUpperCase(Locale.ROOT),
                modifier = Modifier.padding(/*horizontal = 12.dp, vertical = 13.dp*/padding),
                textAlign = TextAlign.Center,color = MaterialTheme.colors.onSecondary,
                fontWeight = FontWeight(800),
                letterSpacing = 2.5.sp,
                fontSize = fontSize
            )
        }
    }
}

@Composable
fun ButtonWithIcon(
modifier : Modifier = Modifier,
loading : Boolean = false,
enabled : Boolean = true,
backgroundColor : Color = MaterialTheme.colors.secondary,
icon : ImageVector,
text : String,
onClick : () -> Unit
) {
    Button(
        colors = ButtonDefaults.buttonColors(backgroundColor = backgroundColor),
        elevation = ButtonDefaults.elevation(1.dp),
        enabled = enabled,
        onClick = {
            onClick()
        },
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        contentPadding = PaddingValues(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = "", tint = MaterialTheme.colors.onSecondary)
        if(loading) CircularProgressIndicator(color = MaterialTheme.colors.onSecondary, modifier = Modifier.size(20.dp))
        else Text(text = text, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)

    }
}

@Composable
fun AnimatedCircle(
    modifier: Modifier = Modifier,
    color : Color = MaterialTheme.colors.secondary,
    remainingTime : Long,
    selectedTime : Long,
    state : Boolean
) {
    val currentState = remember {
        MutableTransitionState(AnimatedCircleProgress.START)
            .apply { targetState = AnimatedCircleProgress.END }
    }
    val stroke = with(LocalDensity.current) { Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round) }
    val transition = updateTransition(currentState, label = "")
    val angleOffset by transition.animateFloat(
        transitionSpec = {
            tween(
                delayMillis = 100,
                durationMillis = 900,
                easing = FastOutSlowInEasing
            )
        }, label = ""
    ) { progress ->
        if (progress == AnimatedCircleProgress.START) {
            0f
        } else {
            if(state)360f * (remainingTime.toFloat() / (selectedTime.toFloat()))
            else 360f

        }
    }
    val shift by transition.animateFloat(
        transitionSpec = {
            tween(
                delayMillis = 100,
                durationMillis = 900,
                easing = CubicBezierEasing(0.28f, 0.8f, 0.63f, 0.83f)
            )
        }, label = ""
    ) { progress ->
        if (progress == AnimatedCircleProgress.START) {
            -45f
        } else {
            0f
        }
    }
    val alpha by transition.animateFloat(
        transitionSpec = {
            tween(
                delayMillis = 100,
                durationMillis = 900,
                easing = LinearEasing
            )
        }, label = ""
    ) { progress ->
        if (progress == AnimatedCircleProgress.START) {
            0.7f
        } else {
            1f
        }
    }

    Canvas(modifier.alpha(alpha = alpha)) {
        val innerRadius = (size.minDimension - stroke.width)/2
        val halfSize = size / 2.0f
        val topLeft = Offset(
            halfSize.width - innerRadius,
            halfSize.height - innerRadius
        )
        val size = Size(innerRadius * 2, innerRadius * 2)
        val startAngle = shift - 90f
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = angleOffset,
            topLeft = topLeft,
            size = size,
            useCenter = false,
            style = stroke
        )
    }
}
private enum class AnimatedCircleProgress { START, END }
@Composable
fun AnimatedCircleText(text : String, modifier: Modifier){
    Text(text = text.toUpperCase(Locale.ROOT), style = MaterialTheme.typography.h2, modifier = modifier)
}

@Preview
@Composable
fun Preview(){
        HealthTheme {
            Box(Modifier.background(Color.White)) {
                AnimatedCircle(
                    remainingTime = 110000,
                    selectedTime = 20,
                    state = true,
                    modifier = Modifier
                        .height(210.dp)
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                )
            }
        }
}