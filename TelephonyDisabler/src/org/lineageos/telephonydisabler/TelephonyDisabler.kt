/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.telephonydisabler

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.ApplicationInfoFlags
import android.os.SystemProperties
import android.util.Log

object TelephonyDisabler {
    private const val TAG = "TelephonyDisabler"

    private const val SKU_PROPERTY = "ro.boot.hardware.sku"
    private const val SKU_WIFI = "wifi"

    private val EUICC_DEPENDENCIES = listOf(
        "com.google.android.gms",
        "com.google.android.gsf",
    )

    private val EUICC_PACKAGES = listOf(
        "com.google.android.euicc",
        "com.google.android.ims",
    )

    private val ADDITIONAL_PACKAGES = listOf(
        "com.android.messaging",
    )

    private fun isInstalled(pm: PackageManager, pkgName: String) = runCatching {
        val info = pm.getApplicationInfo(pkgName, ApplicationInfoFlags.of(0))
        info.flags and ApplicationInfo.FLAG_INSTALLED != 0
    }.getOrDefault(false)

    private fun isInstalledAndEnabled(pm: PackageManager, pkgName: String) = runCatching {
        val info = pm.getApplicationInfo(pkgName, ApplicationInfoFlags.of(0))
        Log.d(TAG, "package $pkgName installed, enabled = ${info.enabled}")
        info.enabled
    }.getOrDefault(false)

    fun enableOrDisablePackages(context: Context) {
        val pm = context.packageManager
        val isWifi = SystemProperties.get(SKU_PROPERTY, "") == SKU_WIFI
        val gmsMissing = EUICC_DEPENDENCIES.any { !isInstalledAndEnabled(pm, it) }

        mapOf(
            EUICC_PACKAGES to (isWifi || gmsMissing),
            ADDITIONAL_PACKAGES to isWifi
        ).forEach { (pkgs, shouldDisable) ->
            val flag = if (shouldDisable) {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            }

            for (pkg in pkgs) {
                if (isInstalled(pm, pkg)) {
                    pm.setApplicationEnabledSetting(pkg, flag, 0)
                }
            }
        }
    }
}
