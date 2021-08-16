package hu.scsaba.health.model.entities.user

data class User(
    val username : String = "",
    val uid : String = "",
    val email : String = "",
    val heightInCm : Int? = null,
    val weightInKg : Int? = null,
    val age : Int? = null,
    )
