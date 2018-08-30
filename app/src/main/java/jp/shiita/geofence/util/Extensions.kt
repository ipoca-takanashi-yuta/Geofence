package jp.shiita.geofence.util

import android.content.Context
import android.widget.Toast

/**
 * Created by Yuta Takanashi on 2018/08/30.
 */
fun <T> List<T>.toPair(): Pair<T, T> {
    if (this.size != 2) {
        throw IllegalArgumentException("List is not of length 2")
    }
    return Pair(this[0], this[1])
}

fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}