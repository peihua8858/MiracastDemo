package com.peihua.miracastdemo

import android.app.Activity

class WifiSinkDisplayManager() {
    val callbacks = mutableListOf<ReceiverApiModel.() -> Unit>()
    val callback: ReceiverApiModel.() -> Unit = {
        callbacks.forEach {
            val api = ReceiverApiModel().apply(it)
            onChangeStatus {
                api.invokeOnChangeStatus(it)
            }
            onChangeUiPortrait {
                api.invokeOnChangeUiPortrait(it)
            }
            onAddWfdSinkGuide {
                api.invokeOnAddWfdSinkGuide()
            }
            onRequestFullScreen {
                api.invokeOnRequestFullScreen()
            }
            onRefreshed {
                api.invokeOnRefreshed(it)
            }
        }
    }

    val mSinkExt: WifiSinkExt by lazy { WifiSinkExt(MiraCastApplication.context, callback) }

    companion object {
        private const val TAG = "WifiSinkDisplayManager"
        private val instance = WifiSinkDisplayManager()
        @JvmStatic
        fun getInstance(): WifiSinkDisplayManager {
            return instance
        }
    }

    fun register(callback: ReceiverApiModel.() -> Unit) {
        callbacks.add(callback)
    }

    fun unRegister(callback: ReceiverApiModel.() -> Unit) {
        callbacks.remove(callback)
    }

    fun onStart(activity: Activity) {
        mSinkExt.onStart(activity)
    }

    fun onStop(activity: Activity) {
        mSinkExt.onStop(activity)
    }
}