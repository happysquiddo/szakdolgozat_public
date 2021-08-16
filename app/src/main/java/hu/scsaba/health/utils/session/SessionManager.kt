package hu.scsaba.health.utils.session

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

//@AndroidEntryPoint
class SessionManager @Inject constructor(
    private val context : Context
){

    private val Context.dataStore by preferencesDataStore(name = "userData")

    companion object {
        val USER_ID = stringPreferencesKey("id")
        val USER_NAME = stringPreferencesKey("username")
        val SERVICE_STATE = booleanPreferencesKey("serviceState")

        val BREAK_STATE = booleanPreferencesKey("breakState")
        val START_TIME = longPreferencesKey("startTime")
        val BREAK_INTERVAL = intPreferencesKey("breakInterval")
        val BREAK_DURATION = intPreferencesKey("breakDuration")
    }

    suspend fun clearUserData() {
        context.dataStore.edit { userData ->
            userData.clear()
        }
    }

    suspend fun saveUserData(username : String, uid : String){
        context.dataStore.edit { userData ->
            userData[USER_NAME] = username
            userData[USER_ID] = uid
        }
    }

    fun fetchUsername(): Flow<String> {
        return context.dataStore.data
            .map { preferences ->

                preferences[USER_NAME] ?: ""
            }.flowOn(Dispatchers.IO)
    }

    fun fetchUserId(): Flow<String> {
        return context.dataStore.data
            .map { preferences ->

                preferences[USER_ID] ?: ""
            }.flowOn(Dispatchers.IO)

    }

    suspend fun setServiceState(state : Boolean){
        context.dataStore.edit { userData ->
            userData[SERVICE_STATE] = state
        }

    }

    fun getServiceState(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[SERVICE_STATE] ?: false
        }
    }

    suspend fun setBreakState(state : Boolean) {
        context.dataStore.edit { userData ->
            userData[BREAK_STATE] = state
        }
    }
    suspend fun setBreakInterval(breakIntervalInMinutes : Int) {
        context.dataStore.edit { userData ->
            userData[BREAK_INTERVAL] = breakIntervalInMinutes * 60 * 1000
        }
    }
    suspend fun setBreakDurationInHours(durationInHours: Int) {
        context.dataStore.edit { userData ->
            userData[BREAK_DURATION] = durationInHours
        }
    }
    suspend fun setBreakStartTime(startTime: Long) {
        context.dataStore.edit { userData ->
            userData[START_TIME] = startTime
        }
    }
    fun getBreakState(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[BREAK_STATE] ?: false
        }
    }
    fun getBreakInterval(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[BREAK_INTERVAL] ?: 0
        }
    }
    fun getBreakDurationInHours(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[BREAK_DURATION] ?: 0
        }
    }
    fun getBreakStartTime() : Flow<Long> {
        return context.dataStore.data.map { preferences ->
            preferences[START_TIME] ?: 0L
        }
    }

}
