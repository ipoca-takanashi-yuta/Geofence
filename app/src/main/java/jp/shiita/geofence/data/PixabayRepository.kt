package jp.shiita.geofence.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.reactivex.Single

/**
 * Created by Yuta Takanashi on 2018/08/21.
 */
class PixabayRepository(
        private val pixabayService: PixabayService,
        private val gson: Gson
) {
    fun serarchImage(query: String): Single<List<ImageInfo>> =
            pixabayService.searchImage(query)
                    .map { json ->
                        json.getAsJsonArray("hits")
                                .map { gson.fromJson(it, ImageInfo::class.java) }
                    }
}