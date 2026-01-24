package com.aoya.telegami.utils

import android.util.Log
import com.aoya.telegami.BuildConfig
import de.robv.android.xposed.XposedBridge

object Logger {
    private const val TAG = "Telegami"

    private fun shouldLogToXposed(level: Int): Boolean =
        when {
            BuildConfig.DEBUG -> level >= Log.WARN
            else -> level >= Log.ERROR // Only errors in release
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

        XposedBridge.log("[$TAG] ERROR: $fullMessage")
        XposedBridge.log(Log.getStackTraceString(throwable))

        if (BuildConfig.ENABLE_LOGS) {
            Log.e(TAG, fullMessage, throwable)
        }
    }

    private fun log(
        message: String,
        level: Int,
        source: String?,
        forceXposed: Boolean = false,
    ) {
        val fullMessage = if (source != null) "[$source] $message" else message

        if (forceXposed || shouldLogToXposed(level)) {
            XposedBridge.log("[$TAG] $fullMessage")
        }

        if (BuildConfig.ENABLE_LOGS) {
            Log.println(level, TAG, fullMessage)
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
