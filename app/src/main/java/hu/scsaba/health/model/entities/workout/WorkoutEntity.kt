package hu.scsaba.health.model.entities.workout

data class WorkoutEntity(
    val name : String = "",
    val exerciseList : List<ExerciseEntity>? = null,
    val rounds : Int = 0,
    val durationBetweenExercises : Int = 0,
    val durationBetweenRounds : Int = 0,
)

