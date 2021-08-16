package hu.scsaba.health.model.entities.post

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.popUpTo
import com.google.firebase.Timestamp
import hu.scsaba.health.R
import hu.scsaba.health.model.entities.workout.WorkoutEntity
import hu.scsaba.health.model.entities.post.typeconverter.PostTypes
import hu.scsaba.health.navigation.navArgs
import hu.scsaba.health.utils.helper.Constants
import hu.scsaba.health.utils.helper.Constants.DESTINATION_DO_WORKOUT

data class WorkoutPost(
    override val postId : String = "",
    override val authorId : String = "",
    override val type: PostTypes? = null,
    override val text: String = "",
    override val date: Timestamp? = null,
    override val commentCount: Int = 0,
    override val username: String = "",
    override val comments: List<Comment> = listOf(),
    val content: WorkoutEntity = WorkoutEntity()
) : Post {

    @Composable
    override fun DisplayContent(
        navigate: navArgs
    ){

        var expanded by remember { mutableStateOf(false) }

        Text(text)
        Spacer(modifier = Modifier.height(15.dp))

        Card(modifier = Modifier.fillMaxWidth(), contentColor = MaterialTheme.colors.onSecondary) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(color = MaterialTheme.colors.secondary)
                    .fillMaxWidth()
                    .animateContentSize() // automatically animate size when it changes
                    .padding(top = 9.dp, start = 6.dp, end = 6.dp)
            ) {
                Text(
                    text = content.name,
                    textAlign = TextAlign.Center,
                    /*modifier = Modifier
                        .weight(1f)*/
                    fontWeight = FontWeight(500),
                    fontSize = 17.sp,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = content.rounds.toString() + " " + stringResource(R.string.rounds),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.subtitle1,
                    //modifier = Modifier.alpha(0.9f)
                )


                Crossfade(targetState = expanded) {
                    Column() {
                        if (it) {
                            content.exerciseList!!.forEach { exercise ->
                                val duration = String.format("%02d : %02d",exercise.durationMinutes,exercise.durationSeconds)
                                Row(Modifier.fillMaxWidth()) {
                                    Text(text = exercise.name, modifier = Modifier
                                        .weight(1f)
                                        .wrapContentWidth(Alignment.CenterHorizontally))
                                    Text(text = duration, modifier = Modifier
                                        .weight(1f)
                                        .wrapContentWidth(Alignment.CenterHorizontally))

                                }
                            }
                            Icon(
                                imageVector = Icons.Rounded.PlayCircle,
                                contentDescription = "",
                                modifier = Modifier
                                    .clickable {
                                        navigate("$DESTINATION_DO_WORKOUT/${content.name}/${authorId}"){
                                            popUpTo(Constants.DESTINATION_FEED) { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    }
                                    .size(50.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .fillMaxSize()
                                    .padding(5.dp),
                                tint = MaterialTheme.colors.onSecondary)
                            IconButton(onClick = { expanded = false }, modifier = Modifier.fillMaxWidth()) {
                                Icon(imageVector = Icons.Default.ExpandLess, contentDescription = "Expand less")
                            }
                        } else {
                            IconButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Icon(imageVector = Icons.Default.ExpandMore, contentDescription = "Expand more")
                            }
                        }
                    }
                }

            }
        }
    }
}
