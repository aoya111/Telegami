package com.aoya.telegami.util

import android.util.Log
import com.aoya.telegami.BuildConfig

object Logger {
    private const val TAG = "Telegami"
    private var xposedLogger: ((Int, String, String, Throwable?) -> Unit)? = null

    fun attachXposedLogger(logger: (Int, String, String, Throwable?) -> Unit) {
        xposedLogger = logger
    }

    // Verbose - debug builds only, never to Xposed
    fun v(
        message: String,
        source: String? = null,
    ) {
        if (!BuildConfig.ENABLE_LOGS) return
        val fullMessage = if (source != null) "[$source] $message" else message
        Log.v(TAG, fullMessage)
    }

    // Debug - debug builds only, never to Xposed
    fun d(
        message: String,
        source: String? = null,
    ) {
        if (!BuildConfig.ENABLE_LOGS) return
        val fullMessage = if (source != null) "[$source] $message" else message
        Log.d(TAG, fullMessage)
    }

    // Info - logged but not to Xposed in release
    fun i(
        message: String,
        source: String? = null,
    ) = log(message, Log.INFO, source)

    // Warn - logged to Xposed in debug only
    fun w(
        message: String,
        source: String? = null,
        xposed: Boolean = true,
    ) = log(message, Log.WARN, source, xposed)

    // Error - always logged to Xposed
    fun e(
        message: String,
        source: String? = null,
        xposed: Boolean = true,
    ) = log(message, Log.ERROR, source, xposed)

    // Error with exception - always logged
    fun e(
        message: String,
        throwable: Throwable,
        source: String? = null,
    ) {
        val fullMessage =
            if (source != null) {
                "[$source] $message: ${throwable.message}"
            } else {
                "$message: ${throwable.message}"
            }

        if (BuildConfig.ENABLE_LOGS) {
            Log.e(TAG, fullMessage, throwable)
        }
        xposedLogger?.invoke(Log.ERROR, TAG, fullMessage, throwable)
    }

    private fun log(
        message: String,
        level: Int,
        source: String?,
        forceXposed: Boolean = false,
    ) {
        val fullMessage = if (source != null) "[$source] $message" else message

        if (BuildConfig.ENABLE_LOGS) {
            Log.println(level, TAG, fullMessage)
        }
        if (forceXposed) {
            xposedLogger?.invoke(level, TAG, fullMessage, null)
        }
    }
}

fun Any.logd(message: String) = Logger.d(message, this::class.simpleName)

fun Any.logi(message: String) = Logger.i(message, this::class.simpleName)

fun Any.logw(message: String) = Logger.w(message, this::class.simpleName)

fun Any.loge(message: String) = Logger.e(message, this::class.simpleName)

fun Any.loge(
    message: String,
    throwable: Throwable,
) = Logger.e(message, throwable, this::class.simpleName)
