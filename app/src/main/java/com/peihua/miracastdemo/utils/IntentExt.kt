package com.peihua.miracastdemo.utils

import android.content.Intent
import android.os.Build
import android.os.Parcelable

fun <T : Parcelable> Intent?.getParcelableExtraCompat(key: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this?.getParcelableExtra(key, clazz)
    } else {
        this?.getParcelableExtra(key)
    }
}

fun <T : Parcelable> Intent?.getParcelableArrayExtraCompat(
    key: String,
    clazz: Class<T>,
): Array<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this?.getParcelableArrayExtra(key, clazz)
    } else {
        try {
            this?.getParcelableArrayExtra(key) as? Array<T>
        } catch (e: Exception) {
            dLog { "getParcelableArrayExtraCompat error:${e.message}" }
           return null
        }
    }
}

fun <T : Parcelable> Intent?.getParcelableArrayListExtraCompat(
    key: String,
    clazz: Class<T>,
): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this?.getParcelableArrayListExtra(key, clazz)
    } else {
        this?.getParcelableArrayListExtra(key)
    }
}