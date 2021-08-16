package hu.scsaba.health.model.entities.post

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import hu.scsaba.health.model.entities.post.typeconverter.PostTypes
import hu.scsaba.health.navigation.navArgs

interface Post {
    val postId : String
    val authorId : String
    val type : PostTypes?
    val text : String
    val date: Timestamp?
    val commentCount : Int
    val username: String
    val comments : List<Comment>

    @Composable
    fun DisplayContent( navigate: navArgs )
}