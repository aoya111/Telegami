package com.aoya.telegami

import android.app.Application
import com.aoya.telegami.BuildConfig
import com.aoya.telegami.core.Constants
import com.aoya.telegami.core.Constants.SUPPORTED_TELEGRAM_PACKAGES
import com.aoya.telegami.utils.HookStage
import com.aoya.telegami.utils.hook
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XposedLoader :
    IXposedHookZygoteInit,
    IXposedHookLoadPackage {
    private lateinit var modulePath: String

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        modulePath = startupParam.modulePath
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (arrayOf(Constants.APP_ID, Constants.APP_DEBUG_ID).contains(lpparam.packageName)) {
            findAndHookMethod(
                "${Constants.APP_ID}.service.PrefManager",
                lpparam.classLoader,
                "getActiveVersion",
                XC_MethodReplacement.returnConstant(BuildConfig.VERSION_CODE),
            )
        }

        if (!SUPPORTED_TELEGRAM_PACKAGES.contains(lpparam.packageName)) {
            return
        }

        Application::class.java.hook("attach", HookStage.AFTER) {
            val app = it.thisObject()
            Telegami.init(modulePath, app)
        }
    }
}
