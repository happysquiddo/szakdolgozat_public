package hu.scsaba.health.screens.loggedin.workouts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.popUpTo
import hu.scsaba.health.R
import hu.scsaba.health.navigation.navArgs
import hu.scsaba.health.utils.helper.Constants
import hu.scsaba.health.utils.helper.Constants.DESTINATION_CREATE_WORKOUT
import hu.scsaba.health.utils.helper.Constants.DESTINATION_WORKOUT_LIST
import hu.scsaba.health.ui.composables.ContentCardWithAction
import hu.scsaba.health.ui.composables.NavBase

@ExperimentalAnimationApi
@Composable
fun Exercises(backStackEntry: NavBackStackEntry, navigate: navArgs){
    NavBase(title = stringResource(id = R.string.exercises)) {

        Spacer(modifier = Modifier.padding(vertical = 14.dp))

        Column(modifier = Modifier
            .fillMaxSize()) {
            AnimatedListVisibility(
                modifier = Modifier.weight(1f),
                listIndexFromOne = 1
            ) {
                ContentCardWithAction(text = stringResource(id = R.string.my_workouts),
                    textAlign = TextAlign.Left,
                    backgroundColor = MaterialTheme.colors.secondaryVariant,
                    elevation = 19.dp,
                    modifier = Modifier
                        .clickable {
                            navigate(DESTINATION_WORKOUT_LIST){
                                popUpTo(Constants.DESTINATION_EXERCISES) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                ) {
                    Icon(imageVector = Icons.Default.Bookmark, contentDescription = "")
                }
            }
            Spacer(modifier = Modifier.padding(vertical = 14.dp))

            AnimatedListVisibility(
                modifier = Modifier.weight(1f),
                listIndexFromOne = 2
            ) {
                ContentCardWithAction(text = stringResource(id = R.string.create_workout),
                    textAlign = TextAlign.Left,
                    backgroundColor = MaterialTheme.colors.secondaryVariant,
                    elevation = 4.dp,
                    modifier = Modifier
                        .clickable {
                            navigate(DESTINATION_CREATE_WORKOUT){
                                popUpTo(Constants.DESTINATION_EXERCISES) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "")
                }
            }
            Spacer(modifier = Modifier.padding(vertical = 14.dp))

        }
    }
}

@ExperimentalAnimationApi
@Composable
fun AnimatedListVisibility
            (modifier : Modifier = Modifier,
             listIndexFromOne : Int = 1,
             content : @Composable () -> Unit
) {

    AnimatedVisibility(modifier = modifier,
        visible = true,
        initiallyVisible = false,
        enter = slideInHorizontally(
            initialOffsetX = {-it*listIndexFromOne},
            animationSpec = tween(
                delayMillis = 15*listIndexFromOne,
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        )){
        content()
    }
}