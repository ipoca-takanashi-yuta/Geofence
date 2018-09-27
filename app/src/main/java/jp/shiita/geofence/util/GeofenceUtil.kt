package jp.shiita.geofence.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
    val (geofences, locations) = getGeofenceList(lat, lng, 0.0025, 100f)
    writeLocations(context, locations, 100f)
    val info = locations.mapIndexed { i, (la, ln) -> "Geofence${i + 1}:($la, $ln)" }.joinToString(separator = "\n")
    Log.d(tag, "Add geofences\n$info")
    return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()
}

private fun getGeofenceList(lat: Double, lng: Double, d: Double, r: Float): Pair<List<Geofence>, List<Pair<Double, Double>>> {
    return (-1..1).flatMap { i -> (-1..1).map { j ->
        val cLat = lat + i * d
        val cLng = lng + j * d
        Geofence.Builder()
                .setRequestId("Geofence${i + 1}")
                .setCircularRegion(cLat, cLng, r)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(300000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT or Geofence.GEOFENCE_TRANSITION_DWELL)
                .build() to (cLat to cLng)
    } }.unzip()
}

fun buildNotification(title: String, text: String, pendingIntent: PendingIntent?, context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    generateNotificationChannel(notificationManager, "notification id", "notification", "description")
    val notification = NotificationCompat.Builder(context, "notification id")
            .setStyle(NotificationCompat.BigTextStyle())
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .build()
    notificationManager.notify(Random().nextInt(), notification)
}

fun generateNotificationChannel(manager: NotificationManager, id: String, name: String, description: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (manager.getNotificationChannel(id) == null) {
            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
            channel.description = description
            manager.createNotificationChannel(channel)
        }
    }
}