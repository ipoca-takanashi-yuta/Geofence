package jp.shiita.geofence.data

import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Yuta Takanashi on 2018/08/21.
 */
interface PixabayService {
    @GET("?key=9882315-a5e47dc8283c33ae97e087739")
    fun searchImage(@Query("q") query: String): Single<JsonObject>
}