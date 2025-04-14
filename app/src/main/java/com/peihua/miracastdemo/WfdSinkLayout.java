package com.peihua.miracastdemo;

import android.content.Context;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class WfdSinkLayout extends FrameLayout {
    private static final String TAG = "WfdSinkLayout";
    private static final int GENERIC_INPUT_TYPE_ID_TOUCH_DOWN = 0;
    private static final int GENERIC_INPUT_TYPE_ID_TOUCH_UP = 1;
    private static final int GENERIC_INPUT_TYPE_ID_TOUCH_MOVE = 2;
    private static final int GENERIC_INPUT_TYPE_ID_KEY_DOWN = 3;
    private static final int GENERIC_INPUT_TYPE_ID_KEY_UP = 4;
    private static final int GENERIC_INPUT_TYPE_ID_ZOOM = 5;
    private static final int LONG_PRESS_DELAY = 1000;
    private boolean mHasPerformedLongPress = false;
//    private CountDown mCountDown;
    private int mTouchSlop;
    private float mInitX;
    private float mInitY;
    private boolean mCatchEvents = true;
    private boolean mFullScreenFlag = false;
     boolean mHasFocus = false;
    private Runnable mFocusGetCallback;
    private boolean mCountdownShowing = false;
    // MTK WFD sink UIBC support
    private boolean mLatinCharTest = false;
    private int mTestLatinChar = 0xA0;
    private WfdSinkExt mExt;
    public WfdSinkLayout(Context context) {
        super(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }
    public void setWfdSinkExt(WfdSinkExt mExt) {
        this.mExt = mExt;
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
//        if (mCountDown == null) {
//            mCountDown = new WfdSinkLayout.CountDown();
//        }
//        mCountDown.rememberWindowAttachCount();
//        postDelayed(mCountDown, LONG_PRESS_DELAY
//                + ViewConfiguration.getLongPressTimeout() - delayOffset);
    }
    private void removePendingCallback() {
        Log.v("@M_" + TAG, "removePendingCallback");
//        if (mCountDown != null && !mHasPerformedLongPress) {
//            removeCallbacks(mCountDown);
//            removeCountDown();
//        }
    }
     void setCatchEvents(boolean catched) {
        mCatchEvents = catched;
    }
     void setFullScreenFlag(boolean fullScreen) {
        mFullScreenFlag = fullScreen;
    }
     void setOnFocusGetCallback(Runnable runnable) {
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
//    class CountDown implements Runnable {
//        private int mCountDownNum;
//        private int mOriginalWindowAttachCount;
//        @Override
//        public void run() {
//            if (!mCountdownShowing) {
//                mCountDownNum = 3;
//                addCountdownView(mCountDownNum + "");
//            } else {
//                mCountDownNum--;
//                if (mCountDownNum <= 0) {
//                    if ((mParent != null)
//                            && mOriginalWindowAttachCount == getWindowAttachCount()) {
//                        // Don't use LongClick because KEYCODE_DPAD_CENTER, KEYCODE_ENTER
//                        // will trigger onLongClock as well
//                        if (onLongClick(mSinkViewLayout)) {
//                            mHasPerformedLongPress = true;
//                        }
//                    }
//                    return;
//                } else {
//                    if (mCountdownShowing) {
//                        ViewGroup countdownView = (ViewGroup) mSinkViewLayout
//                                .getTag(R.id.wfd_sink_countdown_num);
//                        if (countdownView != null) {
//                            TextView tv = (TextView) countdownView
//                                    .findViewById(R.id.wfd_sink_countdown_num);
//                            if (tv != null) {
//                                tv.setText(mCountDownNum + "");
//                                tv.postInvalidate();
//                            }
//                        }
//                    }
//                }
//            }
//            postDelayed(this, 1000);
//        }
//        public void rememberWindowAttachCount() {
//            mOriginalWindowAttachCount = getWindowAttachCount();
//        }
//    }
    private void addCountdownView(String countdownNum) {
        if (mCountdownShowing) {
            return;
        }
        ViewGroup countdownView = (ViewGroup) LayoutInflater
                .from(getContext()).inflate(R.layout.wfd_sink_countdown, null);
        TextView tv = (TextView) countdownView
                .findViewById(R.id.wfd_sink_countdown_num);
        tv.setText(countdownNum);
        addView(countdownView);
        setTag(R.id.wfd_sink_countdown_num, countdownView);
        mCountdownShowing = true;
    }
    private void removeCountDown() {
        if (mCountdownShowing) {
            View countdownView = (View) getTag(R.id.wfd_sink_countdown_num);
            if (countdownView != null) {
                removeView(countdownView);
                setTag(R.id.wfd_sink_countdown_num, null);
            }
        }
        mCountdownShowing = false;
    }
}