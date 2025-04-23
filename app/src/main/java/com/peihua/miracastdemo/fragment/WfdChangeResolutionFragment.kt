package com.peihua.miracastdemo.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.DialogFragment
import com.mediatek.provider.MtkSettingsExt
import com.peihua.miracastdemo.R
import com.peihua.miracastdemo.utils.dLog


/**
 * Dialog fragment for setting the discoverability timeout.
 */
class WfdChangeResolutionFragment : DialogFragment(), DialogInterface.OnClickListener {
    private var mCurrentResolution = 0
    private var mWhichIndex = 0
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mCurrentResolution = Settings.Global.getInt(
            requireActivity().contentResolver,
            MtkSettingsExt.Global.WIFI_DISPLAY_RESOLUTION, 0
        )
        dLog { "@M_" + TAG + "create dialog, current resolution is " + mCurrentResolution }
        val resolutionArray = R.array.wfd_resolution_entry
        val resolutionIndex = WfdChangeResolution.Companion.DEVICE_RESOLUTION_LIST.indexOf(mCurrentResolution)
        mWhichIndex = resolutionIndex
        return AlertDialog.Builder(activity)
            .setTitle(R.string.wfd_change_resolution_menu_title)
            .setSingleChoiceItems(
                resolutionArray,
                resolutionIndex, this
            )
            .setPositiveButton(android.R.string.ok, this)
            .create()
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            val userChoice = WfdChangeResolution.Companion.DEVICE_RESOLUTION_LIST.get(mWhichIndex)
            dLog { "@M_" + TAG + "User click ok button, set resolution as " + userChoice }
            Settings.Global.putInt(
                requireActivity().contentResolver,
                MtkSettingsExt.Global.WIFI_DISPLAY_RESOLUTION, userChoice
            )
        } else {
            mWhichIndex = which
            dLog { "@M_" + TAG + "User select the item " + mWhichIndex }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!WfdChangeResolution.Companion.DEVICE_RESOLUTION_LIST.contains(mCurrentResolution)) {
            dismiss()
        }
    }

    companion object {
        private const val TAG = "WfdChangeResolutionFragment"
    }
}
