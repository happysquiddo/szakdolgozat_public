package hu.scsaba.health.utils.di

import android.app.NotificationManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import hu.scsaba.health.screens.loggedin.water.WaterForegroundService

@Module
@InstallIn(ServiceComponent::class)
object WaterServiceModule {

    @ServiceScoped
    @Provides
    fun provideWaterService() = WaterForegroundService()

    @ServiceScoped
    @Provides
    fun provideNotificationManager(@ApplicationContext context : Context) : NotificationManager{
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

}