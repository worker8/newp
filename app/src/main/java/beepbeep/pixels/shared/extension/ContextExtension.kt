package beepbeep.pixels.shared.extension

import android.content.Context
import android.net.ConnectivityManager

fun Context.isConnectedToInternet(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager;
    return when (cm.activeNetworkInfo.type) {
        ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE -> true
        else -> false
    }
}