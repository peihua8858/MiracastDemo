package com.peihua.miracastdemo

import android.app.Application
import android.content.Context

class MiraCastApplication: Application() {
    companion object {
        private const val TAG = "CastApplication"
        private lateinit var mApplication: MiraCastApplication

        @JvmStatic
        val context: Context
            get() = mApplication

        @JvmStatic
        val application: MiraCastApplication
            get() = mApplication
    }

    override fun onCreate() {
        super.onCreate()
        mApplication = this
    }
}