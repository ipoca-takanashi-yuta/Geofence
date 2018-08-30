package jp.shiita.geofence.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
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
    private lateinit var clipboardManager: ClipboardManager
    private var location: Location? = null
    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        geofencingClient = LocationServices.getGeofencingClient(this)
        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
//        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        (googleMapFragment as SupportMapFragment).getMapAsync(this)

        if (readLocations(this).first.isNotEmpty()) {
            addGeofences.isEnabled = false
            removeGeofences.isEnabled = true
        }
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

            geofencingClient.addGeofences(
                    getGeofencingRequest(TAG, lat, lng, this),
                    getGeofencePendingIntent(this))?.run {
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
            geofencingClient.removeGeofences(getBeforeGeofencePendingIntent(this))?.run {
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
        map?.setOnCircleClickListener { circle ->
            val center = circle.center
            val locString = "${center.latitude},${center.longitude}"
            clipboardManager.primaryClip = ClipData.newPlainText("", locString)
            showToast("${locString}をコピーしました")
        }
        plotGeofence()
    }

    private fun plotGeofence() {
        val (locations, radius) = readLocations(this)
        if (locations.isEmpty()) return
        val center = locations.unzip().run { first.average() to second.average() }
        map?.let { m ->
            locations.map { LatLng(it.first, it.second) }
                    .forEach { m.addCircle(getCircle(it, radius.toDouble())) }
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
            .clickable(true)

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
