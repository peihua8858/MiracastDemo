package com.peihua.miracastdemo.utils

import android.content.Context
import android.widget.Toast
import android.widget.Toast.Duration
import androidx.annotation.StringDef
import androidx.annotation.StringRes
import com.peihua.miracastdemo.MiraCastApplication

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.showToast(message: String, @Duration duration: Int) {
    Toast.makeText(this, message, duration).show()
}

fun showToast(message: CharSequence): Toast {
    val toast = Toast.makeText(MiraCastApplication.context, message, Toast.LENGTH_LONG)
    toast.show()
    return toast
}

fun showToast(message: String, @Duration duration: Int): Toast {
    val toast = Toast.makeText(MiraCastApplication.context, message, duration)
    toast.show()
    return toast
}

fun Int.showToast(): Toast {
    val toast = Toast.makeText(MiraCastApplication.context, this, Toast.LENGTH_LONG)
    toast.show()
    return toast
}

fun Int.showToast(@Duration duration: Int): Toast {
    val toast = Toast.makeText(MiraCastApplication.context, this, duration)
    toast.show()
    return toast
}