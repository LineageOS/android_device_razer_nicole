/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.telephonydisabler

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.ApplicationInfoFlags
import android.util.Log

object TelephonyDisabler {
    private const val TAG = "TelephonyDisabler"

    private val TELEPHONY_PACKAGES = listOf(
        "com.android.messaging",
        "com.android.ons",
        "com.google.android.messages",
        "com.oma.dm.sub",
        "com.razer.verizondata",
        "com.verizon.mips.services",
        "com.vzw.apnlib",
    )

    private fun isInstalled(pm: PackageManager, pkgName: String) =
        runCatching {
                val info = pm.getApplicationInfo(pkgName, ApplicationInfoFlags.of(0))
                info.flags and ApplicationInfo.FLAG_INSTALLED != 0
            }
            .getOrDefault(false)

    fun enableOrDisablePackages(context: Context) {
        val pm = context.packageManager
        val disable = !pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        val flag =
            if (disable) {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            }

        for (pkg in TELEPHONY_PACKAGES) {
            if (isInstalled(pm, pkg)) {
                pm.setApplicationEnabledSetting(pkg, flag, 0)
            }
        }
    }
}
