package jp.shiita.geofence.data

/**
 * Created by Yuta Takanashi on 2018/08/21.
 */
class GithubRepository(private val gitHubService: GitHubService) {
    fun getRepos(user: String) = gitHubService.getRepos(user)
}