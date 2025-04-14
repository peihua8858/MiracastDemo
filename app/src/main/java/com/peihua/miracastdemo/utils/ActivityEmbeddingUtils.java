/*
 * Copyright (C) 2021 The Android Open Source Project
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
 * limitations under the License.
 */

package com.peihua.miracastdemo.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.peihua.miracastdemo.R;

/** An util class collecting all common methods for the embedding activity features. */
public class ActivityEmbeddingUtils {
    // The smallest value of current width of the window when the split should be used.
    private static final float MIN_CURRENT_SCREEN_SPLIT_WIDTH_DP = 720f;
    // The smallest value of the smallest-width (sw) of the window in any rotation when
    // the split should be used.
    private static final float MIN_SMALLEST_SCREEN_SPLIT_WIDTH_DP = 600f;
    // The minimum width of the activity to show the regular homepage layout.
    private static final float MIN_REGULAR_HOMEPAGE_LAYOUT_WIDTH_DP = 380f;
    private static final String TAG = "ActivityEmbeddingUtils";

    private static boolean setSplitFlag = true;
    private static int mOrientationSettings = 0;

    /** Get the smallest pixel value of width of the window when the split should be used. */
    public static int getMinCurrentScreenSplitWidthPx(Context context) {
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, MIN_CURRENT_SCREEN_SPLIT_WIDTH_DP, dm);
    }

    /**
     * Get the smallest pixel value of the smallest-width (sw) of the window in any rotation when
     * the split should be used.
     */
    public static int getMinSmallestScreenSplitWidthPx(Context context) {
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, MIN_SMALLEST_SCREEN_SPLIT_WIDTH_DP, dm);
    }

    /**
     * Get the ratio to use when splitting windows. This should be a float which describes
     * the percentage of the screen which the first window should occupy.
     */
    public static float getSplitRatio(Context context) {
        return context.getResources().getFloat(R.dimen.config_activity_embed_split_ratio);
    }

    /** Whether to support embedding activity feature. */
//    public static boolean isEmbeddingActivityEnabled(Context context) {
//        final boolean isFlagEnabled = FeatureFlagUtils.isEnabled(context,
//                FeatureFlagUtils.SETTINGS_SUPPORT_LARGE_SCREEN);
//        final boolean isSplitSupported = SplitController.getInstance().isSplitSupported();
//
//        Log.d(TAG, "isFlagEnabled = " + isFlagEnabled);
//        Log.d(TAG, "isSplitSupported = " + isSplitSupported);
//        Log.d(TAG, "iswfdsink = " + (SystemProperties.get("ro.vendor.mtk_wfd_sink_support", "0").equals("1")));
//
//        //return isFlagEnabled && isSplitSupported && (!SystemProperties.get("ro.vendor.mtk_wfd_sink_support", "0").equals("1"));
//        return isFlagEnabled && isSplitSupported && setSplitFlag;
//    }

    /** Whether to show the regular or simplified homepage layout. */
    public static boolean isRegularHomepageLayout(Activity activity) {
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        return dm.widthPixels >= (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, MIN_REGULAR_HOMEPAGE_LAYOUT_WIDTH_DP, dm);
    }

    public static void setIsSplitEnabled(boolean isSplitEnabled) {
          setSplitFlag = isSplitEnabled;
      }

    public static int getOrientationSettings() {
        return mOrientationSettings;
    }

    public static void setOrientationSettings(int oritationSettings) {
        mOrientationSettings = oritationSettings;
    }
}
