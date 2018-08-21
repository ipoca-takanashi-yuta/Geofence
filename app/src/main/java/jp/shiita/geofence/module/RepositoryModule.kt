package jp.shiita.geofence.module

import dagger.Module
import dagger.Provides
import jp.shiita.geofence.data.GitHubService
import jp.shiita.geofence.data.GithubRepository
import javax.inject.Singleton

/**
 * Created by Yuta Takanashi on 2018/08/21.
 */
@Module
class RepositoryModule {
    @Provides
    @Singleton
    fun provideGitHubRepository(gitHubService: GitHubService) = GithubRepository(gitHubService)
}