package jp.shiita.geofence.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by Yuta Takanashi on 2018/08/29.
 */
fun writeLocations(context: Context, locations: List<Pair<Double, Double>>) {
    val locStrings = locations.map { "${it.first},${it.second}" }
    context.getGeofencePreferences().editClear {
        putInt("count", locStrings.size)
        locStrings.forEachIndexed { i, s ->
            putString((i + 1).toString(), s)
        }
    }
}

fun readLocations(context: Context): List<Pair<Double, Double>> {
    context.getGeofencePreferences().run {
        val locCount = getInt("count", 0)
        val locations = (1..locCount).map { i ->
            getString(i.toString(), "")
                    .split(",")
                    .map(String::toDouble)
                    .toPair()
        }
        return if (locations.isEmpty()) emptyList() else locations
    }
}

fun clearLocation(context: Context) = context.getGeofencePreferences().editClear {}

private fun Context.getGeofencePreferences(): SharedPreferences =
        getSharedPreferences("Geofence", Context.MODE_PRIVATE)

private fun SharedPreferences.editClear(func: SharedPreferences.Editor.() -> Unit) {
    edit().run {
        clear()
        func()
        apply()
    }
}