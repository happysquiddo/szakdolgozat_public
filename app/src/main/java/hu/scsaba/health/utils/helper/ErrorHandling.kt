package hu.scsaba.health.utils.helper

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import hu.scsaba.health.HealthApplication
import hu.scsaba.health.R
import hu.scsaba.health.utils.exception.CustomException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

sealed class ResultWrapper<out T> {
    data class Success<out T>(val value: T?): ResultWrapper<T>()
    data class Failure(val errorMessageToUser : String? = ""): ResultWrapper<Nothing>()
}

class ErrorHandling {
    companion object{
        suspend fun <T> taskErrorHandlerFlow(safeCall: () -> Task<T>): Flow<ResultWrapper<T>> {
            return flow {
                try {
                    val result = safeCall().await()

                    emit(ResultWrapper.Success(result))
                } catch (e: Exception) {
                    var message = HealthApplication.Strings.get(R.string.something_wrong)
                    if(e is FirebaseAuthInvalidCredentialsException || e is FirebaseAuthInvalidUserException)
                        message = HealthApplication.Strings.get(R.string.invalid_credentials)
                    else if(e is FirebaseAuthUserCollisionException)
                        message = HealthApplication.Strings.get(R.string.email_exists)
                    else if(e is CustomException)// ResultWrapper.FAILURE(e.message)
                        message = e.message!!
                    emit(ResultWrapper.Failure(message))
                }
            }.flowOn(Dispatchers.IO)
        }
        suspend fun <T> taskErrorHandler(safeCall: () -> Task<T>): ResultWrapper<T>{
            return try {
                val result = safeCall().await()

                ResultWrapper.Success(result)
            } catch (e: Exception) {
                var message = HealthApplication.Strings.get(R.string.something_wrong)

                if(e is FirebaseAuthInvalidCredentialsException)
                    message = HealthApplication.Strings.get(R.string.invalid_credentials)
                else if(e is FirebaseAuthUserCollisionException)
                    message = HealthApplication.Strings.get(R.string.email_exists)
                else if(e is CustomException)// ResultWrapper.FAILURE(e.message)
                    message = e.message!!

                ResultWrapper.Failure(message)
            }

        }
    }
}