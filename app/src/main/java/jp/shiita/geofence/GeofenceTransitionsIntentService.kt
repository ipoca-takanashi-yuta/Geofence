package jp.shiita.geofence

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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path


/**
 * Created by Yuta Takanashi on 2018/08/08.
 */
class GeofenceTransitionsIntentService : IntentService("Geofence") {
    private lateinit var geofencingClient: GeofencingClient
    private var beforePendingIntent: PendingIntent? = null

    override fun onCreate() {
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
                resetGeofences(location.latitude, location.longitude)
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
        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val service = retrofit.create(GitHubService::class.java)
        val repoCall = service.listRepos("shiita0903")
        repoCall.enqueue(object : Callback<List<Repo>> {
            override fun onFailure(call: Call<List<Repo>>?, t: Throwable?) {}

            override fun onResponse(call: Call<List<Repo>>?, response: Response<List<Repo>>) {
                val text = response.body()?.get(0)?.name

                val notification = NotificationCompat.Builder(this@GeofenceTransitionsIntentService)
                        .setStyle(NotificationCompat.BigTextStyle())
                        .setSmallIcon(R.drawable.notification_icon_background)
                        .setContentTitle(title)
                        .setContentText(text)
                        .build()
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(1111, notification)
            }
        })
    }

    interface GitHubService {
        @GET("users/{user}/repos")
        fun listRepos(@Path("user") user: String): Call<List<Repo>>
    }

    data class Repo(
        val id: String = "",
        val name: String = "",
        val fullName: String = "",
        val url: String = "",
        val description: String = ""
    )

    companion object {
        private val TAG = GeofenceTransitionsIntentService::class.java.simpleName
    }
}