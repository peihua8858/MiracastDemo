package com.peihua.miracastdemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.peihua.miracastdemo.utils.ActivityEmbeddingUtils;

public class FullScreenDialog extends Dialog implements
        SurfaceHolder.Callback {
    public static final String TAG = "FullScreenDialog";
    //StatusBarManager.DISABLE_NONE
    public static final int DISABLE_NONE = 0x00000000;
    private static final int ORIENTATION_NOT_BACKUP = -100;
    private int mOrientationBak = ORIENTATION_NOT_BACKUP;
    private WfdSinkExt mExt;
    private SurfaceView mSinkView;
    private WfdSinkLayout mSinkViewLayout;
    private Activity mActivity;
    private int mSystemUiBak;
    private boolean mSurfaceShowing = false;
    private boolean mGuideShowing = false;
    private boolean mCountdownShowing = false;

    public FullScreenDialog(Activity activity) {
        //super(activity,
        //        android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        super(activity, R.style.dialog_fullscreen);
        mActivity = activity;
    }

    public void setWfdSinkExt(WfdSinkExt mExt) {
        this.mExt = mExt;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("@M_" + TAG, "dialog onCreate");
        ViewGroup.LayoutParams viewParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mSinkViewLayout = new WfdSinkLayout(mActivity);
        mSinkViewLayout.setWfdSinkExt(mExt);
        mSinkViewLayout.setFocusableInTouchMode(true);
        setContentView(mSinkViewLayout);
        mSinkView = new SurfaceView(mActivity);
        mSinkView.setFocusableInTouchMode(false);
        mSinkView.setFocusable(false);
        mSinkViewLayout.addView(mSinkView, viewParams);
    }

    @Override
    protected void onStart() {
        Log.d("@M_" + TAG, "dialog onStart");
        super.onStart();
        mSystemUiBak = mSinkViewLayout.getSystemUiVisibility();
        mSinkViewLayout.setOnFocusGetCallback(new Runnable() {
            @Override
            public void run() {
                requestFullScreen(mSystemUiBak);
            }
        });
        mSinkViewLayout.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int i) {
                        Log.i("@M_" + TAG, "onSystemUiVisibilityChange: " + i);
                        if (i == 0) {
                            mSinkViewLayout.setFullScreenFlag(false);
                            // Workaround for WMS timing issue
                            if (mSinkViewLayout.mHasFocus) {
                                requestFullScreen(mSystemUiBak);
                            }
                        } else {
                            mSinkViewLayout.setFullScreenFlag(true);
                        }
                    }
                });
        requestFullScreen(mSystemUiBak);
        mActivity.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSinkView.getHolder().addCallback(this);
    }

    @Override
    protected void onStop() {
        Log.d("@M_" + TAG, "dialog onStop");
        mSinkViewLayout.setSystemUiVisibility(mSystemUiBak);
        mActivity.getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSinkView.getHolder().removeCallback(this);
        restoreOrientation();
        super.onStop();
    }

    @Override
    public void dismiss() {
        Log.d("@M_" + TAG, "dialog dismiss");
        disconnect();
        mActivity.finish();
        super.dismiss();
    }

    @Override
    public void onBackPressed() {
        Log.d("@M_" + TAG, "dialog onBackPressed");
        if (mGuideShowing) {
            removeWfdSinkGuide();
            return;
        }
        disconnect();
        super.onBackPressed();
    }

    private void disconnect() {
        if (mSurfaceShowing) {
            mExt.disconnectWfdSinkConnection();
        }
        mSurfaceShowing = false;
        if (mGuideShowing) {
            removeWfdSinkGuide();
        }
    }

    private void removeWfdSinkGuide() {
        if (mGuideShowing) {
            View guide = (View) mSinkViewLayout
                    .getTag(R.string.wfd_sink_guide_content);
            if (guide != null) {
                mSinkViewLayout.removeView(guide);
                mSinkViewLayout.setTag(R.string.wfd_sink_guide_content, null);
            }
        }
        mSinkViewLayout.setCatchEvents(true);
        mGuideShowing = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        Log.d("@M_" + TAG, "surface changed: " + width + "x" + height);
        int systemUiVis = mSinkViewLayout.getSystemUiVisibility();
        if (mSinkViewLayout.mHasFocus &&
                (systemUiVis & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
            requestFullScreen(systemUiVis);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("@M_" + TAG, "surface created");
        if (!mSurfaceShowing) {
            mExt.setupWfdSinkConnection(holder.getSurface());
        }
        mSurfaceShowing = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("@M_" + TAG, "surface destroyed");
        disconnect();
    }

    private void requestFullScreen(int systemUi) {
        if (Build.VERSION.SDK_INT >= 14) {
            systemUi |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        if (Build.VERSION.SDK_INT >= 16) {
            systemUi |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        if (Build.VERSION.SDK_INT >= 18) {
            systemUi |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            //| View.SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED;
        }
        final int newUiOptions = systemUi;
        mSinkViewLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("@M_" + TAG, "request full screen: " + Integer.toHexString(newUiOptions));
                mSinkViewLayout.setSystemUiVisibility(newUiOptions);
            }
        }, 500);
    }

    /**
     * Request orientation when enter WFD sink full screen view.
     *
     * @param isPortrait Request orientation to portrait or not
     */
    public void requestOrientation(boolean isPortrait) {
        mOrientationBak = mActivity.getRequestedOrientation();
        mActivity
                .setRequestedOrientation(isPortrait ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    /**
     * Restore orientation when exit WFD sink full screen view.
     */
    public void restoreOrientation() {
        /*if (mOrientationBak != ORIENTATION_NOT_BACKUP) {
            mActivity.setRequestedOrientation(mOrientationBak);
        }*/
        ActivityEmbeddingUtils.setIsSplitEnabled(true);
        Log.d(TAG, "restoreOrientation ---->ActivityEmbeddingUtils.getOrientationSettings()--->" + ActivityEmbeddingUtils.getOrientationSettings());
        Settings.System.putInt(getContext().getContentResolver(), Settings.System.USER_ROTATION, ActivityEmbeddingUtils.getOrientationSettings());
    }
}
