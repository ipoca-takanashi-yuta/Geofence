package jp.shiita.geofence

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
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
    override fun onHandleIntent(intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            Log.e(TAG, geofencingEvent.errorCode.toString())
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
            notify("GEOFENCE_TRANSITION_ENTER", this)
        }
        else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            notify("GEOFENCE_TRANSITION_EXIT", this)
        }
    }

    private fun notify(title: String, context: Context) {
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

                val builder = NotificationCompat.Builder(context)
                        .setStyle(NotificationCompat.BigTextStyle())
                        .setSmallIcon(R.drawable.notification_icon_background)
                        .setContentTitle(title)
                        .setContentText(text)
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(1111, builder.build())
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