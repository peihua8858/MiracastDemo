/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.peihua.miracastdemo;


import android.content.Context;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class WifiDisplayPreferenceController extends BasePreferenceController implements
        LifecycleObserver, OnStart, OnStop {
    /**
     * The setting is available, and searchable to all search clients.
     */
    public static final int AVAILABLE = 0;

    /**
     * The setting is available, but is not searchable to any search client.
     */
    public static final int AVAILABLE_UNSEARCHABLE = 1;
    /**
     * The setting is not, and will not supported by this device.
     * <p>
     * There is no guarantee that the setting page exists, and any links to the Setting should take
     * you to the home page of Settings.
     */
    public static final int UNSUPPORTED_ON_DEVICE = 3;


    /**
     * The setting cannot be changed by the current user.
     * <p>
     * Links to the Setting should take you to the page of the Setting, even if it cannot be
     * changed.
     */
    public static final int DISABLED_FOR_USER = 4;

    private final MediaRouter mRouter;
    private Preference mPreference;
    private WifiManager mWifiManager;
    private Context mContext = null;
    private final MediaRouter.Callback mRouterCallback = new MediaRouter.SimpleCallback() {
        @Override
        public void onRouteSelected(MediaRouter router, int type, RouteInfo info) {
            refreshSummary(mPreference);
        }

        @Override
        public void onRouteUnselected(MediaRouter router, int type, RouteInfo info) {
            refreshSummary(mPreference);
        }

        @Override
        public void onRouteAdded(MediaRouter router, RouteInfo info) {
            refreshSummary(mPreference);
        }

        @Override
        public void onRouteRemoved(MediaRouter router, RouteInfo info) {
            refreshSummary(mPreference);
        }

        @Override
        public void onRouteChanged(MediaRouter router, RouteInfo info) {
            refreshSummary(mPreference);
        }
    };

    public WifiDisplayPreferenceController(Context context, String key) {
        super(context, key);
        mContext = context;
        mRouter = context.getSystemService(MediaRouter.class);
        mRouter.setRouterGroupId(MediaRouter.MIRRORING_GROUP_ID);
    }

    @Override
    public int getAvailabilityStatus() {
        return WifiDisplaySettings.isAvailable(mContext) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        mWifiManager = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);

        if(mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED ||
           mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLING){
               mPreference.setEnabled(false);
        }
    }

    @Override
    public CharSequence getSummary() {
        CharSequence summary = mContext.getString(R.string.disconnected);

        final int routeCount = mRouter.getRouteCount();
        for (int i = 0; i < routeCount; i++) {
            final RouteInfo route = mRouter.getRouteAt(i);
            if (route.matchesTypes(MediaRouter.ROUTE_TYPE_REMOTE_DISPLAY)
                    && route.isSelected() && !route.isConnecting()) {
                CharSequence status = route.getStatus();
                if (!TextUtils.isEmpty(status)) {
                    summary = status;
                } else {
                    summary = mContext.getString(R.string.wifi_display_status_connected);
                }
                break;
            }
        }
        return summary;
    }

    @Override
    public void onStart() {
        mRouter.addCallback(MediaRouter.ROUTE_TYPE_REMOTE_DISPLAY, mRouterCallback);
    }

    @Override
    public void onStop() {
        mRouter.removeCallback(mRouterCallback);
    }
}
