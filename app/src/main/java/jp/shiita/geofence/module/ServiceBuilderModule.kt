package jp.shiita.geofence.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.shiita.geofence.service.GeofenceTransitionsIntentService

/**
 * Created by Yuta Takanashi on 2018/08/21.
 */
@Module
internal abstract class ServiceBuilderModule {
    @ContributesAndroidInjector
    internal abstract fun contributeGeofenceTransitionsIntentService(): GeofenceTransitionsIntentService
}
