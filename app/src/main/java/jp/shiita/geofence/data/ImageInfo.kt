package jp.shiita.geofence.data

import com.google.gson.annotations.SerializedName

/**
 * Created by Yuta Takanashi on 2018/08/21.
 */
data class ImageInfo (
    val id: Int = 0,
    val pageURL: String = "",
    val type: String = "",
    val tags: String = "",
    val previewURL: String = "",
    val previewWidth: Int = 0,
    val previewHeight: Int = 0,
    val webformatURL: String = "",
    val webformatWidth: Int = 0,
    val webformatHeight: Int = 0,
    val largeImageURL: String = "",
    val fullHDURL: String = "",
    val imageURL: String = "",
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val imageSize: Int = 0,
    val views: Int = 0,
    val downloads: Int = 0,
    val favorites: Int = 0,
    val likes: Int = 0,
    val comments: Int = 0,
    @SerializedName("user_id")
    val userId: Int = 0,
    val user: String = "",
    val userImageURL: String = ""
)