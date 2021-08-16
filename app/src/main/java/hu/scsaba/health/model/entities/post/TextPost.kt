package hu.scsaba.health.model.entities.post

import androidx.compose.runtime.Composable
import com.google.firebase.Timestamp
import hu.scsaba.health.model.entities.post.typeconverter.PostTypes
import hu.scsaba.health.navigation.navArgs

data class TextPost(
    override val postId : String = "",
    override val authorId : String = "",
    override val type: PostTypes,
    override val text: String = "",
    override val date: Timestamp? = null,
    override val commentCount: Int = 0,
    override val username: String = "",
    override val comments: List<Comment> = listOf(),
) : Post {

    @Composable
    override fun DisplayContent(navigate : navArgs){

    }
}
