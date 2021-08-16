package hu.scsaba.health.services

import com.google.firebase.Timestamp
import hu.scsaba.health.R
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.getField
import hu.scsaba.health.HealthApplication
import hu.scsaba.health.model.entities.water.WaterEntity
import hu.scsaba.health.model.entities.workout.WorkoutEntity
import hu.scsaba.health.model.entities.post.Comment
import hu.scsaba.health.model.entities.post.WorkoutPost
import hu.scsaba.health.model.entities.post.typeconverter.PostTypes
import hu.scsaba.health.utils.exception.CustomException
import hu.scsaba.health.utils.helper.ErrorHandling
import hu.scsaba.health.utils.helper.ResultWrapper
import hu.scsaba.health.utils.session.SessionManager
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.flow.*
import java.lang.Exception
import java.time.ZonedDateTime
import java.time.temporal.IsoFields

class Firestore @Inject constructor(
    private val firestore : FirebaseFirestore,
    private val sessionManager: SessionManager
    ){

    suspend fun searchForUsernameDuplicate(username: String): ResultWrapper<QuerySnapshot> {
        val ref = firestore.collection("users")
        val equalUsernameRRef = ref.limit(1L).whereEqualTo("username", username)
        val usernameMatch =  ErrorHandling.taskErrorHandler { equalUsernameRRef.get() }
        return if (usernameMatch is ResultWrapper.Success) {
            usernameMatch
        } else ResultWrapper.Failure(HealthApplication.Strings.get(R.string.something_wrong))
    }

    private suspend fun getUsernameById(userId : String) : String{
        return ErrorHandling.taskErrorHandler {
            firestore.collection("users").document(userId).get()
        }.let {
            return@let if(it is ResultWrapper.Success) it.value!!.getField<String>("username")!!
            else ""
        }
    }

    suspend fun editUserData(weight : Int, height : Int, age : Int) : ResultWrapper<Void>{
        val userId = withContext(Dispatchers.IO){
            sessionManager.fetchUserId().first()
        }
        val ref = firestore.collection("users").document(userId)
        return ErrorHandling.taskErrorHandler {
            ref.set(mapOf(
                "weightInKg" to weight,
                "heightInCm" to height,
                "age" to age
            ),SetOptions.merge())
        }
    }

    suspend fun getWorkoutList(): Flow<ResultWrapper<QuerySnapshot>> {
        val userId = withContext(Dispatchers.IO){
            sessionManager.fetchUserId().first()
        }
        val ref = firestore.collection("users").document(userId).collection("workouts")
        return ErrorHandling.taskErrorHandlerFlow {
            ref.get()
        }
    }
    suspend fun getWorkout(workoutName : String, uid : String): Flow<ResultWrapper<DocumentSnapshot>> {
        val ref = firestore.collection("users").document(uid)
            .collection("workouts").document(workoutName)
        return ErrorHandling.taskErrorHandlerFlow {
            ref.get()
        }
    }

    suspend fun saveWorkout(workoutEntity: WorkoutEntity) : ResultWrapper<Void>{
        val userId = withContext(Dispatchers.IO){
            sessionManager.fetchUserId().first()
        }
        val ref = firestore.collection("users").document(userId).collection("workouts")
        val equalNameRef = ref.limit(1L).whereEqualTo(FieldPath.documentId(),workoutEntity.name)
        val nameMatch = ErrorHandling.taskErrorHandler { equalNameRef.get() }
        return if (nameMatch is ResultWrapper.Success) {
            ErrorHandling.taskErrorHandler {
                if (!nameMatch.value!!.isEmpty) {
                    throw CustomException(HealthApplication.Strings.get(R.string.name_exists))
                }
                ref.document(workoutEntity.name).set(workoutEntity)
            }
        } else ResultWrapper.Failure(HealthApplication.Strings.get(R.string.something_wrong))
    }

    suspend fun saveWorkoutProgress(): ResultWrapper<Void> {
        val userId = withContext(Dispatchers.IO){
            sessionManager.fetchUserId().first()
        }
        val ref = firestore.collection("users")
            .document(userId).collection("progress")
        val weekOfYear = ZonedDateTime.now().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        val increment = FieldValue.increment(1)
        val query = ref.limit(1).whereEqualTo("weekOfYear", weekOfYear)
        val currentWeekReference = try {
             withContext(Dispatchers.IO){
                ErrorHandling.taskErrorHandler {
                    query.get()
                }.run {
                    when(this){
                        is ResultWrapper.Success -> this.value!!
                        else -> throw CustomException(HealthApplication.Strings.get(R.string.something_wrong))
                    }
                }
            }
        }catch (e : Exception){
            return ResultWrapper.Failure(e.message)
        }
        return ErrorHandling.taskErrorHandler {
            if(currentWeekReference.isEmpty){
                ref.document().set(mapOf(
                    "weekOfYear" to weekOfYear,
                    "workoutsDone" to increment
                ))
            }else{
                ref.document(currentWeekReference.documents[0].id).update("workoutsDone",increment)
            }
        }
    }

    suspend fun postWorkout(workoutEntity: WorkoutEntity, postText : String) : ResultWrapper<Void>{
        val userId = withContext(Dispatchers.IO){
            sessionManager.fetchUserId().first()
        }
        val postsRef =
            firestore.collection("posts").document()
        val documentId = postsRef.id

        val userPostsRef =
            firestore.collection("users")
                .document(userId)
                .collection("posts")
                .document(documentId)

        val username = withContext(Dispatchers.IO){
            getUsernameById(userId)
        }
        val post = WorkoutPost(documentId,userId,PostTypes.WorkoutPost,postText,Timestamp.now(),0,username, listOf(),workoutEntity)
        val result = ErrorHandling.taskErrorHandler { postsRef.set(post) }
        if( result is ResultWrapper.Success){
            return ErrorHandling.taskErrorHandler {
                userPostsRef.set(post)
            }
        }
        return ResultWrapper.Failure(HealthApplication.Strings.get(R.string.something_wrong))
    }


    /**
     * Id-vel meghatározott poszt alá komment feltöltése Firestore-ba
     * @param text a komment szövege
     * @param username a komment szerzője
     * @param uid a komment szerzőjének id-je
     * @param postAuthorId az eredeti poszt szerzőjének id-je.
     * @param postId a poszt id-je
     */
    suspend fun postComment(text : String, commenterId : String, postAuthorId : String, postId : String): ResultWrapper<Void> {
        val userId = withContext(Dispatchers.IO){
            sessionManager.fetchUserId().first()
        }
        val postRef =
            firestore.collection("posts").document(postId)

        val userPostRef =
            firestore.collection("users")
                .document(postAuthorId)
                .collection("posts")
                .document(postId)
        val commentRef =
            firestore.collection("posts")
                .document(postId)
                .collection("comments")
                .document()
        val username = withContext(Dispatchers.IO){
            getUsernameById(userId)
        }
        val comment = Comment(text,commenterId, username, Timestamp.now())
        val incrementCommentCount = FieldValue.increment(1)
        val addedComment = FieldValue.arrayUnion(comment)

        val batch: WriteBatch = firestore.batch()
        batch.set(postRef,mapOf("commentCount" to incrementCommentCount), SetOptions.merge())
        batch.set(userPostRef,mapOf("commentCount" to incrementCommentCount), SetOptions.merge())
        batch.set(commentRef,comment)
        batch.update(postRef, "comments", addedComment)
        batch.update(userPostRef, "comments", addedComment)
        return ErrorHandling.taskErrorHandler {
            batch.commit()
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun getOpenCommentSection(postId: String) = callbackFlow {
        val commentsRef = firestore.collection("posts")
            .document(postId).collection("comments").orderBy("date", Query.Direction.ASCENDING)

        val subscription = commentsRef.addSnapshotListener{ snapshot, e ->
            if(e != null){
                offer(ResultWrapper.Failure(HealthApplication.Strings.get(R.string.something_wrong)))
                close(e)
                return@addSnapshotListener
            }
            try {
                val result = snapshot?.toObjects(Comment::class.java)
                offer(ResultWrapper.Success(result))
            }catch (e : Exception){
                offer(ResultWrapper.Failure(e.message))
            }
        }
        awaitClose { subscription.remove() }
    }

    @ExperimentalCoroutinesApi
    suspend fun getAllPosts(lastVisible : DocumentSnapshot?, pageSize : Int) :
            Flow<ResultWrapper<QuerySnapshot>>{
        val postsRef = if(lastVisible == null){
            firestore.collection("posts").orderBy("date", Query.Direction.DESCENDING).limit(pageSize.toLong())
        }else{
            firestore.collection("posts").orderBy("date", Query.Direction.DESCENDING)
                .startAfter(lastVisible).limit(pageSize.toLong())
        }

        return ErrorHandling.taskErrorHandlerFlow {
            postsRef.get()
        }
    }
    @ExperimentalCoroutinesApi
    suspend fun getUserPosts(lastVisible : DocumentSnapshot?, pageSize : Int, uid: String) :
            Flow<ResultWrapper<QuerySnapshot>>{

        val postsRef = if(lastVisible == null){
            firestore
                .collection("users")
                .document(uid).collection("posts")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())
        }else{
            firestore
                .collection("users")
                .document(uid).collection("posts")
                .orderBy("date", Query.Direction.DESCENDING)
                .startAfter(lastVisible).limit(pageSize.toLong())
        }

        return ErrorHandling.taskErrorHandlerFlow {
            postsRef.get()
        }
    }


    @ExperimentalCoroutinesApi
    suspend fun observeWaterIntake(): Flow<WaterEntity> = callbackFlow {
        val userId = withContext(Dispatchers.IO){
            sessionManager.fetchUserId().first()
        }

        val ref = firestore.collection("users").document(userId).collection("water")
        val date = dateToday()
        val subscription = ref.document(date).addSnapshotListener{ snapshot, e ->

            if(e != null){
                close(e)
                return@addSnapshotListener
            }
            try {
                offer(WaterEntity(count = snapshot!!.data?.get("count") as Long, date))
            }catch (t : Throwable){
                offer(WaterEntity(count = 0L, date = date))
                print(t.message)
            }
        }
        awaitClose { subscription.remove() }
    }

    @ExperimentalCoroutinesApi
    suspend fun observeWaterIntakeHistory(): Flow<List<WaterEntity>> = callbackFlow {
        val userId = withContext(Dispatchers.IO){
            sessionManager.fetchUserId().first()
        }

        val ref = firestore.collection("users").document(userId).collection("water")
        val subscription = ref.addSnapshotListener{ snapshot, e ->

            if(e != null){
                close(e)
                return@addSnapshotListener
            }
            try {
                val dailyIntakeList = mutableListOf<WaterEntity>()
                snapshot!!.documentChanges.forEach{
                    dailyIntakeList.add(WaterEntity(count = it.document["count"] as Long, date = it.document.id))
                }
                offer(dailyIntakeList.toList())
            }catch (t : Throwable){
                offer(listOf(WaterEntity(0L,"")))
                print(t.message)
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun incrementWaterCount(){
        val userId = withContext(Dispatchers.IO){
            sessionManager.fetchUserId().first()
        }
        val ref = firestore.collection("users").document(userId).collection("water")
        val increment = FieldValue.increment(1)
        val date = dateToday()

        ErrorHandling.taskErrorHandler {
            ref.document(date).set(mapOf("count" to increment), SetOptions.merge())
        }

    }
    suspend fun decrementWaterCount(){
        val userId = withContext(Dispatchers.IO){
            sessionManager.fetchUserId().first()
        }
        val ref = firestore.collection("users").document(userId).collection("water")
        val increment = FieldValue.increment(-1)
        val date = dateToday()

        val result = ErrorHandling.taskErrorHandler {
            ref.document(date).get()
        }
        if(result is ResultWrapper.Success && result.value!!.getField<Long>("count")!! > 0 ){
            ErrorHandling.taskErrorHandler {
                ref.document(date).set(mapOf("count" to increment), SetOptions.merge())
            }
        }
    }

    suspend fun addRegisteredUserData(userData : HashMap<String, String>, uid : String){
        firestore.collection("users").document(uid).set(userData).await()
    }

    suspend fun getUserData(userId : String?): ResultWrapper<DocumentSnapshot> {
        val ref : DocumentReference = if(userId == null){
            val currentUid = withContext(Dispatchers.IO){
                sessionManager.fetchUserId().first()
            }
            firestore.collection("users").document(currentUid)
        } else{
            firestore.collection("users").document(userId)
        }
        return ErrorHandling.taskErrorHandler {
            ref.get()
        }
    }

    suspend fun searchUsers(searchTerm: String): Flow<ResultWrapper<QuerySnapshot>> {
        val ref = firestore
            .collection("users")
            .whereGreaterThanOrEqualTo("username", searchTerm)
            .whereLessThanOrEqualTo("username", searchTerm+ '\uf8ff')
        return ErrorHandling.taskErrorHandlerFlow {
            ref.get()
        }
    }

    suspend fun getProgress(uid : String): Flow<ResultWrapper<QuerySnapshot>> {
        val ref = firestore
            .collection("users")
            .document(uid)
            .collection("progress")
            .orderBy("weekOfYear")
            .limit(5)

        return ErrorHandling.taskErrorHandlerFlow {
            ref.get()
        }
    }

    private fun dateToday() : String{
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
}