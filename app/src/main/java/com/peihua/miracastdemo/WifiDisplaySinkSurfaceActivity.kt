package com.peihua.miracastdemo

import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.peihua.miracastdemo.utils.ActivityEmbeddingUtils
import com.peihua.miracastdemo.utils.Logcat

@Suppress("DEPRECATION")
class WifiDisplaySinkSurfaceActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private val mSurfaceView: SurfaceView by lazy {
        SurfaceView(this)
    }
    private val contentView by lazy { WfdSinkLayout(this) }
    private val callback: WifiSinkApiModel.() -> Unit = {
        onRefreshed {
            Logcat.d("onRefreshed: $it")
            mSurfaceView.refresh(it.width, it.height)
        }
        onConnection {
            Logcat.d("onConnection: $it")
        }
        onDisconnection {
            Logcat.d("onDisconnection: $it")
            finish()
        }
        onChangeStatus {
            Logcat.d("onChangeStatus: $it")
        }
        onChangeUiPortrait {
            Logcat.d("onChangeUiPortrait: $it")
        }
        onRequestFullScreen {
            val systemUiVis = window.decorView.systemUiVisibility;
            if ((systemUiVis and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                requestFullScreen(systemUiVis)
            }
        }
    }
    private var mSurfaceShowing = false;
    private fun requestFullScreen(systemUi: Int) {
        var systemUi = systemUi;
        systemUi = systemUi or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        systemUi = systemUi or View.SYSTEM_UI_FLAG_FULLSCREEN;
        systemUi = systemUi or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        //| View.SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED;
        val newUiOptions = systemUi;
        Logcat.d(
            "WifiDisplaySinkSurface",
            "request full screen: " + Integer.toHexString(newUiOptions)
        );
        runOnUiThread {
            Logcat.d(
                "WifiDisplaySinkSurface",
                "request full screen: " + Integer.toHexString(newUiOptions)
            );
            window.decorView.systemUiVisibility = newUiOptions;
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView.setBackgroundColor(Color.BLACK)
        contentView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        setContentView(contentView)
        requestFullScreen(window.decorView.systemUiVisibility)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );
        addSurfaceView()
        mSurfaceView.holder.addCallback(this)
        WifiSinkDisplayManager.getInstance().register(callback)
    }

    private fun addSurfaceView() {
        if (mSurfaceView.parent != null) {
            (mSurfaceView.parent as ViewGroup).removeView(mSurfaceView)
        }

        contentView.addView(mSurfaceView, FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }


    override fun onStop() {
        super.onStop()
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );
        mSurfaceView.holder.removeCallback(this)
        WifiSinkDisplayManager.getInstance().unRegister(callback)
        disconnect()
        restoreOrientation()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        disconnect()
    }

    /**
     * Restore orientation when exit WFD sink full screen view.
     */
    public fun restoreOrientation() {
        /*if (mOrientationBak != ORIENTATION_NOT_BACKUP) {
            mActivity.setRequestedOrientation(mOrientationBak);
        }*/
        ActivityEmbeddingUtils.setIsSplitEnabled(true);
        Logcat.d("restoreOrientation ---->ActivityEmbeddingUtils.getOrientationSettings()--->" + ActivityEmbeddingUtils.getOrientationSettings());
        Settings.System.putInt(
            contentResolver,
            Settings.System.USER_ROTATION,
            ActivityEmbeddingUtils.getOrientationSettings()
        );
    }

    fun SurfaceView.refresh(width: Int, height: Int) {
        val size = Point()
        windowManager.defaultDisplay.getRealSize(size)
        val wm_width = size.x
        val wm_height = size.y
        val resize_width = (wm_height * width / height)
        Logcat.d(
            "refresh width  : " + width + "  height : " + height
                    + " wm width: " + wm_width + " wm height:" + wm_height + "resize_width" + resize_width
        )
        val sinkViewFL = FrameLayout.LayoutParams(resize_width, wm_height, Gravity.CENTER)
        layoutParams = sinkViewFL
    }

    private fun disconnect() {
        if (mSurfaceShowing) {
            WifiSinkDisplayManager.getInstance().disconnectWfdSinkConnection();
        }
        mSurfaceShowing = false;
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Logcat.d("@M_${TAG}", "surfaceCreated")
        if (holder != null/* && mSinkExt.isSinkMode*/) {
            if (!mSurfaceShowing) {
                WifiSinkDisplayManager.getInstance().waitWfdSinkConnection(holder.surface)
            }
            mSurfaceShowing = true;
        }
    }

    override fun surfaceChanged(
        holder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int,
    ) {
        Logcat.d("@M_${TAG}", "surfaceChanged")
        val systemUiVis = window.decorView.systemUiVisibility;
        if ((systemUiVis and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
            requestFullScreen(systemUiVis);
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Logcat.d("@M_${TAG}", "surfaceDestroyed")
        disconnect()
    }

    companion object {
        private const val TAG = "WifiDisplaySinkSurface"
    }
}