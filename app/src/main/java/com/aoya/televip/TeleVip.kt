package com.aoya.televip

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.aoya.televip.core.i18n.TranslationManager
import com.aoya.televip.core.obfuscate.ResolverManager
import com.aoya.televip.data.AppDatabase
import com.aoya.televip.utils.HookManager
import dalvik.system.DexClassLoader
import java.io.File
import kotlin.system.measureTimeMillis

object TeleVip {
    lateinit var context: Context
        private set
    lateinit var classLoader: ClassLoader
        private set
    lateinit var packageName: String
        private set

    lateinit var hookManager: HookManager

    lateinit var db: AppDatabase

    var currentActivity: Activity? = null
        private set

    fun init(
        modulePath: String,
        app: Application,
    ) {
        this.context = app

        TranslationManager.init(context)
        ResolverManager.init(context.packageName)

        val newModule = File(context.filesDir, "televip.dex")
        File(modulePath).copyTo(newModule, true)
        newModule.setReadOnly()

        this.classLoader =
            DexClassLoader(newModule.absolutePath, null, null, context.classLoader)
        this.hookManager = HookManager()
        this.packageName = context.packageName
        this.db = AppDatabase.getDatabase(context)

        app.registerActivityLifecycleCallbacks(
            object : ActivityLifecycleCallbacks {
                override fun onActivityCreated(
                    activity: Activity,
                    savedInstanceState: Bundle?,
                ) {}

                override fun onActivityStarted(activity: Activity) {}

                override fun onActivityResumed(activity: Activity) {
                    currentActivity = activity
                }

                override fun onActivityPaused(activity: Activity) {
                    if (currentActivity == activity) {
                        currentActivity = null
                    }
                }

                override fun onActivityStopped(activity: Activity) {}

                override fun onActivitySaveInstanceState(
                    activity: Activity,
                    outState: Bundle,
                ) {}

                override fun onActivityDestroyed(activity: Activity) {}
            },
        )

        try {
            val initTime = measureTimeMillis { init() }
        } catch (t: Throwable) {
            showToast(Toast.LENGTH_LONG, "Failed to initialize: ${t.message}")
            return
        }
    }

    private fun init() {
        hookManager.init()
    }

    fun runOnMainThread(
        appContext: Context? = null,
        block: (Context) -> Unit,
    ) {
        val useContext = appContext ?: context
        Handler(useContext.mainLooper).post {
            block(useContext)
        }
    }

    fun showToast(
        duration: Int,
        message: String,
        appContext: Context? = null,
    ) {
        val useContext = appContext ?: context
        runOnMainThread(useContext) {
            Toast.makeText(useContext, message, duration).show()
        }
    }

    fun loadClass(name: String): Class<*> = classLoader.loadClass(name)
}
