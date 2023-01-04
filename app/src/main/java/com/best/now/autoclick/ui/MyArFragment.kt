package com.best.now.autoclick.ui
import com.blankj.utilcode.util.ToastUtils
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.sceneform.ux.ArFragment

//可以直接使用ArFragment   我这里为了中文提示
class MyArFragment : ArFragment() {
    override fun handleSessionException(sessionException: UnavailableException) {
        val message: String
        when (sessionException) {
            is UnavailableArcoreNotInstalledException -> message = "Please install ARCore"
            is UnavailableApkTooOldException -> message = "Please upgrade ARCore"
            is UnavailableSdkTooOldException -> message = "Please upgrade the app"
            is UnavailableDeviceNotCompatibleException -> message = "The current device department does not support AR"
            else -> {
                message = "Failed to create an AR session, please check the model adaptation, arcore version and system version"
                val var3 = sessionException.toString()
            }
        }
        ToastUtils.showLong(message)
    }
}