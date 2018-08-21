package jp.shiita.geofence.module

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import jp.shiita.geofence.data.GitHubService
import jp.shiita.geofence.data.GithubRepository
import jp.shiita.geofence.data.HeartRailsRepository
import jp.shiita.geofence.data.HeartRailsService
import javax.inject.Singleton

/**
 * Created by Yuta Takanashi on 2018/08/21.
 */
@Module
class RepositoryModule {
    @Provides
    @Singleton
    fun provideGitHubRepository(gitHubService: GitHubService) = GithubRepository(gitHubService)

    @Provides
    @Singleton
    fun provideHeartRailsRepository(heartRailsService: HeartRailsService, gson: Gson) = HeartRailsRepository(heartRailsService, gson)
}