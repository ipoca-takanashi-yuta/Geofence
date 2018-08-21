package jp.shiita.geofence.data

/**
 * Created by Yuta Takanashi on 2018/08/21.
 */
data class Geolocation(
        val city: String = "",
        val cityKana: String = "",
        val town: String = "",
        val townKana: String = "",
        val x: Double = 0.0,
        val y: Double = 0.0,
        val distance: Double = 0.0,
        val prefecture: String = "",
        val postal: String = ""
) {
    val address: String
        get() = "$prefecture $city $town"
}