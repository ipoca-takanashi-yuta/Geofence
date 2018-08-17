package jp.shiita.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var geofencingClient: GeofencingClient
    private var beforePendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
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

        }
    }

    private fun Context.toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
