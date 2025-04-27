package com.aoya.televip

import android.app.Application
import com.aoya.televip.core.Constants.SUPPORTED_TELEGRAM_PACKAGES
import com.aoya.televip.utils.HookStage
import com.aoya.televip.utils.hook
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XposedLoader :
    IXposedHookZygoteInit,
    IXposedHookLoadPackage {
    private lateinit var modulePath: String

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        modulePath = startupParam.modulePath
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!SUPPORTED_TELEGRAM_PACKAGES.contains(lpparam.packageName)) {
            return
        }

        Application::class.java.hook("attach", HookStage.AFTER) {
            val app = it.thisObject()
            TeleVip.init(modulePath, app)
        }
    }
}
