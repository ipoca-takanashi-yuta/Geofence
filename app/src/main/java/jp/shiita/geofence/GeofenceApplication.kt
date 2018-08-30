package jp.shiita.geofence

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import jp.shiita.geofence.component.DaggerAppComponent

/**
 * Created by Yuta Takanashi on 2018/08/17.
 */
class GeofenceApplication : DaggerApplication() {
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = DaggerAppComponent.builder()
            .application(this)
            .build()
}