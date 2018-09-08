package jp.shiita.geofence.di

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
import javax.inject.Named
import javax.inject.Singleton

/**
 * Created by Yuta Takanashi on 2018/08/17.
 */
@Module(includes = [RepositoryModule::class])
class ApiModule {
    @Provides
    @Singleton
    @Named("identity")
    fun provideIdentityGson(): Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .create()

    @Provides
    @Singleton
    @Named("lowerCase")
    fun provideLowerCaseGson(): Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

    @Provides
    @Singleton
    @Named("gitHub")
    fun providesGitHubRetrofit(@Named("lowerCase") gson: Gson, okHttpClient: OkHttpClient): Retrofit =
            getRetrofit("https://api.github.com/", gson, okHttpClient)

    @Provides
    @Singleton
    @Named("heartRails")
    fun providesHeartRailsRetrofit(@Named("lowerCase") gson: Gson, okHttpClient: OkHttpClient): Retrofit =
            getRetrofit("http://geoapi.heartrails.com/api/", gson, okHttpClient)

    @Provides
    @Singleton
    @Named("pixabay")
    fun providesPixabayRetrofit(@Named("identity") gson: Gson, okHttpClient: OkHttpClient): Retrofit =
            getRetrofit("https://pixabay.com/api/", gson, okHttpClient)

    @Provides
    @Singleton
    fun providesGitHubService(@Named("gitHub") retrofit: Retrofit): GitHubService =
            retrofit.create(GitHubService::class.java)

    @Provides
    @Singleton
    fun provideHeartRailsService(@Named("heartRails") retrofit: Retrofit): HeartRailsService =
            retrofit.create(HeartRailsService::class.java)

    @Provides
    @Singleton
    fun providePixabayService(@Named("pixabay") retrofit: Retrofit): PixabayService =
            retrofit.create(PixabayService::class.java)

    private fun getRetrofit(baseUrl: String, gson: Gson, okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .build()
}