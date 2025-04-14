/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2015. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */
package com.peihua.miracastdemo;

import android.app.Activity;
import android.app.Dialog;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.peihua.miracastdemo.utils.ActivityEmbeddingUtils;

/**
 * Dialog fragment for WFD sink surface.
 */
public  class WfdSinkSurfaceFragment extends Fragment implements
        SurfaceHolder.Callback, View.OnLongClickListener {
    private static final String TAG = WfdSinkSurfaceFragment.class
            .getSimpleName();
    private WfdSinkExt mExt;
    private SurfaceView mSinkView;
    private WfdSinkLayout mSinkViewLayout;
    private Dialog mDialog;
    private boolean mSurfaceShowing = false;
    private boolean mGuideShowing = false;
    private boolean mCountdownShowing = false;
    private Activity mActivity;
    //StatusBarManager.DISABLE_NONE
    public static final int DISABLE_NONE = 0x00000000;
    private static final int ORIENTATION_NOT_BACKUP = -100;
    private int mOrientationBak = ORIENTATION_NOT_BACKUP;
    // MTK WFD sink UIBC support
    private boolean mLatinCharTest = false;
    private int mTestLatinChar = 0xA0;
    private int mDialogID = 111;
    /** All the widgets to disable in the status bar */
    final private static int sWidgetsToDisable = StatusBarManager.DISABLE_EXPAND
            | StatusBarManager.DISABLE_NOTIFICATION_ICONS
            | StatusBarManager.DISABLE_NOTIFICATION_ALERTS
            | StatusBarManager.DISABLE_HOME
            | StatusBarManager.DISABLE_SEARCH
            | StatusBarManager.DISABLE_RECENT;
    /** The status bar where back/home/recent buttons are shown. */
    private StatusBarManager mStatusBar;
    /**
     * WFD sink surface fragment empty constructor.
     */
    public WfdSinkSurfaceFragment() {
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.d("@M_" + TAG, "onCreate,MTK_WFD_SINK_SUPPORT:" + FeatureOption.MTK_WFD_SINK_SUPPORT);
        if (icicle != null || !FeatureOption.MTK_WFD_SINK_SUPPORT) {
            Log.d("@M_" + TAG, "bundle is not null, recreate");
            dismissAllowingStateLoss();
            getActivity().finish();
            return;
        }
        mActivity = getActivity();
        mExt = new WfdSinkExt(mActivity);
        mExt.registerSinkFragment(this);
        // mActivity.getActionBar().hide();
//        mActivity.getActionBar().show();
        if (mDialog == null) {
            mDialog = new FullScreenDialog(getActivity());
        }
        // setShowsDialog(true);
        // showDialog(mDialogID);
    }
    @Override
    public void onStart() {
        Log.d("@M_" + TAG, "onStart");
        super.onStart();
        mExt.onStart();
        showDialog();
        // Disable the status bar, but do NOT disable back because the user needs a way to go
        mStatusBar = (StatusBarManager) mActivity.getSystemService(Context.STATUS_BAR_SERVICE);
        mStatusBar.disable(sWidgetsToDisable);
    }
    @Override
    public void onStop() {
        Log.d("@M_" + TAG, "onStop");
        mExt.onStop();
        dismissAllowingStateLoss();
        if (mStatusBar != null) {
            mStatusBar.disable(DISABLE_NONE);
        }
        mActivity.finish();
        super.onStop();
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

//   @Override
//    public Dialog onCreateDialog(int id) {
//        Log.d("@M_" + TAG, "mDialog is null? " + (mDialog == null));
//        mLatinCharTest = SystemProperties.get("wfd.uibc.latintest", "0").equals("1");
//        if (mDialog == null) {
//            mDialog = new FullScreenDialog(getActivity());
//        }
//        return mDialog;
//    }
    /**
     * Add WFD sink guide.
     */
    public void addWfdSinkGuide() {
        if (mGuideShowing) {
            return;
        }
        ViewGroup guide = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.wfd_sink_guide, null);
        Button btn = (Button) guide.findViewById(R.id.wfd_sink_guide_ok_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("@M_" + TAG, "ok button onClick");
                removeWfdSinkGuide();
            }
        });
        TextView tv = (TextView) guide
                .findViewById(R.id.wfd_sink_guide_content);
        tv.setText(getActivity().getResources().getString(
                R.string.wfd_sink_guide_content, "3"));
        mSinkViewLayout.addView(guide);
        mSinkViewLayout.setTag(R.string.wfd_sink_guide_content, guide);
        mSinkViewLayout.setCatchEvents(false);
        mGuideShowing = true;
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
    private void addCountdownView(String countdownNum) {
        if (mCountdownShowing) {
            return;
        }
        ViewGroup countdownView = (ViewGroup) LayoutInflater
                .from(getActivity()).inflate(R.layout.wfd_sink_countdown, null);
        TextView tv = (TextView) countdownView
                .findViewById(R.id.wfd_sink_countdown_num);
        tv.setText(countdownNum);
        mSinkViewLayout.addView(countdownView);
        mSinkViewLayout.setTag(R.id.wfd_sink_countdown_num, countdownView);
        mCountdownShowing = true;
    }
    private void removeCountDown() {
        if (mCountdownShowing) {
            View countdownView = (View) mSinkViewLayout
                    .getTag(R.id.wfd_sink_countdown_num);
            if (countdownView != null) {
                mSinkViewLayout.removeView(countdownView);
                mSinkViewLayout.setTag(R.id.wfd_sink_countdown_num, null);
            }
        }
        mCountdownShowing = false;
    }
    /**
     * Request orientation when enter WFD sink full screen view.
     *
     * @param isPortrait
     *            Request orientation to portrait or not
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
        Log.d(TAG,"restoreOrientation ---->ActivityEmbeddingUtils.getOrientationSettings()--->" + ActivityEmbeddingUtils.getOrientationSettings());
        Settings.System.putInt(getActivity().getContentResolver(), Settings.System.USER_ROTATION, ActivityEmbeddingUtils.getOrientationSettings());
    }

    public void refresh(int width,int height){
        Point size = new Point();
        mActivity.getWindowManager().getDefaultDisplay().getRealSize(size);
        int wm_width = size.x;
        int wm_height = size.y;
        int resize_width = (int)(wm_height*width/height);
        Log.d(TAG,"refresh width  : " + width + "  height : " + height
            + " wm width: " + wm_width+" wm height:" + wm_height + "resize_width" + resize_width);
        LayoutParams sinkViewFL = new LayoutParams(resize_width,wm_height,Gravity.CENTER);
        mSinkView.setLayoutParams(sinkViewFL);
    }

    /**
     * A full screen dialog to show WFD sink view.
     */
    private class FullScreenDialog extends Dialog {
        private Activity mActivity;
        private int mSystemUiBak;
        public FullScreenDialog(Activity activity) {
            //super(activity,
            //        android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            super(activity, R.style.dialog_fullscreen);
            mActivity = activity;
        }
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d("@M_" + TAG, "dialog onCreate");
            ViewGroup.LayoutParams viewParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mSinkViewLayout = new WfdSinkLayout(mActivity);
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
            mSinkView.getHolder().addCallback(WfdSinkSurfaceFragment.this);
        }
        @Override
        protected void onStop() {
            Log.d("@M_" + TAG, "dialog onStop");
            mSinkViewLayout.setSystemUiVisibility(mSystemUiBak);
            mActivity.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mSinkView.getHolder().removeCallback(WfdSinkSurfaceFragment.this);
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
    public void dismissAllowingStateLoss() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }
    public void showDialog(){
        if (mDialog != null) {
            mDialog.show();
        }
    }
    /**
     * WFD sink layout, maintain surface view, count down view, guide view and touch event.
     */
    private class WfdSinkLayout extends FrameLayout {
        private static final int GENERIC_INPUT_TYPE_ID_TOUCH_DOWN = 0;
        private static final int GENERIC_INPUT_TYPE_ID_TOUCH_UP = 1;
        private static final int GENERIC_INPUT_TYPE_ID_TOUCH_MOVE = 2;
        private static final int GENERIC_INPUT_TYPE_ID_KEY_DOWN = 3;
        private static final int GENERIC_INPUT_TYPE_ID_KEY_UP = 4;
        private static final int GENERIC_INPUT_TYPE_ID_ZOOM = 5;
        private static final int LONG_PRESS_DELAY = 1000;
        private boolean mHasPerformedLongPress = false;
        private CountDown mCountDown;
        private int mTouchSlop;
        private float mInitX;
        private float mInitY;
        private boolean mCatchEvents = true;
        private boolean mFullScreenFlag = false;
        private boolean mHasFocus = false;
        private Runnable mFocusGetCallback;
        public WfdSinkLayout(Context context) {
            super(context);
            mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        }
        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (!mCatchEvents) {
                return false;
            }
            final int action = ev.getAction();
            Log.d("@M_" + TAG, "onTouchEvent action=" + action);
            switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
                    StringBuilder eventDesc = new StringBuilder();
                    eventDesc.append(
                            String.valueOf(GENERIC_INPUT_TYPE_ID_TOUCH_DOWN))
                            .append(",");
                    eventDesc.append(getTouchEventDesc(ev));
                    sendUibcInputEvent(eventDesc.toString());
                }
                mInitX = ev.getX();
                mInitY = ev.getY();
                mHasPerformedLongPress = false;
                checkForLongClick(0);
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
                    StringBuilder eventDesc = new StringBuilder();
                    eventDesc.append(
                            String.valueOf(GENERIC_INPUT_TYPE_ID_TOUCH_UP))
                            .append(",");
                    eventDesc.append(getTouchEventDesc(ev));
                    sendUibcInputEvent(eventDesc.toString());
                }
                removePendingCallback();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
                    StringBuilder eventDesc = new StringBuilder();
                    eventDesc.append(
                            String.valueOf(GENERIC_INPUT_TYPE_ID_TOUCH_MOVE))
                            .append(",");
                    eventDesc.append(getTouchEventDesc(ev));
                    sendUibcInputEvent(eventDesc.toString());
                }
                if (Math.hypot(ev.getX() - mInitX, ev.getY() - mInitY) > mTouchSlop) {
                    removePendingCallback();
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                removePendingCallback();
                break;
            }
            default:
                break;
            }
            return true;
        }
        @Override
        public boolean onGenericMotionEvent(MotionEvent event) {
            if (!mCatchEvents) {
                return false;
            }
            Log.d("@M_" + TAG, "onGenericMotionEvent event.getSource()="
                    + event.getSource());
            if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
                if (event.getSource() == InputDevice.SOURCE_MOUSE) {
                    switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_MOVE:
                        StringBuilder eventDesc = new StringBuilder();
                        eventDesc
                                .append(
                                        String
                                                .valueOf(GENERIC_INPUT_TYPE_ID_TOUCH_MOVE))
                                .append(",");
                        eventDesc.append(getTouchEventDesc(event));
                        sendUibcInputEvent(eventDesc.toString());
                        return true;
                    case MotionEvent.ACTION_SCROLL:
                        // process the scroll wheel movement...
                        return true;
                    default:
                        break;
                    }
                }
            }
            return true;
        }
        @Override
        public boolean onKeyPreIme(int keyCode, KeyEvent event) {
            if (!mCatchEvents || !mFullScreenFlag) {
                return false;
            }
            Log.d("@M_" + TAG, "onKeyPreIme keyCode=" + keyCode + ", action=" + event.getAction());
            if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
                int asciiCode = event.getUnicodeChar();
                if (asciiCode == 0 || asciiCode < 0x20) {
                    Log.d("@M_" + TAG, "Can't find unicode for keyCode=" + keyCode);
                    asciiCode = KeyCodeConverter.keyCodeToAscii(keyCode);
                }
                boolean onKeyUp = event.getAction() == KeyEvent.ACTION_UP;
                if (mLatinCharTest && keyCode == KeyEvent.KEYCODE_F1) {
                    Log.d("@M_" + TAG, "Latin Test Mode enabled");
                    asciiCode = mTestLatinChar;
                    if (onKeyUp) {
                        if (mTestLatinChar == 0xFF) {
                            mTestLatinChar = 0xA0;
                        } else {
                            mTestLatinChar++;
                        }
                    }
                }
                Log.d("@M_" + TAG, "onKeyPreIme asciiCode=" + asciiCode);
                if (asciiCode == 0x00) {
                    Log.d("@M_" + TAG, "Can't find control for keyCode=" + keyCode);
                } else {
                    StringBuilder eventDesc = new StringBuilder();
                    eventDesc.append(
                            String.valueOf(onKeyUp ? GENERIC_INPUT_TYPE_ID_KEY_UP :
                                       GENERIC_INPUT_TYPE_ID_KEY_DOWN))
                            .append(",").append(
                                    String.format("0x%04x", asciiCode)).append(
                                    ", 0x0000");
                    sendUibcInputEvent(eventDesc.toString());
                    return true;
                }
            }
            return false;
        }
        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            super.onWindowFocusChanged(hasWindowFocus);
            Log.d("@M_" + TAG, "onWindowFocusChanged: " + hasWindowFocus);
            mHasFocus = hasWindowFocus;
            if (hasWindowFocus && mFocusGetCallback != null) {
                mFocusGetCallback.run();
            }
        }
        private String getTouchEventDesc(MotionEvent ev) {
            final int pointerCount = ev.getPointerCount();
            String ret;
            StringBuilder eventDesc = new StringBuilder();
            eventDesc.append(String.valueOf(pointerCount)).append(",");
            for (int p = 0; p < pointerCount; p++) {
                eventDesc.append(String.valueOf(ev.getPointerId(p)))
                .append(",")
                .append(String.valueOf((int) (ev.getXPrecision() * ev.getX(p))))
                .append(",")
                .append(String.valueOf((int) (ev.getYPrecision() * ev.getY(p))))
                .append(",");
            }
            ret = eventDesc.toString();
            return ret.substring(0, ret.length() - 1);
        }
        private void sendUibcInputEvent(String eventDesc) {
            Log.d("@M_" + TAG, "sendUibcInputEvent: " + eventDesc);
            mExt.sendUibcEvent(eventDesc);
        }
        private void checkForLongClick(int delayOffset) {
            mHasPerformedLongPress = false;
            if (mCountDown == null) {
                mCountDown = new CountDown();
            }
            mCountDown.rememberWindowAttachCount();
            postDelayed(mCountDown, LONG_PRESS_DELAY
                    + ViewConfiguration.getLongPressTimeout() - delayOffset);
        }
        private void removePendingCallback() {
            Log.v("@M_" + TAG, "removePendingCallback");
            if (mCountDown != null && !mHasPerformedLongPress) {
                removeCallbacks(mCountDown);
                removeCountDown();
            }
        }
        private void setCatchEvents(boolean catched) {
            mCatchEvents = catched;
        }
        private void setFullScreenFlag(boolean fullScreen) {
            mFullScreenFlag = fullScreen;
        }
        private void setOnFocusGetCallback(Runnable runnable) {
            mFocusGetCallback = runnable;
        }
        @Override
        protected void onDetachedFromWindow() {
            removePendingCallback();
            super.onDetachedFromWindow();
        }
        /**
         *  Runnable to update long press count down UI.
         */
        class CountDown implements Runnable {
            private int mCountDownNum;
            private int mOriginalWindowAttachCount;
            @Override
            public void run() {
                if (!mCountdownShowing) {
                    mCountDownNum = 3;
                    addCountdownView(mCountDownNum + "");
                } else {
                    mCountDownNum--;
                    if (mCountDownNum <= 0) {
//                        if ((mParent != null)
//                                && mOriginalWindowAttachCount == getWindowAttachCount()) {
//                            // Don't use LongClick because KEYCODE_DPAD_CENTER, KEYCODE_ENTER
//                            // will trigger onLongClock as well
//                            if (onLongClick(mSinkViewLayout)) {
//                                mHasPerformedLongPress = true;
//                            }
//                        }
                        return;
                    } else {
                        if (mCountdownShowing) {
                            ViewGroup countdownView = (ViewGroup) mSinkViewLayout
                                    .getTag(R.id.wfd_sink_countdown_num);
                            if (countdownView != null) {
                                TextView tv = (TextView) countdownView
                                        .findViewById(R.id.wfd_sink_countdown_num);
                                if (tv != null) {
                                    tv.setText(mCountDownNum + "");
                                    tv.postInvalidate();
                                }
                            }
                        }
                    }
                }
                postDelayed(this, 1000);
            }
            public void rememberWindowAttachCount() {
                mOriginalWindowAttachCount = getWindowAttachCount();
            }
        }
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
    @Override
    public boolean onLongClick(View v) {
        Log.d("@M_" + TAG, "onLongClick");
        dismissAllowingStateLoss();
        mActivity.finish();
        return true;
    }

}
