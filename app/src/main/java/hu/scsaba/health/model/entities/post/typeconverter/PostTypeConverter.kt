package hu.scsaba.health.model.entities.post.typeconverter

import com.google.firebase.firestore.QuerySnapshot
import hu.scsaba.health.model.entities.post.Post
import hu.scsaba.health.model.entities.post.TextPost
import hu.scsaba.health.model.entities.post.WorkoutPost

fun QuerySnapshot?.convertToPosts() : MutableList<Post>{
    val posts = mutableListOf<Post>()
    for (document in this!!.documents){
        when(document.data?.get("type")){
            PostTypes.WorkoutPost.name -> document.toObject(WorkoutPost::class.java).also {
                posts.add(it!!)
            }
            PostTypes.TextPost.name -> document.toObject(TextPost::class.java).also {
                posts.add(it!!)
            }
        }
    }
    return posts
}