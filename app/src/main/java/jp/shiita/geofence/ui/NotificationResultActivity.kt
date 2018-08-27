package jp.shiita.geofence.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import com.bumptech.glide.Glide
import jp.shiita.geofence.R
import kotlinx.android.synthetic.main.activity_notification_result.*

class NotificationResultActivity : AppCompatActivity() {
    private val urls: ArrayList<String> by lazy { intent.getStringArrayListExtra(URLS) ?: arrayListOf() }
    private val location: String by lazy { intent.getStringExtra(LOCATION) ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_result)
        title = "${location}の画像"

        urls.zip((1..urls.size).map { ImageView(this) }).forEach { (url, imageView) ->
            Glide.with(this)
                    .load(url)
                    .into(imageView)
            resultImages.addView(imageView)
        }
    }

    companion object {
        const val URLS = "urls"
        const val LOCATION = "location"
    }
}
