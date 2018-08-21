package jp.shiita.geofence.data

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by Yuta Takanashi on 2018/08/21.
 */
interface GitHubService {
    @GET("users/{user}/repos")
    fun getRepos(@Path("user") user: String): Single<List<Repo>>
}