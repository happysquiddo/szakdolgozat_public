package hu.scsaba.health.services

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import hu.scsaba.health.HealthApplication
import hu.scsaba.health.R
import hu.scsaba.health.utils.exception.CustomException
import hu.scsaba.health.utils.helper.ErrorHandling
import hu.scsaba.health.utils.helper.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


class Auth @Inject constructor(private val auth : FirebaseAuth, private val firestore : Firestore) {

    suspend fun signUpWithPassword(email: String, password: String, username : String)
    : Flow<ResultWrapper<AuthResult>>{
        val usernameMatch = firestore.searchForUsernameDuplicate(username)
        return if (usernameMatch is ResultWrapper.Success) {
            ErrorHandling.taskErrorHandlerFlow {
                if (!usernameMatch.value!!.isEmpty) {
                    throw CustomException(HealthApplication.Strings.get(R.string.name_exists))
                }
                auth.createUserWithEmailAndPassword(email, password)
            }.onEach { resultWrapper ->
                when(resultWrapper){
                    is ResultWrapper.Success -> {
                        val user = auth.currentUser
                        user?.updateProfile(
                            UserProfileChangeRequest.Builder().setDisplayName(username).build()
                        )

                        val uid = user!!.uid
                        val userData = hashMapOf(
                            "uid" to uid,
                            "email" to email,
                            "username" to username,
                        )

                        firestore.addRegisteredUserData(userData, uid)

                    }
                }
            }.flowOn(Dispatchers.IO)
        } else {
            flow {
                emit(ResultWrapper.Failure(HealthApplication.Strings.get(R.string.something_wrong)))
            }
        }
    }

    suspend fun loginWithPassword(email: String, password: String) : Flow<ResultWrapper<AuthResult>>{
        return ErrorHandling.taskErrorHandlerFlow {
            auth.signInWithEmailAndPassword(email, password)
        }
    }

    fun logOut(){
        auth.signOut()
    }

    fun isLoggedIn() : Boolean {
        val user = auth.currentUser
        return user != null
    }
}