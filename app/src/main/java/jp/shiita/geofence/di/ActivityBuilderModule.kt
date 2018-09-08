package jp.shiita.geofence.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.shiita.geofence.ui.MainActivity

/**
 * Created by Yuta Takanashi on 2018/08/21.
 */
@Module
internal abstract class ActivityBuilderModule {
    @ContributesAndroidInjector
    internal abstract fun contributeMainActivity(): MainActivity
}