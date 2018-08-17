package jp.shiita.geofence

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest

/**
 * Created by Yuta Takanashi on 2018/08/17.
 */
fun getGeofencePendingIntent(context: Context): PendingIntent =
    PendingIntent.getService(context, 0,
            Intent(context, GeofenceTransitionsIntentService::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT)

fun getGeofencingRequest(tag: String, lat: Double, lng: Double): GeofencingRequest {
    val (geofences, locations) = getGeofenceList(lat, lng)
    val info = locations.mapIndexed { i, (la, ln) -> "Geofence${i + 1}:($la, $ln)" }.joinToString(separator = "\n")
    Log.d(tag, "[Add geofences]\n$info")
    return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()
}

private fun getGeofenceList(lat: Double, lng: Double): Pair<List<Geofence>, List<Pair<Double, Double>>> {
    val dLat = arrayOf(0.01, 0.0, -0.01, 0.0)
    val dLng = arrayOf(0.0, 0.01, 0.0, -0.01)
    return dLat.zip(dLng).mapIndexed { i, (la, ln) ->
        val cLat = lat + la
        val cLng = lng + ln
        Geofence.Builder()
                .setRequestId("Geofence${i + 1}")
                .setCircularRegion(cLat, cLng, 100f)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build() to (cLat to cLng)
    }.unzip()
}