package com.peihua.miracastdemo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.hardware.display.WifiDisplayStatus
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.core.content.edit
import com.peihua.miracastdemo.utils.Logcat

class WifiSinkExt(private val mContext: Context, callback: ReceiverApiModel.() -> Unit) :
    BroadcastReceiver(), SurfaceHolder.Callback {
    companion object {
        private const val TAG = "WifiSinkExt"
        private const val ACTION_WFD_PORTRAIT = "com.mediatek.wfd.portrait"
        const val ACTION_WIFI_DISPLAY_STATUS_CHANGED =
            "android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED"
        private const val WFD_NAME = "wifi_display";
        private const val KEY_WFD_SINK_GUIDE = "wifi_display_hide_guide";
    }

    private var mSurfaceShowing = false;
    private val apiModel = ReceiverApiModel().apply(callback)
    private val mDisplayManager =
        mContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    private var mSinkToast: Toast? = null;
    private var mPreWfdState = -1;
    private var mUiPortrait = false;
    private var mRefreshed = false;
    val mSurfaceView: SurfaceView by lazy { SurfaceView(mContext) };
    init {
//        mSurfaceView.holder.addCallback(this)
    }
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("UnsafeImplicitIntentLaunch")
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED) {
                val status = mDisplayManager.wifiDisplayStatus
                val bStateOn =
                    status != null && status.featureState == WifiDisplayStatus.FEATURE_STATE_ON
                if (bStateOn) {
                    val wfdState = status?.activeDisplayState
                    if (wfdState == WifiDisplayStatus.DISPLAY_STATE_CONNECTED) {
                        val intent = Intent("mediatek.settings.WFD_SINK_SETTINGS");
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                        context?.startActivity(intent)
                    }
                }
            }
        }
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        Logcat.v("@M_${TAG}", "receive action: $action")
        var width = 0;
        var height = 0;
        if (ACTION_WIFI_DISPLAY_STATUS_CHANGED == action) {
            width = intent.getIntExtra("width", 0)
            height = intent.getIntExtra("height", 0)
            Logcat.d("@M_${TAG}", "receive width$width-----height$height");
            if (width != 0 && height != 0) {
                apiModel.invokeOnRefreshed(WifiSinkDisplayStatus(width, height))
            }
            context?.handleWfdStatusChanged(wifiDisplayStatus);
        } else if (ACTION_WFD_PORTRAIT == action) {
            mUiPortrait = true;
        }
    }

    fun handleWfdStatusChanged(status: WifiDisplayStatus?, context: Context) {
        context.handleWfdStatusChanged(status)
    }

    fun Context.handleWfdStatusChanged(status: WifiDisplayStatus?) {
        val bStateOn = status != null && status.featureState == WifiDisplayStatus.FEATURE_STATE_ON
        Logcat.d("@M_${TAG}", "handleWfdStatusChanged bStateOn: $bStateOn")
        if (bStateOn) {
            val wfdState = status!!.activeDisplayState
            Logcat.d("@M_${TAG}", "handleWfdStatusChanged wfdState: $wfdState")
            handleWfdStateChanged(wfdState, isSinkMode)
            apiModel.invokeOnChangeStatus(wfdState)
            mPreWfdState = wfdState
        } else {
            handleWfdStateChanged(WifiDisplayStatus.DISPLAY_STATE_NOT_CONNECTED, isSinkMode)
            apiModel.invokeOnChangeStatus(WifiDisplayStatus.DISPLAY_STATE_NOT_CONNECTED)
            mPreWfdState = -1
        }
    }

    private fun Context.handleWfdStateChanged(wfdState: Int, sinkMode: Boolean) {
        Logcat.d("@M_${TAG}", "handleWfdStateChanged wfdState: $wfdState, sinkMode: $sinkMode,isSinkMode: $isSinkMode")
        when (wfdState) {
            WifiDisplayStatus.DISPLAY_STATE_NOT_CONNECTED -> {
                if (sinkMode) {
                    Logcat.d("@M_${TAG}", "dismiss fragment")
                    apiModel.invokeOnDisconnection(wfdState)
                    setWfdMode(false)
                }
                if (mPreWfdState == WifiDisplayStatus.DISPLAY_STATE_CONNECTED) {
                    showToast(false)
                }
                mUiPortrait = false
            }

            WifiDisplayStatus.DISPLAY_STATE_CONNECTING -> {
                if (sinkMode) {
                    apiModel.invokeOnConnection(wfdState)
                }
            }
            WifiDisplayStatus.DISPLAY_STATE_CONNECTED -> {
                if (sinkMode) {
                    Logcat.d("@M_${TAG}", "mUiPortrait: $mUiPortrait")
                    apiModel.invokeOnChangeUiPortrait(mUiPortrait)
                    val preferences = getSharedPreferences(WFD_NAME, Context.MODE_PRIVATE)
                    val showGuide = preferences.getBoolean(KEY_WFD_SINK_GUIDE, true)
                    if (showGuide) {
                        apiModel.invokeOnAddWfdSinkGuide()
                        preferences.edit { putBoolean(KEY_WFD_SINK_GUIDE, false) }
                    }
                    if (mPreWfdState != WifiDisplayStatus.DISPLAY_STATE_CONNECTED) {
                        showToast(true)
                    }
                    apiModel.invokeOnConnection(wfdState)
                    apiModel.invokeOnChangeStatus(wfdState)

                }
                mUiPortrait = false
            }
        }
    }

    val wifiDisplayStatus: WifiDisplayStatus?
        get() = mDisplayManager.wifiDisplayStatus

    fun onStart(activity: Activity) {
        onStop(activity)
        setWfdMode(true)
//        mSurfaceView.holder.addCallback(this)
        registerInnerReceiver()
        onRegister(activity)
    }

    private fun registerInnerReceiver() {
        if (FeatureOption.MTK_WFD_SINK_SUPPORT) {
            val filter = IntentFilter()
            filter.addAction(ACTION_WIFI_DISPLAY_STATUS_CHANGED)
            mContext.registerReceiver(mReceiver, filter)
        }
    }

    fun onStop(activity: Activity) {
        unRegister(activity)
//        mSurfaceView.holder.removeCallback(this)
    }

    /**
     * Called when activity started.
     */
    fun onRegister(context: Context) {
        Logcat.d("@M_${TAG}", "onRegister>>>>FeatureOption.MTK_WFD_SINK_SUPPORT: ${FeatureOption.MTK_WFD_SINK_SUPPORT}")
        if (FeatureOption.MTK_WFD_SINK_SUPPORT) {
            val wfdStatus = wifiDisplayStatus
            context.handleWfdStatusChanged(wfdStatus);
            val filter = IntentFilter();
            filter.addAction(ACTION_WIFI_DISPLAY_STATUS_CHANGED);
            filter.addAction(ACTION_WFD_PORTRAIT)
            context.registerReceiver(this, filter);
        }
    }

    fun unRegister(context: Context) {
        Logcat.d("@M_${TAG}", "onStop");
        if (FeatureOption.MTK_WFD_SINK_SUPPORT) {
            try {
                context.unregisterReceiver(this);
            } catch (e: Exception) {
               e.printStackTrace()
            }
            mRefreshed = false;
        }
    }

    /**
     * Setup WFD sink connection, called when WFD sink surface is available.
     *
     * @param surface The surface will be sent to native to draw source image
     */
    fun setupWfdSinkConnection(surface: Surface?) {
        Logcat.d(TAG, "setupWfdSinkConnection")
        setWfdMode(true)
        waitWfdSinkConnection(surface)
    }

    /**
     * Disconnect WFD sink connection, called when WFD sink surface will exit.
     */
    fun disconnectWfdSinkConnection() {
        Logcat.d(TAG, "disconnectWfdSinkConnection")
        mDisplayManager.disconnectWifiDisplay()
        setWfdMode(false)
        Logcat.d(TAG, "after disconnectWfdSinkConnection")
    }

    private fun showToast(connected: Boolean) {
        mSinkToast?.cancel()
        mSinkToast = Toast.makeText(
            mContext,
            if (connected) R.string.wfd_sink_toast_enjoy else R.string.wfd_sink_toast_disconnect,
            if (connected) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).apply { show() }
    }

    var isSinkMode: Boolean
        get() = mDisplayManager.isSinkEnabled ?: false
        set(value) = mDisplayManager.enableSink(value)

    fun setWfdMode(sink: Boolean) {
        Logcat.d("Miracast_$TAG", "setWfdMode $sink")
        mDisplayManager.enableSink(sink)
        Logcat.d("Miracast_$TAG", "after setWfdMode $sink,isSinkMode: $isSinkMode")
    }

    fun waitWfdSinkConnection(surface: Surface?) {
        mDisplayManager.waitWifiDisplayConnection(surface)
        Logcat.d("Miracast_$TAG", "waitWfdSinkConnection $surface")
    }

    /**
     * Send UIBC event to framework.
     *
     * @param eventDesc UIBC event description string
     */
    fun sendUibcEvent(eventDesc: String?) {
        mDisplayManager.sendUibcInputEvent(eventDesc)
        Logcat.d("Miracast_$TAG", "sendUibcInputEvent $eventDesc")
    }

    private fun disconnect() {
        if (mSurfaceShowing) {
            disconnectWfdSinkConnection();
        }
        mSurfaceShowing = false;
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        Logcat.d("@M_${TAG}", "surfaceCreated")
        if (holder != null) {
            if (!mSurfaceShowing) {
                setupWfdSinkConnection(holder.surface)
            }
            mSurfaceShowing = true;
        }
    }

    override fun surfaceChanged(
        holder: SurfaceHolder?,
        format: Int,
        width: Int,
        height: Int,
    ) {
        Logcat.d("@M_${TAG}", "surfaceChanged")
        apiModel.invokeOnRequestFullScreen();
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Logcat.d("@M_${TAG}", "surfaceDestroyed")
        disconnect()
    }

}



data class WifiSinkDisplayStatus(var width: Int, var height: Int)


class ReceiverApiModel {
    private var onRefreshed: ((WifiSinkDisplayStatus) -> Unit?)? = null

    private var onConnection: ((Int) -> Unit?)? = null
    private var onDisconnection: ((Int) -> Unit?)? = null

    private var onChangeStatus: ((Int) -> Unit?)? = null
    private var onRequestFullScreen: (() -> Unit?)? = null
    private var onChangeUiPortrait: ((Boolean) -> Unit?)? = null
    private var addWfdSinkGuide: (() -> Unit?)? = null
    infix fun onConnection(onConnection: (Int) -> Unit?): ReceiverApiModel {
        this.onConnection = onConnection
        return this
    }
    infix fun onDisconnection(onDisconnection: (Int) -> Unit?): ReceiverApiModel {
        this.onDisconnection = onDisconnection
        return this
    }
    infix fun onRequestFullScreen(onRequestFullScreen: (() -> Unit)?): ReceiverApiModel {
        this.onRequestFullScreen = onRequestFullScreen
        return this
    }

    infix fun onRefreshed(onRefreshed: ((WifiSinkDisplayStatus) -> Unit?)?): ReceiverApiModel {
        this.onRefreshed = onRefreshed
        return this
    }


    infix fun onChangeStatus(onChangeStatus: ((Int) -> Unit)?): ReceiverApiModel {
        this.onChangeStatus = onChangeStatus
        return this
    }

    infix fun onChangeUiPortrait(onChangeUiPortrait: ((Boolean) -> Unit)?): ReceiverApiModel {
        this.onChangeUiPortrait = onChangeUiPortrait
        return this
    }

    infix fun onAddWfdSinkGuide(addWfdSinkGuide: (() -> Unit?)?): ReceiverApiModel {
        this.addWfdSinkGuide = addWfdSinkGuide
        return this
    }

    fun invokeOnRefreshed(status: WifiSinkDisplayStatus) {
        this.onRefreshed?.invoke(status)
    }

    fun invokeOnChangeStatus(status: Int) {
        this.onChangeStatus?.invoke(status)
    }

    fun invokeOnChangeUiPortrait(isPortrait: Boolean) {
        this.onChangeUiPortrait?.invoke(isPortrait)
    }

    fun invokeOnAddWfdSinkGuide() {
        this.addWfdSinkGuide?.invoke()
    }

    fun invokeOnRequestFullScreen() {
        this.onRequestFullScreen?.invoke()
    }
    fun invokeOnConnection(status: Int) {
        this.onConnection?.invoke(status)
    }
    fun invokeOnDisconnection(status: Int) {
        this.onDisconnection?.invoke(status)
    }
}