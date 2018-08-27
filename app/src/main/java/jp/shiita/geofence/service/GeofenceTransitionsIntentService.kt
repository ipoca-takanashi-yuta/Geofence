package jp.shiita.geofence.service

import android.Manifest
import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.LocationServices
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import jp.shiita.geofence.R
import jp.shiita.geofence.data.HeartRailsRepository
import jp.shiita.geofence.data.PixabayRepository
import jp.shiita.geofence.getGeofencePendingIntent
import jp.shiita.geofence.getGeofencingRequest
import jp.shiita.geofence.ui.NotificationResultActivity
import javax.inject.Inject


/**
 * Created by Yuta Takanashi on 2018/08/08.
 */
class GeofenceTransitionsIntentService : IntentService("Geofence") {
    private lateinit var geofencingClient: GeofencingClient
    private var beforePendingIntent: PendingIntent? = null

    @Inject lateinit var heartRailsRepository: HeartRailsRepository
    @Inject lateinit var pixabayRepository: PixabayRepository

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
                Log.d(TAG, "Enter\n$geofences\n$locString")
                searchGeolocation(location.latitude, location.longitude)
                // resetGeofences(location.latitude, location.longitude)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT  -> Log.d(TAG, "Exit\n$geofences\n$locString")
            Geofence.GEOFENCE_TRANSITION_DWELL -> Log.d(TAG, "Dwell\n$geofences\n$locString")
            else                               -> Log.d(TAG, "unknown event")
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

    private fun searchGeolocation(lat: Double, lng: Double) {
        heartRailsRepository.getGeolocations(lat, lng)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onError = { Log.e(TAG, "onError", it) },
                        onSuccess = {
                            val geolocation = it.first()
                            searchImage(listOf(geolocation.town, geolocation.city, geolocation.prefecture))
                        }
                )
    }

    private fun searchImage(queries: List<String>) {
        pixabayRepository.serarchImage(queries[0])
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onError = { Log.e(TAG, "onError", it) },
                        onSuccess = { imageInfoList ->
                            if (imageInfoList.isEmpty()) {
                                if (queries.size > 1) {
                                    searchImage(queries.drop(1))    // クエリを変えて再検索
                                }
                                return@subscribeBy
                            }

                            val urls = imageInfoList
                                    .map {
                                        when {
                                            it.imageURL.isNotBlank() -> it.imageURL
                                            else                     -> it.largeImageURL
                                        }}
                                    .filter { it.isNotEmpty() }
                                    .take(5)
                            notify(urls)
                        }
                )
    }

    private fun notify(urls: List<String>) {
        val intent = Intent(this, NotificationResultActivity::class.java).apply {
            putStringArrayListExtra(NotificationResultActivity.URLS, ArrayList(urls))
        }
        val stackBuilder = TaskStackBuilder.create(this).apply { addNextIntentWithParentStack(intent) }
        val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        buildNotification("Geofence", "Enter", pendingIntent)
    }

    private fun buildNotification(title: String, text: String, pendingIntent: PendingIntent) {
        val notification = NotificationCompat.Builder(this@GeofenceTransitionsIntentService)
                .setStyle(NotificationCompat.BigTextStyle())
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .build()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1111, notification)
    }

    companion object {
        private val TAG = GeofenceTransitionsIntentService::class.java.simpleName
    }
}