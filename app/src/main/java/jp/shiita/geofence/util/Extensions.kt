package jp.shiita.geofence.util

import android.content.Context
import android.widget.Toast

/**
 * Created by Yuta Takanashi on 2018/08/30.
 */
fun <T> List<T>.toPair(): Pair<T, T> {
    require(size == 2)
    return this[0] to this[1]
}

fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}