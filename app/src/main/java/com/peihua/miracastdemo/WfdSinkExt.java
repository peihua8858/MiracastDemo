package com.peihua.miracastdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.hardware.display.WifiDisplayStatus;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.peihua.miracastdemo.utils.Logcat;

/**
 * MTK sink setting helper class.
 */
public class WfdSinkExt {
    private static final String TAG = "WfdSinkExt";
    private static final String ACTION_WFD_PORTRAIT = "com.mediatek.wfd.portrait";
    //DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED
    public static final String ACTION_WIFI_DISPLAY_STATUS_CHANGED = "android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED";
    private Context mContext;
    private DisplayManager mDisplayManager;
    // WFD sink supported
    private static final String WFD_NAME = "wifi_display";
    private static final String KEY_WFD_SINK_GUIDE = "wifi_display_hide_guide";
    private WfdSinkSurfaceFragment mSinkFragment;
    private Toast mSinkToast;
    private int mPreWfdState = -1;
    private boolean mUiPortrait = false;
    private boolean mRefreshed = false;

    /**
     * Empty constructor.
     */
    public WfdSinkExt() {
    }

    /**
     * WFD sink helper class.
     *
     * @param context WFD sink setting context
     */
    public WfdSinkExt(Context context) {
        mContext = context;
        mDisplayManager = (DisplayManager) mContext
                .getSystemService(Context.DISPLAY_SERVICE);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v("@M_" + TAG, "receive action: " + action);
            int width = 0;
            int height = 0;
            if (ACTION_WIFI_DISPLAY_STATUS_CHANGED
                    .equals(action)) {
                width = intent.getIntExtra("width", 0);
                height = intent.getIntExtra("height", 0);
                Log.d("@M_" + TAG, "receive width" + width + "-----height" + height);
                if (mSinkFragment != null && width != 0 && height != 0 && mRefreshed == false) {
                    mSinkFragment.refresh(width, height);
                    mRefreshed = true;
                }
                handleWfdStatusChanged(mDisplayManager.getWifiDisplayStatus());
            } else if (ACTION_WFD_PORTRAIT.equals(action)) {
                mUiPortrait = true;
            }
        }
    };

    /**
     * Called when activity started.
     */
    public void onStart() {
        Log.d("@M_" + TAG, "onStart");
        if (FeatureOption.MTK_WFD_SINK_SUPPORT) {
            WifiDisplayStatus wfdStatus = mDisplayManager
                    .getWifiDisplayStatus();
            handleWfdStatusChanged(wfdStatus);
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_WIFI_DISPLAY_STATUS_CHANGED);
            filter.addAction(ACTION_WFD_PORTRAIT);
            mContext.registerReceiver(mReceiver, filter);
        }
    }

    /**
     * Called when activity stopped.
     */
    public void onStop() {
        Log.d("@M_" + TAG, "onStop");
        if (FeatureOption.MTK_WFD_SINK_SUPPORT) {
            mContext.unregisterReceiver(mReceiver);
            mRefreshed = false;
        }
    }

    /**
     * Setup WFD sink connection, called when WFD sink surface is available.
     *
     * @param surface The surface will be sent to native to draw source image
     */
    public void setupWfdSinkConnection(Surface surface) {
        Log.d("@M_" + TAG, "setupWfdSinkConnection");
        setWfdMode(true);
        waitWfdSinkConnection(surface);
    }

    /**
     * Disconnect WFD sink connection, called when WFD sink surface will exit.
     */
    public void disconnectWfdSinkConnection() {
        Log.d("@M_" + TAG, "disconnectWfdSinkConnection");
        mDisplayManager.disconnectWifiDisplay();
        setWfdMode(false);
        Log.d("@M_" + TAG, "after disconnectWfdSinkConnection");
    }

    /**
     * Add sink fragment for call back.
     *
     * @param fragment WFD sink fragment
     */
    public void registerSinkFragment(WfdSinkSurfaceFragment fragment) {
        mSinkFragment = fragment;
    }

    private void handleWfdStatusChanged(WifiDisplayStatus status) {
        boolean bStateOn = (status != null && status.getFeatureState()
                == WifiDisplayStatus.FEATURE_STATE_ON);
        Log.d("@M_" + TAG, "handleWfdStatusChanged bStateOn: " + bStateOn);
        if (bStateOn) {
            int wfdState = status.getActiveDisplayState();
            Log.d("@M_" + TAG, "handleWfdStatusChanged wfdState: " + wfdState);
            // if (mPreWfdState != wfdState) {
            handleWfdStateChanged(wfdState, isSinkMode());
            mPreWfdState = wfdState;
            // }
        } else {
            // if (mPreWfdState != -1) {
            handleWfdStateChanged(
                    WifiDisplayStatus.DISPLAY_STATE_NOT_CONNECTED,
                    isSinkMode());
            // }
            mPreWfdState = -1;
        }
    }

    private void handleWfdStateChanged(int wfdState, boolean sinkMode) {
        switch (wfdState) {
            case WifiDisplayStatus.DISPLAY_STATE_NOT_CONNECTED:
                if (sinkMode) {
                    Log.d("@M_" + TAG, "dismiss fragment");
                    if (mSinkFragment != null) {
                        mSinkFragment.dismissAllowingStateLoss();
                    }
                    setWfdMode(false);
                }
                if (mPreWfdState == WifiDisplayStatus.DISPLAY_STATE_CONNECTED) {
                    showToast(false);
                }
                mUiPortrait = false;
                break;
            case WifiDisplayStatus.DISPLAY_STATE_CONNECTING:
                break;
            case WifiDisplayStatus.DISPLAY_STATE_CONNECTED:
                if (sinkMode) {
                    Log.d("@M_" + TAG, "mUiPortrait: " + mUiPortrait);
                    //mSinkFragment.requestOrientation(mUiPortrait);
                    SharedPreferences preferences = mContext.getSharedPreferences(
                            WFD_NAME, Context.MODE_PRIVATE);
                    boolean showGuide = preferences.getBoolean(KEY_WFD_SINK_GUIDE, true);
                    if (showGuide) {
                        if (mSinkFragment != null) {
                            mSinkFragment.addWfdSinkGuide();
                            preferences.edit()
                                    .putBoolean(KEY_WFD_SINK_GUIDE, false).commit();
                        }
                    }
                    if (mPreWfdState != WifiDisplayStatus.DISPLAY_STATE_CONNECTED) {
                        showToast(true);
                    }
                }
                mUiPortrait = false;
                break;
            default:
                break;
        }
    }

    private void showToast(boolean connected) {
        if (mSinkToast != null) {
            mSinkToast.cancel();
        }
        mSinkToast = Toast.makeText(mContext,
                connected ? R.string.wfd_sink_toast_enjoy
                        : R.string.wfd_sink_toast_disconnect,
                connected ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        mSinkToast.show();
    }

    private boolean isSinkMode() {
        return mDisplayManager.isSinkEnabled();
    }

    private void setWfdMode(boolean sink) {
        Logcat.d("Miracast_" + TAG, "setWfdMode " + sink);
        mDisplayManager.enableSink(sink);
    }

    private void waitWfdSinkConnection(Surface surface) {
        mDisplayManager.waitWifiDisplayConnection(surface);
        Logcat.d("Miracast_" + TAG, "waitWfdSinkConnection " + surface);
    }

    /**
     * Send UIBC event to framework.
     *
     * @param eventDesc UIBC event description string
     */
    public void sendUibcEvent(String eventDesc) {
        mDisplayManager.sendUibcInputEvent(eventDesc);
        Logcat.d("Miracast_" + TAG, "sendUibcInputEvent " + eventDesc);
    }
}
