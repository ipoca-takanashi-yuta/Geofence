package jp.shiita.geofence.data

import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Yuta Takanashi on 2018/08/21.
 */
interface HeartRailsService {
    @GET("json?method=searchByGeoLocation")
    fun getGeolocations(@Query("y") lat: Double,
                        @Query("x") lng: Double): Single<JsonObject>
}