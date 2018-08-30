package jp.shiita.geofence.ui

import android.Manifest
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import dagger.android.AndroidInjection
import jp.shiita.geofence.R
import jp.shiita.geofence.util.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), LocationListener, OnMapReadyCallback {
    private lateinit var geofencingClient: GeofencingClient
    private var beforePendingIntent: PendingIntent? = null
    private var location: Location? = null
    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        geofencingClient = LocationServices.getGeofencingClient(this)
//        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        (googleMapFragment as SupportMapFragment).getMapAsync(this)
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
//                showToast("位置情報が取得できていません")
//                return@setOnClickListener
//            }
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 50f, this)

            val lat = latEdit.text.toString().toDoubleOrNull() ?: 35.648334
            val lng = lngEdit.text.toString().toDoubleOrNull() ?: 139.721371

            beforePendingIntent = getGeofencePendingIntent(this)
            geofencingClient.addGeofences(getGeofencingRequest(TAG, lat, lng, this), beforePendingIntent)?.run {
                addOnSuccessListener {
                    showToast("addOnSuccess")
                    plotGeofence()
                    addGeofences.isEnabled = false
                    removeGeofences.isEnabled = true
                }
                addOnFailureListener {
                    showToast("addOnFailure")
                }
            }
        }
        removeGeofences.setOnClickListener {
            geofencingClient.removeGeofences(beforePendingIntent)?.run {
                addOnSuccessListener {
                    showToast("removeOnSuccess")
                    eraseGeofence()
                    addGeofences.isEnabled = true
                    removeGeofences.isEnabled = false
                }
                addOnFailureListener {
                    showToast("removeOnFailure")
                }
            }
        }
    }

    override fun onLocationChanged(location: Location?) {
        this.location = location
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

    override fun onProviderEnabled(p0: String?) {}

    override fun onProviderDisabled(p0: String?) {}

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        plotGeofence()
    }

    private fun plotGeofence() {
        val locations = readLocations(this)
        if (locations.isEmpty()) return
        val center = locations.unzip().run { first.average() to second.average() }
        map?.let { m ->
            locations.map { LatLng(it.first, it.second) }
                    .forEach { m.addCircle(getCircle(it, 100.0)) }
            m.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(center.first, center.second), 16f))
        }
    }

    private fun eraseGeofence() {
        map?.clear()
        clearLocation(this)
    }

    private fun getCircle(latLng: LatLng, radius: Double): CircleOptions = CircleOptions()
            .center(latLng)
            .radius(radius)
            .strokeColor(Color.argb(32, 0, 0, 255))
            .fillColor(Color.argb(32, 0, 0, 255))

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
