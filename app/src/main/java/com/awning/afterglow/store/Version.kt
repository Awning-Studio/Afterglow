package com.awning.afterglow.store

import android.content.pm.PackageInfo
import com.awning.afterglow.ApplicationContext

/**
 * Afterglow 版本
 */
object Version {
    private val packageInfo: PackageInfo =
        ApplicationContext.packageManager.getPackageInfo(ApplicationContext.packageName, 0)

    /**
     * 当前版本名
     */
    val name: String = packageInfo.versionName
}