package hu.scsaba.health.model.entities.workout

data class ExerciseEntity(
    val name : String = "",
    val durationMinutes : Int = 0,
    val durationSeconds : Int = 0
)