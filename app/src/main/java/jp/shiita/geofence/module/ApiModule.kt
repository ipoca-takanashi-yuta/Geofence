package jp.shiita.geofence.module

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import jp.shiita.geofence.data.GitHubService
import jp.shiita.geofence.data.HeartRailsService
import jp.shiita.geofence.data.PixabayService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Created by Yuta Takanashi on 2018/08/17.
 */
@Module(includes = [RepositoryModule::class])
class ApiModule {
    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

    @Provides
    @Singleton
    fun providesGitHubService(gson: Gson, okHttpClient: OkHttpClient): GitHubService =
            getRetrofit("https://api.github.com/", gson, okHttpClient)
                    .create(GitHubService::class.java)

    @Provides
    @Singleton
    fun provideHeartRailsService(gson: Gson, okHttpClient: OkHttpClient): HeartRailsService =
            getRetrofit("http://geoapi.heartrails.com/api/", gson, okHttpClient)
                    .create(HeartRailsService::class.java)
    @Provides
    @Singleton
    fun providePixabayService(okHttpClient: OkHttpClient): PixabayService =
            getRetrofit("https://pixabay.com/api/", Gson(), okHttpClient)
                    .create(PixabayService::class.java)

    private fun getRetrofit(baseUrl: String, gson: Gson, okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .build()
}