package jp.shiita.geofence.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import jp.shiita.geofence.R
import jp.shiita.geofence.service.GeofenceTransitionsIntentService
import java.util.*

/**
 * Created by Yuta Takanashi on 2018/08/17.
 */
fun getGeofencePendingIntent(context: Context): PendingIntent {
    val code = Random().nextInt()
    writeRequestCode(context, code)
    return PendingIntent.getService(context, code,
            Intent(context, GeofenceTransitionsIntentService::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT)
}

fun getBeforeGeofencePendingIntent(context: Context): PendingIntent =
        PendingIntent.getService(context, readRequestCode(context),
                Intent(context, GeofenceTransitionsIntentService::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT)

fun getGeofencingRequest(tag: String, lat: Double, lng: Double, context: Context): GeofencingRequest {
    val (geofences, locations) = getGeofenceList(lat, lng, 0.002, 100f)
    writeLocations(context, locations, 100f)
    val info = locations.mapIndexed { i, (la, ln) -> "Geofence${i + 1}:($la, $ln)" }.joinToString(separator = "\n")
    Log.d(tag, "Add geofences\n$info")
    return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()
}

private fun getGeofenceList(lat: Double, lng: Double, d: Double, r: Float): Pair<List<Geofence>, List<Pair<Double, Double>>> {
    val dLat = arrayOf(d, 0.00, -d, 0.00)
    val dLng = arrayOf(0.00, d, 0.00, -d)
    return dLat.zip(dLng).mapIndexed { i, (la, ln) ->
        val cLat = lat + la
        val cLng = lng + ln
        Geofence.Builder()
                .setRequestId("Geofence${i + 1}")
                .setCircularRegion(cLat, cLng, r)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build() to (cLat to cLng)
    }.unzip()
}

fun buildNotification(title: String, text: String, pendingIntent: PendingIntent?, context: Context) {
    val notification = NotificationCompat.Builder(context)
            .setStyle(NotificationCompat.BigTextStyle())
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .build()
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(Random().nextInt(), notification)
}