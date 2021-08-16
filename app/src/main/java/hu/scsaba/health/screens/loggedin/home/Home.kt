package hu.scsaba.health.screens.loggedin.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.popUpTo
import hu.scsaba.health.R
import hu.scsaba.health.navigation.navArgs
import hu.scsaba.health.utils.helper.Constants
import hu.scsaba.health.utils.helper.Constants.DESTINATION_BREAKS
import hu.scsaba.health.utils.helper.Constants.DESTINATION_EXERCISES
import hu.scsaba.health.utils.helper.Constants.DESTINATION_WATER
import hu.scsaba.health.ui.composables.NavBase
import hu.scsaba.health.utils.helper.Constants.DESTINATION_ATTRIBUTIONS

@Composable
fun Home(backStackEntry: NavBackStackEntry, navigate: navArgs){
    
   Column() {
       NavBase(title = stringResource(id = R.string.home)) {

           Column() {
               CardSummary(icon = Icons.Rounded.DirectionsRun, text = stringResource(id = R.string.exercises)
               ) { navigate(DESTINATION_EXERCISES, navigateFromHome()) }
               CardSummary(icon = Icons.Rounded.FreeBreakfast, text = stringResource(id = R.string.schedule_breaks)
               ) { navigate(DESTINATION_BREAKS, navigateFromHome()) }
               CardSummary(icon = Icons.Rounded.LocalDrink, text = stringResource(id = R.string.water_intake)
               ) { navigate(DESTINATION_WATER, navigateFromHome()) }
           }
           Column(
               horizontalAlignment = Alignment.End,
               verticalArrangement = Arrangement.Bottom,
               modifier = Modifier
                   .fillMaxSize()
                   .offset(y = (-68).dp)) {
               Text(text = stringResource(id = R.string.attributions),
                   style = MaterialTheme.typography.subtitle1,
                   modifier = Modifier.clickable {
                       navigate(DESTINATION_ATTRIBUTIONS, navigateFromHome())
                   }
               )
           }

       }

   }
}
@Composable
fun CardSummary(icon : ImageVector, text : String, onClick : () -> Unit){
    Card(backgroundColor = MaterialTheme.colors.surface, elevation = 0.dp,
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.large)
            .clickable { onClick() }){
        Row(Modifier.padding(end = 11.dp, top = 25.dp,bottom = 25.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = "",
                tint = MaterialTheme.colors.secondary, modifier = Modifier
                    .size(32.dp)
                    .weight(1f))
            Text(text = text, textAlign = TextAlign.Left,
                modifier = Modifier
                    .weight(2f), fontWeight = FontWeight(500), fontSize = 22.sp, letterSpacing = 0.9.sp)
            Icon(imageVector = Icons.Rounded.ArrowForward, contentDescription = "",
                tint = MaterialTheme.colors.onSurface)

        }
    }
    Spacer(modifier = Modifier.padding(vertical = 9.dp))
}

private fun navigateFromHome(): NavOptionsBuilder.() -> Unit {
    return {
        popUpTo(Constants.DESTINATION_HOME) { inclusive = false }
        launchSingleTop = true
    }
}