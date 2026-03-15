package com.example.ads.crosspromo.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.TransactionTooLargeException
import android.view.View

fun Context?.isNetworkAvailable(): Boolean {
    this?.let {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val isAvailable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = cm?.activeNetwork ?: return false
            val actNw = cm.getNetworkCapabilities(networkCapabilities) ?: return false
            when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> true
            }
        } else {
            cm?.let { it.activeNetworkInfo?.isConnected } ?: run { false }
        }
        return isAvailable
    }
    return false
}

fun Activity.openUrl(appUri: Uri) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, appUri))
    } catch (e: TransactionTooLargeException) {
        e.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun Int.isOdd() = this % 2 == 1
fun Int.isEven() = this % 2 == 0