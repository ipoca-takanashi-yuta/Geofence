package jp.shiita.geofence.ui

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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

class MainActivity : AppCompatActivity(), LocationListener {
    private lateinit var geofencingClient: GeofencingClient
    private var beforePendingIntent: PendingIntent? = null
    private var location: Location? = null

    @Inject lateinit var heartRailsRepository: HeartRailsRepository
    @Inject lateinit var pixabayRepository: PixabayRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        geofencingClient = LocationServices.getGeofencingClient(this)
//        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        addGeofences.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Log.d(TAG, "permission is denied")
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 9999)
                }
                return@setOnClickListener
            }

//            if (location == null) {
//                toast("位置情報が取得できていません")
//                return@setOnClickListener
//            }
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 50f, this)

            val lat = latEdit.text.toString().toDoubleOrNull() ?: 35.648334
            val lng = lngEdit.text.toString().toDoubleOrNull() ?: 139.721371

            beforePendingIntent = getGeofencePendingIntent(this)
            geofencingClient.addGeofences(getGeofencingRequest(TAG, lat, lng, this), beforePendingIntent)?.run {
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
            clearView()
            startLoading()
            heartRailsRepository.getGeolocations(35.648334, 139.721371)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onError = {
                                Log.e(TAG, "onError", it)
                                stopLoading()
                            },
                            onSuccess = {
                                val geolocation = it.first()
                                townInfo.text = geolocation.address
                                searchImage(listOf(geolocation.town, geolocation.city, geolocation.prefecture))
                            }
                    )
        }
    }

    override fun onLocationChanged(location: Location?) {
        this.location = location
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

    override fun onProviderEnabled(p0: String?) {}

    override fun onProviderDisabled(p0: String?) {}

    private fun searchImage(queries: List<String>) {
        pixabayRepository.serarchImage(queries[0])
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onError = {
                            Log.e(TAG, "onError", it)
                            stopLoading()
                        },
                        onSuccess = { imageInfoList ->
                            if (imageInfoList.isEmpty()) {
                                when (queries.size) {
                                    1 -> {
                                        toast("画像が見つかりませんでした")
                                        stopLoading()
                                    }
                                    else -> searchImage(queries.drop(1))    // クエリを変えて再検索
                                }
                                return@subscribeBy
                            }

                            queryInfo.text = "[検索ワード] : ${queries[0]}"
                            val urls = imageInfoList
                                    .map {
                                        when {
                                            it.imageURL.isNotBlank() -> it.imageURL
                                            else                     -> it.largeImageURL
                                        }}
                                    .filter { it.isNotEmpty() }
                                    .take(5)
                            loadImages(urls)
                        }
                )
    }

    private fun loadImages(urls: List<String>) {
        urls.zip((1..urls.size).map { ImageView(this) }).forEach { (url, imageView) ->
            Glide.with(this)
                    .load(url)
                    .addListener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?,
                                                  isFirstResource: Boolean) = false

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?,
                                                     dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            stopLoading()
                            return false
                        }
                    })
                    .into(imageView)
            townImages.addView(imageView)
        }
    }

    private fun clearView() {
        townInfo.text = ""
        queryInfo.text = ""
        townImages.removeAllViews()
    }

    private fun startLoading() {
        loadingIndicator.visibility = View.VISIBLE
        latlngButton.isEnabled = false
    }

    private fun stopLoading() {
        loadingIndicator.visibility = View.GONE
        latlngButton.isEnabled = true
    }

    private fun Context.toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
