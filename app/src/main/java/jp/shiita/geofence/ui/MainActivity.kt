package jp.shiita.geofence.ui

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.location.GeofencingClient
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
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    private lateinit var geofencingClient: GeofencingClient
    private var beforePendingIntent: PendingIntent? = null
    @Inject
    lateinit var heartRailsRepository: HeartRailsRepository
    @Inject
    lateinit var pixabayRepository: PixabayRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        geofencingClient = LocationServices.getGeofencingClient(this)

        addGeofences.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Log.d(TAG, "permission is denied")
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 9999)
                }
                return@setOnClickListener
            }

            beforePendingIntent = getGeofencePendingIntent(this)
            geofencingClient.addGeofences(getGeofencingRequest(TAG, 35.648334, 139.721371), beforePendingIntent)?.run {
                addOnSuccessListener {
                    toast("addOnSuccess")
                    addGeofences.isEnabled = false
                    removeGeofences.isEnabled = true
                }
                addOnFailureListener {
                    toast("addOnFailure")
                }
            }
        }
        removeGeofences.setOnClickListener {
            geofencingClient.removeGeofences(beforePendingIntent)?.run {
                addOnSuccessListener {
                    toast("removeOnSuccess")
                    addGeofences.isEnabled = true
                    removeGeofences.isEnabled = false
                }
                addOnFailureListener {
                    toast("removeOnFailure")
                }
            }
        }
        latlngButton.setOnClickListener {
            heartRailsRepository.getGeolocations(35.648334, 139.721371)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onError = { Log.e(TAG, "onError", it) },
                            onSuccess = {
                                val geolocation = it.first()
                                searchImage(geolocation.city)
                                townInfo.text = geolocation.address
                            }
                    )
        }
    }

    private fun searchImage(query: String) {
        pixabayRepository.serarchImage(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onError = { Log.e(TAG, "onError", it) },
                        onSuccess = { imageInfoList ->
                            imageInfoList.firstOrNull()?.let {
                                val url = when {
                                    !it.imageURL.isEmpty()      -> it.imageURL
                                    !it.largeImageURL.isEmpty() -> it.largeImageURL
                                    else                        -> {
                                        toast("画像が見つかりませんでした")
                                        return@let
                                    }
                                }
                                Glide.with(this)
                                        .load(url)
                                        .into(townImage)
                            }
                        }
                )
    }

    private fun Context.toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
