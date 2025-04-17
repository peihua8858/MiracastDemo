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
    //    private val mSinkExt: WifiSinkExt by lazy {
//        WifiSinkExt(this) {
//            onRefreshed {
//                Logcat.d("onRefreshed: $it")
//                mSurfaceView.refresh(it.width, it.height)
//            }
//            onConnection {
//                Logcat.d("onConnection: $it")
//            }
//            onDisconnection {
//                Logcat.d("onDisconnection: $it")
//                finish()
//            }
//            onChangeStatus {
//                Logcat.d("onChangeStatus: $it")
//            }
//            onChangeUiPortrait {
//                Logcat.d("onChangeUiPortrait: $it")
//            }
//            onRequestFullScreen {
//                val systemUiVis = window.decorView.systemUiVisibility;
//                if ((systemUiVis and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
//                    requestFullScreen(systemUiVis)
//                }
//            }
//        }
//    }
    private val mSurfaceView: SurfaceView by lazy {
        SurfaceView(this)
//        mSinkExt.mSurfaceView
    }
    val contentView by lazy { FrameLayout(this) }
    val callback: ReceiverApiModel.() -> Unit = {
        onRefreshed {
            Logcat.d("onRefreshed: $it")
            mSurfaceView.refresh(it.width, it.height)
        }
        onConnection {
            Logcat.d("onConnection: $it")
            setupWfdSinkConnection()
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

    fun setupWfdSinkConnection() {
//        addSurfaceView()
//        mSinkExt.waitWfdSinkConnection(mSurfaceView.holder.surface)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = intent.extras ?: Bundle()
        val width = bundle.getInt("width", 1920)
        val height = bundle.getInt("height", 1080)
        Logcat.d("onCreate: [$width, $height]")
//        set1Px()
        setMathParent()
//        mSurfaceView.refresh(width, height)
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
        WifiSinkDisplayManager.getInstance().onRegister(this)
//        mSinkExt.onStart(this)
//
//        mSinkExt.handleWfdStatusChanged(mSinkExt.wifiDisplayStatus, this)
//        mSinkExt.onRegister(this)
    }

    private fun set1Px() {
        val window = window
        window.setGravity(Gravity.START or Gravity.TOP)
        val params = window.attributes
        params.x = 0
        params.y = 0
        params.height = 1
        params.width = 1
        window.attributes = params
    }

    private fun setMathParent() {
        val window = window
        val params = window.attributes
        params.height = LayoutParams.MATCH_PARENT
        params.width = LayoutParams.MATCH_PARENT
        window.attributes = params
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
//        mSinkExt.onStop(this)
        mSurfaceView.holder.removeCallback(this)
        WifiSinkDisplayManager.getInstance().unRegister(callback)
        WifiSinkDisplayManager.getInstance().onUnRegister(this)
//        WifiSinkDisplayManager.getInstance().onStop(this)
        restoreOrientation();
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
        if (width > 0 && height > 0) {
            setMathParent()
        }
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

    override fun surfaceCreated(holder: SurfaceHolder?) {
        Logcat.d("@M_${TAG}", "surfaceCreated")
        if (holder != null/* && mSinkExt.isSinkMode*/) {
            if (!mSurfaceShowing) {
                WifiSinkDisplayManager.getInstance().waitWfdSinkConnection(holder.surface)
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
        val systemUiVis = window.decorView.systemUiVisibility;
        if ((systemUiVis and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
            requestFullScreen(systemUiVis);
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Logcat.d("@M_${TAG}", "surfaceDestroyed")
        disconnect()
    }

    companion object {
        private const val TAG = "WifiDisplaySinkSurface"
    }
}