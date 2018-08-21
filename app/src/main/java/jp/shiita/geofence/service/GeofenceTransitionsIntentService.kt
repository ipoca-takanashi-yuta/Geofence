package jp.shiita.geofence.service

import android.Manifest
import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.LocationServices
import dagger.android.AndroidInjection
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import jp.shiita.geofence.R
import jp.shiita.geofence.data.GithubRepository
import jp.shiita.geofence.getGeofencePendingIntent
import jp.shiita.geofence.getGeofencingRequest
import javax.inject.Inject


/**
 * Created by Yuta Takanashi on 2018/08/08.
 */
class GeofenceTransitionsIntentService : IntentService("Geofence") {
    private lateinit var geofencingClient: GeofencingClient
    private var beforePendingIntent: PendingIntent? = null

    @Inject
    lateinit var gitHubRepository: GithubRepository

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        geofencingClient = LocationServices.getGeofencingClient(this)
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "onHandleIntent")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            Log.e(TAG, geofencingEvent.errorCode.toString())
            return
        }

        val geofences = geofencingEvent.triggeringGeofences.map { it.requestId }.joinToString(separator = "\n")
        val location = geofencingEvent.triggeringLocation
        val locString = "(${location.latitude}, ${location.longitude})"

        when (geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                notify("Enter\n$geofences\n$locString")
                Log.d(TAG, "Enter\n$geofences\n$locString")
//                resetGeofences(location.latitude, location.longitude)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                notify("Exit\n$geofences\n$locString")
                Log.d(TAG, "Exit\n$geofences\n$locString")
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                notify("Dwell\n$geofences\n$locString")
                Log.d(TAG, "Dwell\n$geofences\n$locString")
            }
            else -> {
                notify("unknown event")
                Log.d(TAG, "unknown event")
            }
        }
    }

    private fun resetGeofences(lat: Double, lng: Double) {
        Log.d(TAG, "reset geofences")
        if (beforePendingIntent != null) {
            geofencingClient.removeGeofences(beforePendingIntent)?.run {
                addOnSuccessListener { Log.d(TAG, "removeOnSuccess") }
                addOnFailureListener { Log.d(TAG, "removeOnFailure") }
            }
        }
        beforePendingIntent = getGeofencePendingIntent(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(getGeofencingRequest(TAG, lat, lng), beforePendingIntent)?.run {
                addOnSuccessListener { Log.d(TAG, "addOnSuccess") }
                addOnFailureListener { Log.d(TAG, "addOnFailure") }
            }
        }
    }

    private fun notify(title: String) {
        gitHubRepository.getRepos("shiita0903")
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onError = { Log.e(TAG, "onError", it) },
                        onSuccess = {
                            Log.d(TAG, "onSuccess")
                            val text = it?.get(0)?.name ?: ""
                            buildNotification(title, text)
                        }
                )
    }

    private fun buildNotification(title: String, text: String) {
        val notification = NotificationCompat.Builder(this@GeofenceTransitionsIntentService)
                .setStyle(NotificationCompat.BigTextStyle())
                .setSmallIcon(R.drawable.notification_icon_background)
                .setContentTitle(title)
                .setContentText(text)
                .build()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1111, notification)
    }

    companion object {
        private val TAG = GeofenceTransitionsIntentService::class.java.simpleName
    }
}