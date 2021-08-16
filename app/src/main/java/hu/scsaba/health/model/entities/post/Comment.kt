package hu.scsaba.health.model.entities.post

import com.google.firebase.Timestamp

data class Comment(
    val text : String = "",
    val uid : String = "",
    val username : String = "",
    val date: Timestamp? = null,
)
