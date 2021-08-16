package hu.scsaba.health.model

import android.content.Context
import android.content.Intent
import com.google.firebase.auth.AuthResult
import hu.scsaba.health.model.entities.water.WaterEntity
import hu.scsaba.health.screens.loggedin.water.WaterForegroundService
import hu.scsaba.health.utils.helper.ResultWrapper
import hu.scsaba.health.utils.session.SessionManager
import hu.scsaba.health.services.Auth
import hu.scsaba.health.services.Firestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import hu.scsaba.health.model.entities.workout.WorkoutEntity
import hu.scsaba.health.model.entities.post.Comment

import hu.scsaba.health.screens.loggedin.breaks.BreaksAlarmManager
import kotlinx.coroutines.flow.*


class Repository @Inject constructor(
    private val auth : Auth,
    private val firestore: Firestore,
    private val sessionManager: SessionManager,
    private val breaksAlarmManager: BreaksAlarmManager,
    private val context : Context
){

    suspend fun searchUsers(searchTerm: String): Flow<ResultWrapper<QuerySnapshot>> {
        return firestore.searchUsers(searchTerm)
    }

    private suspend fun saveUser(username: String, uid: String){
        sessionManager.saveUserData(username,uid)
    }
    private suspend fun resetUser(){
        sessionManager.clearUserData()
        stopWaterForegroundService()
        stopWorking()
    }
    suspend fun getCurrentUserId(): String {
        return sessionManager.fetchUserId().first()
    }

    suspend fun logOut(){
        auth.logOut()
        resetUser()
    }

    suspend fun isLoggedIn() : Boolean{
        val loggedIn = auth.isLoggedIn()
        if(!loggedIn) resetUser()
        return loggedIn
    }

    fun observeServiceState() : Flow<Boolean>{
        return sessionManager.getServiceState()
    }

    private suspend fun setServiceState(state : Boolean){
        sessionManager.setServiceState(state)
    }

    suspend fun signUpWithPassword(email: String, password: String, username : String)
            : Flow<ResultWrapper<AuthResult>>{
        return auth.signUpWithPassword(email, password, username).onEach { result ->
            if(result is ResultWrapper.Success) {
                val user = result.value!!.user!!
                saveUser(username, user.uid )

            }
        }
    }

    suspend fun loginWithPassword(email: String, password: String) : Flow<ResultWrapper<AuthResult>>{
        return auth.loginWithPassword(email, password).onEach { result ->
            if(result is ResultWrapper.Success) {
                val user = result.value!!.user!!
                saveUser(user.displayName!!, user.uid )
            }
        }
    }

    suspend fun getUserData(userId : String? = null): ResultWrapper<DocumentSnapshot> {
        return firestore.getUserData(userId)
    }

    suspend fun editUserData(weight : Int, height : Int, age : Int) : ResultWrapper<Void>{
        return firestore.editUserData(weight, height, age)
    }

    suspend fun incrementWaterCount(){
        firestore.incrementWaterCount()
    }

    suspend fun decrementWaterCount(){
        firestore.decrementWaterCount()
    }

    @ExperimentalCoroutinesApi
    suspend fun observeWaterIntake(): Flow<WaterEntity> = firestore.observeWaterIntake()

    @ExperimentalCoroutinesApi
    suspend fun observeWaterIntakeHistory(): Flow<List<WaterEntity>> = firestore.observeWaterIntakeHistory()

    suspend fun startWaterForegroundService(){
        val serviceIntent = Intent(context, WaterForegroundService::class.java)
        context.startService(serviceIntent)

        setServiceState(true)
    }

    suspend fun stopWaterForegroundService(){
        val serviceIntent = Intent(context, WaterForegroundService::class.java)
        context.stopService(serviceIntent)
        setServiceState(false)
    }

    suspend fun startWorking(durationInHours : Int, breakIntervalInMinutes : Int, startTime : Long){
        breaksAlarmManager.startWorking(durationInHours, breakIntervalInMinutes)
        setBreakInterval(breakIntervalInMinutes)
        setBreakDuration(durationInHours)
        setBreakStartTime(startTime)
        setBreakState(true)
    }

    suspend fun stopWorking(){
        breaksAlarmManager.stopWorking()
        setBreakState(false)
    }

    fun setNextReminder(interval : Int){
        breaksAlarmManager.setNextReminder(interval)
    }



    suspend fun saveWorkout(workoutEntity: WorkoutEntity) : ResultWrapper<Void>{
        return firestore.saveWorkout(workoutEntity)
    }

    suspend fun getWorkoutList(): Flow<ResultWrapper<QuerySnapshot>> {
        return firestore.getWorkoutList()
    }

    suspend fun getWorkout(workoutName: String, uid : String): Flow<ResultWrapper<DocumentSnapshot>> {
        return firestore.getWorkout(workoutName, uid)
    }

    suspend fun saveWorkoutProgress(): ResultWrapper<Void> {
        return firestore.saveWorkoutProgress()
    }

    @ExperimentalCoroutinesApi
    suspend fun getAllPosts(lastVisible : DocumentSnapshot?, pageSize : Int): Flow<ResultWrapper<QuerySnapshot>> {
        return firestore.getAllPosts(lastVisible, pageSize)
    }
    @ExperimentalCoroutinesApi
    suspend fun getUserPosts(lastVisible : DocumentSnapshot?, pageSize : Int, uid: String ): Flow<ResultWrapper<QuerySnapshot>> {
        return firestore.getUserPosts(lastVisible, pageSize, uid)
    }
    suspend fun postWorkout(workoutEntity: WorkoutEntity, postText : String): ResultWrapper<Void>{
        return firestore.postWorkout(workoutEntity, postText)
    }
    @ExperimentalCoroutinesApi
    suspend fun getOpenCommentSection(postId: String): Flow<ResultWrapper<MutableList<Comment>>> {
        return firestore.getOpenCommentSection(postId)
    }
    suspend fun postComment(text : String, commenterId : String, postAuthorId : String, postId : String):
            ResultWrapper<Void> {
        return firestore.postComment(text, commenterId, postAuthorId, postId)
    }

    private suspend fun setBreakState(state : Boolean){
        sessionManager.setBreakState(state)
    }

    private suspend fun setBreakInterval(breakIntervalInMinutes : Int){
        sessionManager.setBreakInterval(breakIntervalInMinutes)
    }

    private suspend fun setBreakDuration(durationInHours :Int){
        sessionManager.setBreakDurationInHours(durationInHours)
    }

    private suspend fun setBreakStartTime(startTime: Long){
        sessionManager.setBreakStartTime(startTime)
    }

    fun observeBreakState() : Flow<Boolean> {
        return sessionManager.getBreakState()
    }

    fun getBreakInterval() : Flow<Int> {
        return sessionManager.getBreakInterval()
    }

    fun getBreakDurationInHours() : Flow<Int> {
        return sessionManager.getBreakDurationInHours()
    }

    fun observeBreakStartTime() : Flow<Long> {
        return sessionManager.getBreakStartTime()
    }

    suspend fun getProgress(uid: String): Flow<ResultWrapper<QuerySnapshot>> {
        return firestore.getProgress(uid)
    }

}