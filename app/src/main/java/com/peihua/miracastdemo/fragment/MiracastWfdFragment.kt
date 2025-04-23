package com.peihua.miracastdemo.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.hardware.display.DisplayManager
import android.hardware.display.WifiDisplay
import android.hardware.display.WifiDisplayStatus
import android.media.MediaRouter
import android.net.Uri
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.provider.Settings
import android.text.TextUtils
import android.util.Slog
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreference
import com.android.internal.app.MediaRouteDialogPresenter
import com.mediatek.provider.MtkSettingsExt
import com.peihua.miracastdemo.FeatureOption
import com.peihua.miracastdemo.R
import com.peihua.miracastdemo.utils.dLog

class MiracastWfdFragment : PreferenceFragmentCompat() {
    private val mHandler = Handler(Looper.getMainLooper())
    private val mRouter: MediaRouter by lazy {
        val router = requireContext().getSystemService(Context.MEDIA_ROUTER_SERVICE) as MediaRouter
        router.setRouterGroupId(MediaRouter.MIRRORING_GROUP_ID)
        router
    }
    private val mDisplayManager: DisplayManager by lazy {
        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }
    private val mWifiP2pManager: WifiP2pManager by lazy { requireContext().getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager }
    private var mStarted = false
    private var mPendingChanges = 0

    private var mWifiDisplayOnSetting = false
    private var mWifiDisplayStatus: WifiDisplayStatus? = null

    /** M: Add for wfd change resolution */
    private var mWfdChangeResolution: WfdChangeResolution? = null
    private val mEmptyView: TextView? = null

    /* certification */
    private var mWifiDisplayCertificationOn = true

    private var mWifiP2pChannel: WifiP2pManager.Channel? = null
    private var mCertCategory: PreferenceGroup? = null
    private var mListen = false
    private var mAutoGO = false
    private var mWpsConfig = WpsInfo.INVALID
    private var mListenChannel = 0
    private var mOperatingChannel = 0
    private val contentResolver
        get() = requireContext().contentResolver

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        /** M: new mWfdChangeResolution for MTK WFD feature @{ */
        if (FeatureOption.MTK_WFD_SUPPORT) {
            mWfdChangeResolution = WfdChangeResolution(requireContext())
        }

        /** M: @} */
        val context: Context = requireContext()
        mWifiP2pChannel = mWifiP2pManager.initialize(context, Looper.getMainLooper(), null)

        setHasOptionsMenu(true)
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        addPreferencesFromResource(R.xml.wifi_display_settings)
    }

    override fun onStart() {
        super.onStart()
        mStarted = true

        val context: Context = requireContext()
        val filter = IntentFilter()
        filter.addAction(DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED)
        context.registerReceiver(mReceiver, filter)

        context.contentResolver.registerContentObserver(
            Settings.Global.getUriFor(
                Settings.Global.WIFI_DISPLAY_ON
            ), false, mSettingsObserver
        )
        context.contentResolver.registerContentObserver(
            Settings.Global.getUriFor(
                Settings.Global.WIFI_DISPLAY_CERTIFICATION_ON
            ), false, mSettingsObserver
        )
        context.contentResolver.registerContentObserver(
            Settings.Global.getUriFor(
                Settings.Global.WIFI_DISPLAY_WPS_CONFIG
            ), false, mSettingsObserver
        )

        mRouter.addCallback(
            MediaRouter.ROUTE_TYPE_REMOTE_DISPLAY, mRouterCallback,
            MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN
        )

        update(CHANGE_ALL)
        /** M: WFD sink support @{ */
        if (mWfdChangeResolution != null) {
//            mWfdChangeResolution!!.onStart()
        }
        /** @}
         */
    }

    override fun onStop() {
        super.onStop()
        mStarted = false
        /** M: WFD sink support @{ */
        if (mWfdChangeResolution != null) {
//            mWfdChangeResolution!!.onStop()
        }
        val context: Context = requireContext()
        context.unregisterReceiver(mReceiver)

        context.contentResolver.unregisterContentObserver(mSettingsObserver)

        mRouter.removeCallback(mRouterCallback)

        unscheduleUpdate()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (mWifiDisplayStatus != null && (mWifiDisplayStatus!!.featureState
                    != WifiDisplayStatus.FEATURE_STATE_UNAVAILABLE)
        ) {
            val item = menu.add(
                Menu.NONE,
                MENU_ID_ENABLE_WIFI_DISPLAY, 0,
                R.string.wifi_display_enable_menu_item
            )
            item.isCheckable = true
            item.isChecked = mWifiDisplayOnSetting
        }

        /** M: Call WfdChangeResolution create option menu function @{ */
        if (mWfdChangeResolution != null) {
            mWfdChangeResolution!!.onCreateOptionMenu(menu, mWifiDisplayStatus)
        }
        /** M: @} */
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            MENU_ID_ENABLE_WIFI_DISPLAY -> {
                mWifiDisplayOnSetting = !item.isChecked
                item.isChecked = mWifiDisplayOnSetting
                Settings.Global.putInt(
                    contentResolver,
                    Settings.Global.WIFI_DISPLAY_ON, if (mWifiDisplayOnSetting) 1 else 0
                )
                return true
            }
        }
        /** M: Call WfdChangeResolution option menu selected function @{ */
        if (mWfdChangeResolution != null &&
            mWfdChangeResolution!!.onOptionMenuSelected(item, parentFragmentManager)
        ) {
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
        /** M: @} */
    }

    private fun scheduleUpdate(changes: Int) {
        if (mStarted) {
            if (mPendingChanges == 0) {
                mHandler.post(mUpdateRunnable)
            }
            mPendingChanges = mPendingChanges or changes
        }
    }

    private fun unscheduleUpdate() {
        if (mPendingChanges != 0) {
            mPendingChanges = 0
            mHandler.removeCallbacks(mUpdateRunnable)
        }
    }

    private fun update(changes: Int) {
        var invalidateOptions = false

        // Update settings.
        if ((changes and CHANGE_SETTINGS) != 0) {
            mWifiDisplayOnSetting = Settings.Global.getInt(
                contentResolver,
                Settings.Global.WIFI_DISPLAY_ON, 0
            ) != 0
//            mWifiDisplayCertificationOn = Settings.Global.getInt(
//                contentResolver,
//                Settings.Global.WIFI_DISPLAY_CERTIFICATION_ON, 0
//            ) != 0
            mWpsConfig = Settings.Global.getInt(
                contentResolver,
                Settings.Global.WIFI_DISPLAY_WPS_CONFIG, WpsInfo.INVALID
            )

            // The wifi display enabled setting may have changed.
            invalidateOptions = true
        }

        // Update wifi display state.
        if ((changes and CHANGE_WIFI_DISPLAY_STATUS) != 0) {
            mWifiDisplayStatus = mDisplayManager.wifiDisplayStatus

            // The wifi display feature state may have changed.
            invalidateOptions = true
        }

        // Rebuild the routes.
        val preferenceScreen = getPreferenceScreen()
        preferenceScreen.removeAll()

        /** M: WFD sink support @{ */
        var category = PreferenceCategory(requireContext())
        category.title = "可投屏的设备"
        category.order = 2
        preferenceScreen.addPreference(category)

        /** @}
         */

        // Add all known remote display routes.
        val routeCount = mRouter.routeCount
        for (i in 0 until routeCount) {
            val route = mRouter.getRouteAt(i)
            if (route.matchesTypes(MediaRouter.ROUTE_TYPE_REMOTE_DISPLAY)) {
                /** M: WFD sink support @{ */
                category.addPreference(createRoutePreference(route))
            }
        }

        // Additional features for wifi display routes.
        if (mWifiDisplayStatus?.featureState == WifiDisplayStatus.FEATURE_STATE_ON) {
            // Add all unpaired wifi displays.
            for (display in mWifiDisplayStatus!!.displays) {
                if (!display.isRemembered && display.isAvailable
                    && !display.equals(mWifiDisplayStatus!!.activeDisplay)
                ) {
                    category.addPreference(
                        UnpairedWifiDisplayPreference(
                            requireContext(), display, ::pairWifiDisplay
                        )
                    )
                }
            }

            // Add the certification menu if enabled in developer options.
            if (mWifiDisplayCertificationOn) {
                buildCertificationMenu(preferenceScreen)
            }
        }

        // Invalidate menu options if needed.
        if (invalidateOptions) {
            requireActivity().invalidateOptionsMenu()
        }
    }

    private fun createRoutePreference(route: MediaRouter.RouteInfo): RoutePreference {
        val display = findWifiDisplay(route.deviceAddress)
        return if (display != null) {
            WifiDisplayRoutePreference(
                requireContext(),
                route,
                display,
                ::showWifiDisplayOptionsDialog
            )
        } else {
            RoutePreference(requireContext(), route, ::toggleRoute)
        }
    }

    private fun findWifiDisplay(deviceAddress: String?): WifiDisplay? {
        if (mWifiDisplayStatus != null && deviceAddress != null) {
            for (display in mWifiDisplayStatus!!.displays) {
                if (display.deviceAddress == deviceAddress) {
                    return display
                }
            }
        }
        return null
    }

    private fun pairWifiDisplay(display: WifiDisplay) {
        if (display.canConnect()) {
            /** M: WFD sink support @{ */
            mWfdChangeResolution?.prepareWfdConnect()
            /** @}
             */
            mDisplayManager.connectWifiDisplay(display.deviceAddress)
        }
    }

    private fun showWifiDisplayOptionsDialog(display: WifiDisplay) {
        val view =
            requireActivity().layoutInflater.inflate(R.layout.wifi_display_options, null)
        val nameEditText = view.findViewById<View?>(R.id.name) as EditText
        nameEditText.setText(display.friendlyDisplayName)

        val done: DialogInterface.OnClickListener = object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                var name: String? = nameEditText.text.toString().trim { it <= ' ' }
                if (name!!.isEmpty() || name == display.deviceName) {
                    name = null
                }
                mDisplayManager.renameWifiDisplay(display.deviceAddress, name)
            }
        }
        val forget: DialogInterface.OnClickListener = object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                mDisplayManager.forgetWifiDisplay(display.deviceAddress)
            }
        }

        val dialog = AlertDialog.Builder(requireActivity())
            .setCancelable(true)
            .setTitle(R.string.wifi_display_options_title)
            .setView(view)
            .setPositiveButton(R.string.wifi_display_options_done, done)
            .setNegativeButton(R.string.wifi_display_options_forget, forget)
            .create()
        dialog.show()
    }

    private fun toggleRoute(route: MediaRouter.RouteInfo) {
        if (route.isSelected) {
            MediaRouteDialogPresenter.showDialogFragment(
                requireActivity(),
                MediaRouter.ROUTE_TYPE_REMOTE_DISPLAY, null
            )
        } else {
            /** M: WFD sink support @{ */
            if (mWfdChangeResolution != null) {
                mWfdChangeResolution!!.prepareWfdConnect()
            }
            /** @}
             */
            route.select()
        }
    }

    private fun buildCertificationMenu(preferenceScreen: PreferenceScreen) {
        if (mCertCategory == null) {
            mCertCategory = PreferenceCategory(requireContext())
            mCertCategory!!.setTitle(R.string.wifi_display_certification_heading)
            mCertCategory!!.order =
                ORDER_CERTIFICATION
        } else {
            mCertCategory!!.removeAll()
        }
        preferenceScreen.addPreference(mCertCategory!!)

        // display session info if there is an active p2p session
        if (!mWifiDisplayStatus!!.sessionInfo.groupId.isEmpty()) {
            val p = Preference(requireContext())
            p.setTitle(R.string.wifi_display_session_info)
            p.setSummary(mWifiDisplayStatus!!.sessionInfo.toString())
            mCertCategory!!.addPreference(p)

            // show buttons for Pause/Resume when a WFD session is established
            if (mWifiDisplayStatus!!.sessionInfo.sessionId != 0) {
                mCertCategory!!.addPreference(object : Preference(requireContext()) {
                    override fun onBindViewHolder(view: PreferenceViewHolder) {
                        super.onBindViewHolder(view)

                        var b = view.findViewById(R.id.left_button) as Button
                        b.setText(R.string.wifi_display_pause)
                        b.setOnClickListener(object : View.OnClickListener {
                            override fun onClick(v: View?) {
                                mDisplayManager.pauseWifiDisplay()
                            }
                        })

                        b = view.findViewById(R.id.right_button) as Button
                        b.setText(R.string.wifi_display_resume)
                        b.setOnClickListener(object : View.OnClickListener {
                            override fun onClick(v: View?) {
                                mDisplayManager.resumeWifiDisplay()
                            }
                        })
                    }
                })
                mCertCategory!!.layoutResource = R.layout.two_buttons_panel
            }
        }

        // switch for Listen Mode
        var pref: SwitchPreference = object : SwitchPreference(requireContext()) {
            @SuppressLint("MissingPermission")
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onClick() {
                mListen = !mListen
                setListenMode(mListen)
                setChecked(mListen)
            }
        }
        pref.setTitle(R.string.wifi_display_listen_mode)
        pref.setChecked(mListen)
        mCertCategory!!.addPreference(pref)

        // switch for Autonomous GO
        pref = object : SwitchPreference(requireContext()) {
            @SuppressLint("MissingPermission")
            override fun onClick() {
                mAutoGO = !mAutoGO
                if (mAutoGO) {
                    startAutoGO()
                } else {
                    stopAutoGO()
                }
                setChecked(mAutoGO)
            }
        }
        pref.setTitle(R.string.wifi_display_autonomous_go)
        pref.setChecked(mAutoGO)
        mCertCategory!!.addPreference(pref)

        // Drop down list for choosing WPS method (PBC/KEYPAD/DISPLAY)
        var lp = ListPreference(requireContext())
        lp.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener {
            override fun onPreferenceChange(preference: Preference, value: Any?): Boolean {
                val wpsConfig = (value as String?)!!.toInt()
                if (wpsConfig != mWpsConfig) {
                    mWpsConfig = wpsConfig
                    requireActivity().invalidateOptionsMenu()
                    Settings.Global.putInt(
                        contentResolver,
                        Settings.Global.WIFI_DISPLAY_WPS_CONFIG, mWpsConfig
                    )
                }
                return true
            }
        }
        mWpsConfig = Settings.Global.getInt(
            contentResolver,
            Settings.Global.WIFI_DISPLAY_WPS_CONFIG, WpsInfo.INVALID
        )
        val wpsEntries = arrayOf<String?>("Default", "PBC", "KEYPAD", "DISPLAY")
        val wpsValues = arrayOf<String?>(
            "" + WpsInfo.INVALID,
            "" + WpsInfo.PBC,
            "" + WpsInfo.KEYPAD,
            "" + WpsInfo.DISPLAY
        )
        lp.setKey("wps")
        lp.setTitle(R.string.wifi_display_wps_config)
        lp.entries = wpsEntries
        lp.entryValues = wpsValues
        lp.setValue("" + mWpsConfig)
        lp.setSummary("%1\$s")
        mCertCategory!!.addPreference(lp)

        // Drop down list for choosing listen channel
        lp = ListPreference(requireContext())
        lp.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener {
            override fun onPreferenceChange(preference: Preference, value: Any?): Boolean {
                val channel = (value as String?)!!.toInt()
                if (channel != mListenChannel) {
                    mListenChannel = channel
                    requireActivity().invalidateOptionsMenu()
                    setWifiP2pChannels(mListenChannel, mOperatingChannel)
                }
                return true
            }
        }
        val lcEntries = arrayOf<String?>("Auto", "1", "6", "11")
        val lcValues = arrayOf<String?>("0", "1", "6", "11")
        lp.setKey("listening_channel")
        lp.setTitle(R.string.wifi_display_listen_channel)
        lp.entries = lcEntries
        lp.entryValues = lcValues
        lp.setValue("" + mListenChannel)
        lp.setSummary("%1\$s")
        mCertCategory!!.addPreference(lp)

        // Drop down list for choosing operating channel
        lp = ListPreference(requireContext())
        lp.onPreferenceChangeListener = object : Preference.OnPreferenceChangeListener {
            override fun onPreferenceChange(preference: Preference, value: Any?): Boolean {
                val channel = (value as String?)!!.toInt()
                if (channel != mOperatingChannel) {
                    mOperatingChannel = channel
                    requireActivity().invalidateOptionsMenu()
                    setWifiP2pChannels(mListenChannel, mOperatingChannel)
                }
                return true
            }
        }
        val ocEntries = arrayOf<String?>("Auto", "1", "6", "11", "36")
        val ocValues = arrayOf<String?>("0", "1", "6", "11", "36")
        lp.setKey("operating_channel")
        lp.setTitle(R.string.wifi_display_operating_channel)
        lp.entries = ocEntries
        lp.entryValues = ocValues
        lp.setValue("" + mOperatingChannel)
        lp.setSummary("%1\$s")
        mCertCategory!!.addPreference(lp)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun setListenMode(enable: Boolean) {
        if (DEBUG) {
            Slog.d(TAG, "Setting listen mode to: $enable")
        }
        val listener: WifiP2pManager.ActionListener = object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                if (DEBUG) {
                    Slog.d(
                        TAG, ("Successfully " + (if (enable) "entered" else "exited")
                                + " listen mode.")
                    )
                }
            }

            override fun onFailure(reason: Int) {
                Slog.e(
                    TAG, ("Failed to " + (if (enable) "entered" else "exited")
                            + " listen mode with reason " + reason + ".")
                )
            }
        }
        mWifiP2pChannel?.apply {
            if (enable) {
                mWifiP2pManager.startListening(this, listener)
            } else {
                mWifiP2pManager.stopListening(this, listener)
            }
        }
    }

    private fun setWifiP2pChannels(listeningChannel: Int, operatingChannel: Int) {
        if (DEBUG) {
            Slog.d(
                TAG,
                "Setting wifi p2p channel: listeningChannel=$listeningChannel, operatingChannel=$operatingChannel"
            )
        }
        mWifiP2pChannel?.let {
            mWifiP2pManager.setWifiP2pChannels(
                it,
                listeningChannel, operatingChannel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        if (DEBUG) {
                            Slog.d(TAG, "Successfully set wifi p2p channels.")
                        }
                    }

                    override fun onFailure(reason: Int) {
                        Slog.e(TAG, "Failed to set wifi p2p channels with reason $reason.")
                    }
                })
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
    private fun startAutoGO() {
        if (DEBUG) {
            Slog.d(TAG, "Starting Autonomous GO...")
        }
        mWifiP2pManager.createGroup(mWifiP2pChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                if (DEBUG) {
                    Slog.d(TAG, "Successfully started AutoGO.")
                }
            }

            override fun onFailure(reason: Int) {
                Slog.e(TAG, "Failed to start AutoGO with reason $reason.")
            }
        })
    }

    private fun stopAutoGO() {
        if (DEBUG) {
            Slog.d(TAG, "Stopping Autonomous GO...")
        }
        mWifiP2pManager.removeGroup(mWifiP2pChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                if (DEBUG) {
                    Slog.d(TAG, "Successfully stopped AutoGO.")
                }
            }

            override fun onFailure(reason: Int) {
                Slog.e(TAG, "Failed to stop AutoGO with reason $reason.")
            }
        })
    }

    private val mUpdateRunnable: Runnable = object : Runnable {
        override fun run() {
            val changes = mPendingChanges
            mPendingChanges = 0
            update(changes)
        }
    }
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("UnsafeImplicitIntentLaunch")
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED) {
                scheduleUpdate(CHANGE_WIFI_DISPLAY_STATUS)
            }
        }
    }
    private val mSettingsObserver: ContentObserver =
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                scheduleUpdate(CHANGE_SETTINGS)
            }
        }
    private val mRouterCallback: MediaRouter.Callback = object : MediaRouter.SimpleCallback() {
        override fun onRouteAdded(router: MediaRouter?, info: MediaRouter.RouteInfo?) {
            scheduleUpdate(CHANGE_ROUTES)
        }

        override fun onRouteChanged(router: MediaRouter?, info: MediaRouter.RouteInfo?) {
            scheduleUpdate(CHANGE_ROUTES)
        }

        override fun onRouteRemoved(router: MediaRouter?, info: MediaRouter.RouteInfo?) {
            scheduleUpdate(CHANGE_ROUTES)
        }

        override fun onRouteSelected(
            router: MediaRouter?,
            type: Int,
            info: MediaRouter.RouteInfo?,
        ) {
            scheduleUpdate(CHANGE_ROUTES)
        }

        override fun onRouteUnselected(
            router: MediaRouter?,
            type: Int,
            info: MediaRouter.RouteInfo?,
        ) {
            scheduleUpdate(CHANGE_ROUTES)
        }
    }

    companion object {
        private const val TAG = "WifiDisplaySettings"
        private const val DEBUG = false

        private val MENU_ID_ENABLE_WIFI_DISPLAY = Menu.FIRST

        private val CHANGE_SETTINGS = 1 shl 0
        private val CHANGE_ROUTES = 1 shl 1
        private val CHANGE_WIFI_DISPLAY_STATUS = 1 shl 2
        private val CHANGE_ALL = -1

        const val ORDER_CERTIFICATION = 1
        const val ORDER_CONNECTED = 2
        const val ORDER_AVAILABLE = 3
        const val ORDER_UNAVAILABLE = 4

        @JvmStatic
        fun isAvailable(context: Context): Boolean {
            return context.getSystemService(Context.DISPLAY_SERVICE) != null && context.packageManager
                .hasSystemFeature(
                    PackageManager.FEATURE_WIFI_DIRECT
                )
                    && context.getSystemService(Context.WIFI_P2P_SERVICE) != null
        }
    }
}

class WfdChangeResolution(private val context: Context) {

    // WFD sink supported
//    private var mDevicePref: SwitchPreference? = null
//    private var mP2pDevice: WifiP2pDevice? = null
//    fun onStart() {
//        dLog { "@M_$TAG onStart" }
//        if (FeatureOption.MTK_WFD_SINK_SUPPORT) {
//            val filter = IntentFilter()
//            filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
//            context.registerReceiver(mReceiver, filter)
//        }
//    }
//
//    /**
//     * Called when activity stopped.
//     */
//    fun onStop() {
//        dLog { "@M_$TAG onStop" }
//        if (FeatureOption.MTK_WFD_SINK_SUPPORT) {
//            context.unregisterReceiver(mReceiver)
//        }
//    }
//
//    private fun updateDeviceName() {
//        mP2pDevice?.apply {
//            dLog { "@M_$TAG updateDeviceName deviceName: $deviceName" }
//            mDevicePref?.apply {
//                title = if (deviceName.isNullOrEmpty()) {
//                    deviceAddress
//                } else {
//                    deviceName
//                }
//            }
//        }
//    }

    /**
     * Add change resolution option menu.
     *
     * @param menu
     * the menu that change resolution menu item will be added
     * @param status
     * current WFD status
     */
    fun onCreateOptionMenu(menu: Menu, status: WifiDisplayStatus?) {
        val currentResolution = Settings.Global.getInt(
            context.contentResolver,
            MtkSettingsExt.Global.WIFI_DISPLAY_RESOLUTION, 0
        )
        dLog { "@M_$TAG onCreateOptionMenu current resolution is: $currentResolution" }
        if (DEVICE_RESOLUTION_LIST.contains(currentResolution)) {
            status?.apply {
                menu.add(
                    Menu.NONE, MENU_ID_CHANGE_RESOLUTION,
                    0, R.string.wfd_change_resolution_menu_title
                )
                    .setEnabled(
                        status.featureState == WifiDisplayStatus.FEATURE_STATE_ON
                                && (status.activeDisplayState
                                != WifiDisplayStatus.DISPLAY_STATE_CONNECTING)
                    )
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            }
        }
    }

    /**
     * Called when the option menu is selected.
     *
     * @param item
     * the selected menu item
     * @param fragmentManager
     * Fragment manager used to show new fragment
     * @return true, change resolution item is selected, otherwise false
     */
    fun onOptionMenuSelected(item: MenuItem, fragmentManager: FragmentManager): Boolean {
        if (item.itemId == MENU_ID_CHANGE_RESOLUTION) {
            WfdChangeResolutionFragment().show(
                fragmentManager, "change resolution"
            )
            return true
        }
        return false
    }

    /**
     * Called when select one router to connect.
     */
    fun prepareWfdConnect() {
        if (FeatureOption.MTK_WFD_SINK_UIBC_SUPPORT) {
            val intent = Intent()
            intent.setClassName(
                FLOAT_MENU_PACKAGE,
                FLOAT_MENU_CLASS
            )
            context.startServiceAsUser(intent, UserHandle.CURRENT)
        }
    }
//    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent) {
//            val action = intent.action
//            vLog { "@M_$TAG onReceive action: $action" }
//            if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
//                == action
//            ) {
//                mP2pDevice = intent
//                    .getParcelableExtra<Parcelable?>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE) as WifiP2pDevice?
//                updateDeviceName()
//            }
//        }
//    }

    companion object {
        private const val TAG = "WfdChangeResolution"
        const val MENU_ID_CHANGE_RESOLUTION: Int = Menu.FIRST + 1

        // WFD sink UIBC supported
        const val FLOAT_MENU_PACKAGE: String = "com.mediatek.floatmenu"
        const val FLOAT_MENU_CLASS: String = "com.mediatek.floatmenu.FloatMenuService"

        /**
         * Device resolution:
         * 0: 720p 30fps menu disabled
         * 1: 1080p 30fps menu disabled
         * 2: 1080p 30fps
         * 3: 720p 30fps
         */
        val DEVICE_RESOLUTION_LIST: ArrayList<Int> = arrayListOf<Int>(0, 2, 3)
    }
}

open class RoutePreference(
    context: Context,
    private val mRoute: MediaRouter.RouteInfo,
    private val onPreferenceClick: (MediaRouter.RouteInfo) -> Unit,
) : TwoTargetPreference(context), Preference.OnPreferenceClickListener {
    init {
        title = mRoute.name
        setSummary(mRoute.description)
        isEnabled = mRoute.isEnabled
        if (mRoute.isSelected) {
            order = MiracastWfdFragment.ORDER_CONNECTED
            if (mRoute.isConnecting) {
                setSummary(R.string.wifi_display_status_connecting)
            } else {
                val status = mRoute.status
                if (!TextUtils.isEmpty(status)) {
                    setSummary(status)
                } else {
                    setSummary(R.string.wifi_display_status_connected)
                }
            }
        } else {
            if (isEnabled) {
                order = MiracastWfdFragment.ORDER_AVAILABLE
            } else {
                order = MiracastWfdFragment.ORDER_UNAVAILABLE
                if (mRoute.statusCode == MediaRouter.RouteInfo.STATUS_IN_USE) {
                    setSummary(R.string.wifi_display_status_in_use)
                } else {
                    setSummary(R.string.wifi_display_status_not_available)
                }
            }
        }
        onPreferenceClickListener = this
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        onPreferenceClick(mRoute)
        return true
    }
}

open class WifiDisplayRoutePreference(
    context: Context, route: MediaRouter.RouteInfo,
    private val mDisplay: WifiDisplay,
    private val onClick: (WifiDisplay) -> Unit,
) : RoutePreference(context, route, {}), View.OnClickListener {

    override fun getSecondTargetResId(): Int {
        return R.layout.preference_widget_gear
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val gear = holder.findViewById(R.id.settings_button) as ImageView?
        if (gear != null) {
            gear.setOnClickListener(this)
            if (!isEnabled) {
                val value = TypedValue()
                context.theme.resolveAttribute(
                    android.R.attr.disabledAlpha,
                    value, true
                )
                gear.imageAlpha = (value.float * 255).toInt()
                gear.isEnabled = true // always allow button to be pressed
            }
        }
    }

    override fun onClick(v: View?) {
        onClick(mDisplay)
    }
}

class UnpairedWifiDisplayPreference(
    context: Context,
    private val mDisplay: WifiDisplay,
    private val onPreferenceClick: (WifiDisplay) -> Unit,
) : Preference(context), Preference.OnPreferenceClickListener {
    init {
        title = mDisplay.friendlyDisplayName
        setSummary(com.android.internal.R.string.wireless_display_route_description)
        isEnabled = mDisplay.canConnect()
        if (isEnabled) {
            order = MiracastWfdFragment.ORDER_AVAILABLE
        } else {
            order = MiracastWfdFragment.ORDER_UNAVAILABLE
            setSummary(R.string.wifi_display_status_in_use)
        }
        onPreferenceClickListener = this
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        onPreferenceClick(mDisplay)
        return true
    }
}