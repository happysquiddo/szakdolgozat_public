package hu.scsaba.health.utils.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.scsaba.health.model.Repository
import hu.scsaba.health.screens.loggedin.breaks.BreaksAlarmManager
import hu.scsaba.health.utils.session.SessionManager
import hu.scsaba.health.services.Auth
import hu.scsaba.health.services.Firestore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HiltModule {

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideFireBaseAuth(): FirebaseAuth {
        return Firebase.auth
    }
    @Provides
    @Singleton
    fun provideFirebaseFirestore() = Firebase.firestore

    @Provides
    @Singleton
    fun provideAuth(firebaseAuth: FirebaseAuth, firestore: Firestore) : Auth{
        return Auth(firebaseAuth, firestore)
    }

    @Provides
    @Singleton
    fun provideBreaksAlarmManager(@ApplicationContext context : Context) : BreaksAlarmManager {
        return BreaksAlarmManager(context)
    }

    @Provides
    @Singleton
    fun provideRepository(auth : Auth, firestore: Firestore, sessionManager : SessionManager,
                          breaksAlarmManager: BreaksAlarmManager,
                          @ApplicationContext context: Context) : Repository{
        return Repository(auth = auth, firestore = firestore, sessionManager = sessionManager,breaksAlarmManager,
            context = context)
    }

    @Provides
    @Singleton
    fun provideFirestore(firebaseFirestore: FirebaseFirestore
                         , sessionManager: SessionManager
    ) : Firestore {
        return Firestore(firebaseFirestore, sessionManager)
    }
}
