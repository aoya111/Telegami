package com.aoya.telegami

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.widget.Toast
import com.aoya.telegami.core.i18n.TranslationManager
import com.aoya.telegami.core.obfuscate.ResolverManager
import com.aoya.telegami.data.AppDatabase
import com.aoya.telegami.service.Config
import com.aoya.telegami.service.UserConfig
import kotlin.system.measureTimeMillis

object Telegami {
    lateinit var context: Context
        private set
    lateinit var classLoader: ClassLoader
        private set
    lateinit var packageName: String
        private set

    lateinit var db: AppDatabase

    fun init(
        modulePath: String,
        app: Application,
    ) {
        this.context = app

        Config.init(context)
        UserConfig.init(context)
        TranslationManager.init(context, modulePath)
        ResolverManager.init(context.packageName, modulePath)

        this.classLoader = context.classLoader
        this.packageName = context.packageName
        this.db = AppDatabase.getDatabase(context)
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

    fun getResource(
        name: String,
        type: String,
    ): Int = context?.resources?.getIdentifier(name, type, packageName) ?: 0

    fun getDrawableResource(name: String): Drawable? =
        getResource("msg_gallery", "drawable")?.takeIf { it != 0 }?.let {
            context?.getDrawable(it)
        }
}
