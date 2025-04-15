//package com.peihua.miracastdemo
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import android.hardware.display.WifiDisplayStatus
//import android.widget.Toast
//import androidx.core.content.edit
//import com.peihua.miracastdemo.utils.Logcat
//import com.peihua.miracastdemo.utils.showToast
//
//class SinkSurfaceBroadcastReceiver(
//    val sinkExt: WfdSinkExt,
//    val callback: ReceiverApiModel.() -> Unit,
//) : BroadcastReceiver() {
//    val apiModel = ReceiverApiModel().apply(callback)
//
//    companion object {
//        private const val TAG = "SinkSurfaceBroadcastReceiver"
//        private const val ACTION_WFD_PORTRAIT = "com.mediatek.wfd.portrait"
//        const val ACTION_WIFI_DISPLAY_STATUS_CHANGED =
//            "android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED"
//        private const val WFD_NAME = "wifi_display";
//        private const val KEY_WFD_SINK_GUIDE = "wifi_display_hide_guide";
//    }
//
//    private var mPreWfdState = -1;
//    private var mUiPortrait = false;
//    private var mRefreshed = false;
//    private var mSinkToast: Toast? = null;
//    override fun onReceive(context: Context?, intent: Intent?) {
//        val action = intent?.action
//        Logcat.v("@M_$TAG", "receive action: $action")
//        var width = 0;
//        var height = 0;
//        if (ACTION_WIFI_DISPLAY_STATUS_CHANGED == action) {
//            width = intent.getIntExtra("width", 0)
//            height = intent.getIntExtra("height", 0)
//            Logcat.d("@M_$TAG", "receive width$width-----height$height");
//            if (width != 0 && height != 0) {
//                apiModel.invokeOnRefreshed(WifiSinkDisplayStatus(width, height))
//            }
//            context?.handleWfdStatusChanged(sinkExt.wifiDisplayStatus);
//        } else if (ACTION_WFD_PORTRAIT == action) {
//            mUiPortrait = true;
//        }
//    }
//
//    private fun Context.handleWfdStatusChanged(status: WifiDisplayStatus?) {
//        val bStateOn = status != null && status.featureState == WifiDisplayStatus.FEATURE_STATE_ON
//        Logcat.d("@M_$TAG", "handleWfdStatusChanged bStateOn: $bStateOn")
//        if (bStateOn) {
//            val wfdState = status!!.activeDisplayState
//            Logcat.d("@M_$TAG", "handleWfdStatusChanged wfdState: $wfdState")
//            handleWfdStateChanged(wfdState, sinkExt.isSinkMode)
//            mPreWfdState = wfdState
//        } else {
//            handleWfdStateChanged(WifiDisplayStatus.DISPLAY_STATE_NOT_CONNECTED, sinkExt.isSinkMode)
//            mPreWfdState = -1
//        }
//    }
//
//    /**
//     * Called when activity started.
//     */
//    fun onRegister(context: Context) {
//        Logcat.d("@M_$TAG", "onRegister")
//        if (FeatureOption.MTK_WFD_SINK_SUPPORT) {
//            val wfdStatus = sinkExt.wifiDisplayStatus
//            context.handleWfdStatusChanged(wfdStatus);
//            val filter = IntentFilter();
//            filter.addAction(ACTION_WIFI_DISPLAY_STATUS_CHANGED);
//            filter.addAction(ACTION_WFD_PORTRAIT)
//            context.registerReceiver(this, filter);
//        }
//    }
//
//    fun unRegister(context: Context) {
//        Logcat.d("@M_$TAG", "onStop");
//        if (FeatureOption.MTK_WFD_SINK_SUPPORT) {
//            context.unregisterReceiver(this);
//            mRefreshed = false;
//        }
//    }
//
//    private fun Context.handleWfdStateChanged(wfdState: Int, sinkMode: Boolean) {
//        when (wfdState) {
//            WifiDisplayStatus.DISPLAY_STATE_NOT_CONNECTED -> {
//                if (sinkMode) {
//                    Logcat.d("@M_$TAG", "dismiss fragment")
//                    sinkExt.setWfdMode(false)
//                }
//                if (mPreWfdState == WifiDisplayStatus.DISPLAY_STATE_CONNECTED) {
//                    showToast(false)
//                }
//                mUiPortrait = false
//            }
//
//            WifiDisplayStatus.DISPLAY_STATE_CONNECTING -> {}
//            WifiDisplayStatus.DISPLAY_STATE_CONNECTED -> {
//                if (sinkMode) {
//                    Logcat.d("@M_$TAG", "mUiPortrait: $mUiPortrait")
//                    apiModel.invokeOnChangeUiPortrait(mUiPortrait)
//                    val preferences = getSharedPreferences(WFD_NAME, Context.MODE_PRIVATE)
//                    val showGuide = preferences.getBoolean(KEY_WFD_SINK_GUIDE, true)
//                    if (showGuide) {
//                        apiModel.invokeOnAddWfdSinkGuide()
//                        preferences.edit { putBoolean(KEY_WFD_SINK_GUIDE, false) }
//                    }
//                    if (mPreWfdState != WifiDisplayStatus.DISPLAY_STATE_CONNECTED) {
//                        showToast(true)
//                    }
//                    apiModel.invokeOnChangeStatus(wfdState)
//                }
//                mUiPortrait = false
//            }
//        }
//    }
//
//    private fun showToast(connected: Boolean) {
//        mSinkToast?.cancel()
//        val message =
//            if (connected) R.string.wfd_sink_toast_enjoy else R.string.wfd_sink_toast_disconnect
//        mSinkToast = message.showToast(if (connected) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
//    }
//}