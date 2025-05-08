package com.peihua.miracastdemo

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import com.peihua.miracastdemo.utils.dLog
import kotlin.math.hypot

class WfdSinkLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    private var mHasPerformedLongPress = false
    private var mTouchSlop: Int = 0
    private var mInitX = 0f
    private var mInitY = 0f
    private var mCatchEvents = true
    private var mFullScreenFlag = false
    private var mHasFocus = false
    private var mLatinCharTest = false
    private var mTestLatinChar = 0xA0
    private var mFocusGetCallback: Runnable? = null

    init {
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!mCatchEvents) {
            return false
        }
        val action = ev.action
        dLog { "onTouchEvent action=$action" }
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
                    val eventDesc = StringBuilder()
                    eventDesc.append(
                        GENERIC_INPUT_TYPE_ID_TOUCH_DOWN.toString()
                    ).append(",")
                    eventDesc.append(getTouchEventDesc(ev))
                    sendUibcInputEvent(eventDesc.toString())
                }
                mInitX = ev.x
                mInitY = ev.y
                mHasPerformedLongPress = false
//                checkForLongClick(0)
            }

            MotionEvent.ACTION_UP -> {
                if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
                    val eventDesc = StringBuilder()
                    eventDesc.append(
                        GENERIC_INPUT_TYPE_ID_TOUCH_UP.toString()
                    ).append(",")
                    eventDesc.append(getTouchEventDesc(ev))
                    sendUibcInputEvent(eventDesc.toString())
                }
//                removePendingCallback()
            }

            MotionEvent.ACTION_MOVE -> {
                if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
                    val eventDesc = StringBuilder()
                    eventDesc.append(
                        GENERIC_INPUT_TYPE_ID_TOUCH_MOVE.toString()
                    ).append(",")
                    eventDesc.append(getTouchEventDesc(ev))
                    sendUibcInputEvent(eventDesc.toString())
                }
                if (hypot(
                        (ev.x - mInitX).toDouble(),
                        (ev.y - mInitY).toDouble()
                    ) > mTouchSlop
                ) {
//                    removePendingCallback()
                }
            }

            MotionEvent.ACTION_CANCEL -> {
//                removePendingCallback()
            }

            else -> {}
        }
        return true
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (!mCatchEvents) {
            return false
        }
        dLog { "onGenericMotionEvent event.getSource()=" + event.source }
        if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
            if (event.source == InputDevice.SOURCE_MOUSE) {
                when (event.action) {
                    MotionEvent.ACTION_HOVER_MOVE -> {
                        val eventDesc = StringBuilder()
                        eventDesc
                            .append(
                                GENERIC_INPUT_TYPE_ID_TOUCH_MOVE.toString()
                            ).append(",")
                        eventDesc.append(getTouchEventDesc(event))
                        sendUibcInputEvent(eventDesc.toString())
                        return true
                    }

                    MotionEvent.ACTION_SCROLL ->                         // process the scroll wheel movement...
                        return true

                    else -> {}
                }
            }
        }
        return true
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (!mCatchEvents || !mFullScreenFlag) {
            return false
        }
        dLog { "onKeyPreIme keyCode=$keyCode, action=${event.action}" }
        if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
            var asciiCode = event.unicodeChar
            if (asciiCode == 0 || asciiCode < 0x20) {
                dLog { "Can't find unicode for keyCode=$keyCode" }
                asciiCode = KeyCodeConverter.keyCodeToAscii(keyCode)
            }
            val onKeyUp = event.action == KeyEvent.ACTION_UP
            if (mLatinCharTest && keyCode == KeyEvent.KEYCODE_F1) {
                dLog { "Latin Test Mode enabled" }
                asciiCode = mTestLatinChar
                if (onKeyUp) {
                    if (mTestLatinChar == 0xFF) {
                        mTestLatinChar = 0xA0
                    } else {
                        mTestLatinChar++
                    }
                }
            }
            dLog { "onKeyPreIme asciiCode=$asciiCode" }
            if (asciiCode == 0x00) {
                dLog { "Can't find control for keyCode=$keyCode" }
            } else {
                val eventDesc = java.lang.StringBuilder()
                eventDesc.append(
                    (if (onKeyUp) GENERIC_INPUT_TYPE_ID_KEY_UP
                    else GENERIC_INPUT_TYPE_ID_KEY_DOWN).toString()
                ).append(",").append(String.format("0x%04x", asciiCode)).append(", 0x0000")
                sendUibcInputEvent(eventDesc.toString())
                return true
            }
        }
        return false
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        dLog { "onWindowFocusChanged: $hasWindowFocus" }
        mHasFocus = hasWindowFocus
        if (hasWindowFocus) {
            mFocusGetCallback?.run()
        }
    }

    private fun getTouchEventDesc(ev: MotionEvent): String {
        val pointerCount = ev.pointerCount
        val ret: String?
        val eventDesc = java.lang.StringBuilder()
        eventDesc.append(pointerCount.toString()).append(",")
        for (p in 0 until pointerCount) {
            eventDesc.append(ev.getPointerId(p).toString())
                .append(",")
                .append((ev.xPrecision * ev.getX(p)).toInt().toString())
                .append(",")
                .append((ev.yPrecision * ev.getY(p)).toInt().toString())
                .append(",")
        }
        ret = eventDesc.toString()
        return ret.substring(0, ret.length - 1)
    }

    private fun sendUibcInputEvent(eventDesc: String?) {
        dLog { "sendUibcInputEvent: $eventDesc" }
        WifiSinkDisplayManager.getInstance().sendUibcEvent(eventDesc)
    }

    companion object {

        const val GENERIC_INPUT_TYPE_ID_TOUCH_DOWN: Int = 0

        const val GENERIC_INPUT_TYPE_ID_TOUCH_UP: Int = 1

        const val GENERIC_INPUT_TYPE_ID_TOUCH_MOVE: Int = 2

        const val GENERIC_INPUT_TYPE_ID_KEY_DOWN: Int = 3

        const val GENERIC_INPUT_TYPE_ID_KEY_UP: Int = 4

        const val GENERIC_INPUT_TYPE_ID_ZOOM: Int = 5

        const val LONG_PRESS_DELAY: Int = 1000
    }
}