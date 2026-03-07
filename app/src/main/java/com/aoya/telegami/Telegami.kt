package com.aoya.telegami

import android.app.Application
import android.content.Context
import android.os.Handler
import android.widget.Toast
import com.aoya.telegami.core.Config
import com.aoya.telegami.core.i18n.TranslationManager
import com.aoya.telegami.core.obfuscate.ResolverManager
import com.aoya.telegami.data.AppDatabase
import com.aoya.telegami.utils.HookManager
import kotlin.system.measureTimeMillis

object Telegami {
    lateinit var context: Context
        private set
    lateinit var classLoader: ClassLoader
        private set
    lateinit var packageName: String
        private set

    lateinit var hookManager: HookManager

    lateinit var db: AppDatabase

    fun init(
        modulePath: String,
        app: Application,
    ) {
        this.context = app

        Config.init(context.packageName)
        TranslationManager.init(context, modulePath)
        ResolverManager.init(context.packageName, modulePath)

        // TODO: This line can be removed in a future version once all users have updated.
        // Legacy cleanup: remove the telegami.dex file that was unnecessarily copied on every launch.
        context.filesDir.resolve("telegami.dex").delete()

        this.classLoader = context.classLoader
        this.hookManager = HookManager()
        this.packageName = context.packageName
        this.db = AppDatabase.getDatabase(context)

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
