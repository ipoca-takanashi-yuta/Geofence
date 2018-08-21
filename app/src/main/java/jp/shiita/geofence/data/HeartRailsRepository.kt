package jp.shiita.geofence.data

import com.google.gson.Gson
import io.reactivex.Single

/**
 * Created by Yuta Takanashi on 2018/08/21.
 */
class HeartRailsRepository(
        private val heartRailsService: HeartRailsService,
        private val gson: Gson) {

    fun getGeolocations(lat: Double, lng: Double): Single<List<Geolocation>> =
            heartRailsService.getGeolocations(lat, lng)
                    .map {
                        it.getAsJsonObject("response")
                                .getAsJsonArray("location")
                                .map { gson.fromJson(it, Geolocation::class.java) }
                    }
}