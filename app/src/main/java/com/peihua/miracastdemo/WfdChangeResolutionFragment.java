/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
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


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.fragment.app.DialogFragment;

import com.mediatek.provider.MtkSettingsExt;

/**
 * Dialog fragment for setting the discoverability timeout.
 */
public final class WfdChangeResolutionFragment extends DialogFragment
        implements DialogInterface.OnClickListener {
    private static final String TAG = "WfdChangeResolutionFragment";
    private int mCurrentResolution = 0;
    private int mWhichIndex = 0;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mCurrentResolution = Settings.Global.getInt(getActivity().getContentResolver(),
                MtkSettingsExt.Global.WIFI_DISPLAY_RESOLUTION, 0);
        Log.d("@M_" + TAG, "create dialog, current resolution is " + mCurrentResolution);
        int resolutionArray = R.array.wfd_resolution_entry;
        int resolutionIndex = WfdChangeResolution.DEVICE_RESOLUTION_LIST.indexOf(mCurrentResolution);
        mWhichIndex = resolutionIndex;
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.wfd_change_resolution_menu_title)
                .setSingleChoiceItems(resolutionArray,
                 resolutionIndex, this)
                .setPositiveButton(android.R.string.ok, this)
                .create();
    }
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            int userChoice = WfdChangeResolution.DEVICE_RESOLUTION_LIST.get(mWhichIndex);
            Log.d("@M_" + TAG, "User click ok button, set resolution as "
                    + userChoice);
            Settings.Global.putInt(getActivity().getContentResolver(),
                    MtkSettingsExt.Global.WIFI_DISPLAY_RESOLUTION, userChoice);
        } else {
            mWhichIndex = which;
            Log.d("@M_" + TAG, "User select the item " + mWhichIndex);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (!WfdChangeResolution.DEVICE_RESOLUTION_LIST.contains(mCurrentResolution)) {
            dismiss();
        }
    }
}
