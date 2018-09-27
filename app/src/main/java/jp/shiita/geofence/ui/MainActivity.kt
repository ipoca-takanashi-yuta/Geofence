package jp.shiita.geofence.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.android.AndroidInjection
import jp.shiita.geofence.R
import jp.shiita.geofence.util.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var locationProviderClient: FusedLocationProviderClient
    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        geofencingClient = LocationServices.getGeofencingClient(this)
        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        setupLocation()

        (googleMapFragment as SupportMapFragment).getMapAsync(this)

        if (readLocations(this).first.isNotEmpty()) {
            addGeofences.isEnabled = false
            removeGeofences.isEnabled = true
        }
        addGeofences.setOnClickListener { addGeofence() }
        removeGeofences.setOnClickListener { removeGeofence() }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        map?.setOnCircleClickListener { circle ->
            val center = circle.center
            val locString = "${center.latitude},${center.longitude}"
            clipboardManager.primaryClip = ClipData.newPlainText("", locString)
            showToast("${locString}をコピーしました")
        }
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.648334, 139.721371), 16f))
        plotGeofence()
    }

    private fun setupLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val request = LocationRequest().apply {
            interval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                // なんかうまくいかん
//                removeGeofence()
//                addGeofence()
                Log.d(TAG, "onLocationResult:${location.latitude}, ${location.longitude}")
                map?.clear()
                map?.addMarker(MarkerOptions()
                        .position(LatLng(location.latitude, location.longitude))
                        .title("現在地"))
                plotGeofence()
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                super.onLocationAvailability(locationAvailability)
                Log.d(TAG, locationAvailability.toString())
            }
        }
        locationProviderClient.requestLocationUpdates(request, callback, null)
    }

    private fun plotGeofence() {
        val (locations, radius) = readLocations(this)
        if (locations.isEmpty()) return
        map?.let { m ->
            locations.map { LatLng(it.first, it.second) }
                    .forEach { m.addCircle(getCircle(it, radius.toDouble())) }
        }
    }

    private fun addGeofence() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d(TAG, "permission is denied")
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 9999)
            }
            return
        }

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

    private fun removeGeofence() {
        geofencingClient.removeGeofences(getBeforeGeofencePendingIntent(this))?.run {
            addOnSuccessListener {
                showToast("removeOnSuccess")
                map?.clear()
                clearLocation(this@MainActivity)
                addGeofences.isEnabled = true
                removeGeofences.isEnabled = false
            }
            addOnFailureListener {
                showToast("removeOnFailure")
            }
        }
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
