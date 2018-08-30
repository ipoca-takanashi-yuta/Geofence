package jp.shiita.geofence.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by Yuta Takanashi on 2018/08/29.
 */
fun writeLocations(context: Context, locations: List<Pair<Double, Double>>, radius: Float) {
    val locStrings = locations.map { "${it.first},${it.second}" }
    context.getGeofencePreferences().editClear {
        putInt("count", locStrings.size)
        locStrings.forEachIndexed { i, s ->
            putString((i + 1).toString(), s)
        }
        putFloat("radius", radius)
    }
}

fun readLocations(context: Context): Pair<List<Pair<Double, Double>>, Float> {
    context.getGeofencePreferences().run {
        val locCount = getInt("count", 0)
        val locations = (1..locCount).map { i ->
            getString(i.toString(), "")
                    .split(",")
                    .map(String::toDouble)
                    .toPair()
        }
        val radius = getFloat("radius", 0f)
        return (if (locations.isEmpty()) emptyList() else locations) to radius
    }
}

fun clearLocation(context: Context) = context.getGeofencePreferences().editClear {}

fun writeRequestCode(context: Context, requestCode: Int) =
        context.getPendingIntentPreferences().editClear { putInt("requestCode", requestCode) }

fun readRequestCode(context: Context): Int =
        context.getPendingIntentPreferences().run { getInt("requestCode", 0) }

private fun Context.getGeofencePreferences(): SharedPreferences =
        getSharedPreferences("Geofence", Context.MODE_PRIVATE)

private fun Context.getPendingIntentPreferences(): SharedPreferences =
        getSharedPreferences("PendingIntent", Context.MODE_PRIVATE)

fun SharedPreferences.editClear(func: SharedPreferences.Editor.() -> Unit) {
    edit().run {
        clear()
        func()
        apply()
    }
}