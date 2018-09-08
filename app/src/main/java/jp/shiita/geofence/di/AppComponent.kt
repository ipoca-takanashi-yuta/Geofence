package jp.shiita.geofence.di

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import jp.shiita.geofence.GeofenceApplication
import javax.inject.Singleton

/**
 * Created by Yuta Takanashi on 2018/08/17.
 */
@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    ApiModule::class,
    ActivityBuilderModule::class,
    ServiceBuilderModule::class])
interface AppComponent : AndroidInjector<GeofenceApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: GeofenceApplication): Builder
        fun build(): AppComponent
    }

    override fun inject(application: GeofenceApplication)
}