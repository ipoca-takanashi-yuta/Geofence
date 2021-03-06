package jp.shiita.geofence.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import jp.shiita.geofence.data.*
import javax.inject.Named
import javax.inject.Singleton

/**
 * Created by Yuta Takanashi on 2018/08/21.
 */
@Module
class RepositoryModule {
    @Provides
    @Singleton
    fun provideGitHubRepository(gitHubService: GitHubService) =
            GithubRepository(gitHubService)

    @Provides
    @Singleton
    fun provideHeartRailsRepository(heartRailsService: HeartRailsService, @Named("lowerCase")gson: Gson) =
            HeartRailsRepository(heartRailsService, gson)

    @Provides
    @Singleton
    fun providePixabayRepository(pixabayService: PixabayService, @Named("identity")gson: Gson) =
            PixabayRepository(pixabayService, gson)
}