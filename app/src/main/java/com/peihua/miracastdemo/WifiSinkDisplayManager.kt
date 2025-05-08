package com.peihua.miracastdemo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.view.Surface
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.peihua.miracastdemo.utils.ActivityEmbeddingUtils
import com.peihua.miracastdemo.utils.Logcat
import com.peihua.miracastdemo.utils.dLog
import com.peihua.miracastdemo.utils.eLog

class WifiSinkDisplayManager() {
    private val callbacks = mutableMapOf<WifiSinkApiModel.() -> Unit, WifiSinkApiModel>()
    private val apiModel: WifiSinkApiModel.() -> Unit = {
        onConnection {
            callbacks.forEach { (key, api) ->
                api.invokeOnConnection(it)
            }
        }
        onDisconnection {
            callbacks.forEach { (key, api) ->
                api.invokeOnDisconnection(it)
            }
        }
        onChangeStatus {
            callbacks.forEach { (key, api) ->
                api.invokeOnChangeStatus(it)
            }
        }
        onChangeUiPortrait {
            callbacks.forEach { (key, api) ->
                api.invokeOnChangeUiPortrait(it)
            }
        }
        onAddWfdSinkGuide {
            callbacks.forEach { (key, api) ->
                api.invokeOnAddWfdSinkGuide()
            }
        }
        onRequestFullScreen {
            callbacks.forEach { (key, api) ->
                api.invokeOnRequestFullScreen()
            }
        }
        onRefreshed {
            callbacks.forEach { (key, api) ->
                api.invokeOnRefreshed(it)
            }
        }
    }
    private var mOrientationSettings = 0
    val mSinkExt: WifiSinkExt by lazy { WifiSinkExt(MiraCastApplication.context, apiModel) }
    private val mWifiP2pManager: WifiP2pManager by lazy {
        MiraCastApplication.context.getSystemService(
            WifiP2pManager::class.java
        )
    }
    private val mWifiP2pChannel: WifiP2pManager.Channel by lazy {
        mWifiP2pManager.initialize(
            MiraCastApplication.context,
            Looper.getMainLooper(),
            null
        )
    }
    val wifiP2pChannel: WifiP2pManager.Channel
        get() = mWifiP2pChannel

    companion object {
        private const val TAG = "WifiSinkDisplayManager"
        private val instance = WifiSinkDisplayManager()

        @JvmStatic
        fun getInstance(): WifiSinkDisplayManager {
            return instance
        }
    }

    fun register(callback: WifiSinkApiModel.() -> Unit) {
        callbacks[callback] = (WifiSinkApiModel().apply(callback))
    }

    fun unRegister(callback: WifiSinkApiModel.() -> Unit) {
        dLog { "unRegister>>>callbacks.size:${callbacks.size}" }
        callbacks.remove(callback)
        dLog { "unRegister>>>callbacks.size:${callbacks.size}" }
    }

    @SuppressLint("MissingPermission", "NewApi")
    fun onStart() {
        val context = MiraCastApplication.context
        mOrientationSettings =
            Settings.System.getInt(context.contentResolver, Settings.System.USER_ROTATION, 0)
        dLog {  "WfdChangeResolution onPreferenceClick ---->mOrientationSettings--->$mOrientationSettings" }
        ActivityEmbeddingUtils.setOrientationSettings(mOrientationSettings)
        ActivityEmbeddingUtils.setIsSplitEnabled(false)
        Settings.System.putInt(context.contentResolver, Settings.System.USER_ROTATION, 1)
        mSinkExt.onStart(true)
        if (isWifiDisplayCertificationOn) {
            setListenMode(true)
        }
    }

    @SuppressLint("MissingPermission", "NewApi")
    fun onStop() {
        mSinkExt.onStop()
        if (isWifiDisplayCertificationOn) {
            setListenMode(false)
            mWifiP2pChannel.close()
        }
    }

    val isSinkMode: Boolean
        get() = mSinkExt.isSinkMode

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

    fun sendUibcEvent(eventDesc: String?) {
        mSinkExt.sendUibcEvent(eventDesc)
    }

    @SuppressLint("NewApi", "MissingPermission")
    fun disconnectWfdSinkConnection() {
//        onStop(CastApplication.context)
        mSinkExt.disconnectWfdSinkConnection()

    }

    var isListenMode: Boolean = true
        private set
    val isWifiDisplayCertificationOn: Boolean
        get() = Settings.Global.getInt(
            MiraCastApplication.context.contentResolver,
            Settings.Global.WIFI_DISPLAY_CERTIFICATION_ON, 0
        ) != 0

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NEARBY_WIFI_DEVICES]
    )
    fun setListenMode(enable: Boolean) {
        isListenMode = enable
        dLog { "Setting listen mode to: $enable" }
        val listener: WifiP2pManager.ActionListener = object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                dLog { "Successfully " + (if (enable) "entered" else "exited") + " listen mode." }
            }

            override fun onFailure(reason: Int) {
                eLog { "Failed to " + (if (enable) "entered" else "exited") + " listen mode with reason $reason." }
            }
        }
        if (enable) {
            mWifiP2pManager.startListening(mWifiP2pChannel, listener)
        } else {
            mWifiP2pManager.stopListening(mWifiP2pChannel, listener)
        }
    }
}