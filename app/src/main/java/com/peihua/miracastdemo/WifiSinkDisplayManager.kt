package com.peihua.miracastdemo

import android.app.Activity
import android.content.Context
import android.view.Surface
import com.peihua.miracastdemo.utils.Logcat

class WifiSinkDisplayManager() {
    val callbacks = mutableListOf<ReceiverApiModel.() -> Unit>()
    val apiModel: ReceiverApiModel.() -> Unit = {
        onConnection {
            callbacks.forEach { api ->
                val apiModel = ReceiverApiModel().apply(api)
                apiModel.invokeOnConnection(it)
            }
        }
        onDisconnection {
            callbacks.forEach { api ->
                val apiModel = ReceiverApiModel().apply(api)
                apiModel.invokeOnDisconnection(it)
            }
        }
        onChangeStatus {
            callbacks.forEach { api ->
                val apiModel = ReceiverApiModel().apply(api)
                apiModel.invokeOnChangeStatus(it)
            }
        }
        onChangeUiPortrait {
            callbacks.forEach { api ->
                val apiModel = ReceiverApiModel().apply(api)
                apiModel.invokeOnChangeUiPortrait(it)
            }
        }
        onAddWfdSinkGuide {
            callbacks.forEach { api ->
                val apiModel = ReceiverApiModel().apply(api)
                apiModel.invokeOnAddWfdSinkGuide()
            }
        }
        onRequestFullScreen {
            callbacks.forEach { api ->
                val apiModel = ReceiverApiModel().apply(api)
                apiModel.invokeOnRequestFullScreen()
            }
        }
        onRefreshed {
            callbacks.forEach { api ->
                val apiModel = ReceiverApiModel().apply(api)
                apiModel.invokeOnRefreshed(it)
            }
        }
    }
    val mSinkExt: WifiSinkExt by lazy { WifiSinkExt(MiraCastApplication.context, apiModel) }
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

    fun onStart(activity: Activity, isEnable: Boolean) {
        mSinkExt.onStart(activity, isEnable)
    }

    fun onStop(activity: Activity) {
        mSinkExt.onStop(activity)
    }

    fun onRegister(context: Context) {
        mSinkExt.onRegister(context)
    }

    fun onUnRegister(context: Context) {
        mSinkExt.unRegister(context)
    }

    fun setupWfdSinkConnection(surface: Surface?) {
        mSinkExt.setupWfdSinkConnection(surface)
    }

    fun setWfdMode(sink: Boolean) {
        Logcat.d("Miracast_${TAG}", "setWfdMode $sink")
        mSinkExt.setWfdMode(sink)
        Logcat.d("Miracast_${TAG}", "after setWfdMode $sink,isSinkMode: ${mSinkExt.isSinkMode}")
    }

    fun waitWfdSinkConnection(surface: Surface?) {
        mSinkExt.waitWfdSinkConnection(surface)
        Logcat.d("Miracast_${TAG}", "waitWfdSinkConnection $surface")
    }

    fun disconnectWfdSinkConnection() {
        mSinkExt.disconnectWfdSinkConnection()
    }
}