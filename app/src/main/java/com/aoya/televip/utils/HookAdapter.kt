package com.aoya.televip.utils

import android.os.Build
import androidx.annotation.RequiresApi
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Member
import java.util.function.Consumer

@Suppress("UNCHECKED_CAST")
class HookAdapter<Clazz>(
    private val methodHookParam: XC_MethodHook.MethodHookParam,
) {
    fun thisObject(): Clazz = methodHookParam.thisObject as Clazz

    fun nullableThisObject(): Clazz? = methodHookParam.thisObject as Clazz?

    fun method(): Member = methodHookParam.method

    fun <T : Any> arg(index: Int): T = methodHookParam.args[index] as T

    fun <T : Any> arg(
        index: Int,
        clazz: Class<T>,
    ): T? {
        val argValue = methodHookParam.args[index]
        return try {
            clazz.cast(argValue)
        } catch (e: ClassCastException) {
            convertToType(argValue, clazz) ?: handlePrimitiveDefaults(clazz)
        }
    }

    fun <T : Any> argNullable(index: Int): T? = methodHookParam.args.getOrNull(index) as T?

    fun setArg(
        index: Int,
        value: Any?,
    ) {
        if (index < 0 || index >= methodHookParam.args.size) return
        methodHookParam.args[index] = value
    }

    fun args(): Array<Any?> = methodHookParam.args

    fun getResult(): Any? = methodHookParam.result

    fun setResult(result: Any?) {
        methodHookParam.result = result
    }

    fun setThrowable(throwable: Throwable) {
        methodHookParam.throwable = throwable
    }

    fun throwable(): Throwable? = methodHookParam.throwable

    fun invokeOriginal(): Any? = XposedBridge.invokeOriginalMethod(method(), thisObject(), args())

    fun invokeOriginal(args: Array<Any?>): Any? = XposedBridge.invokeOriginalMethod(method(), thisObject(), args)

    @RequiresApi(Build.VERSION_CODES.N)
    fun invokeOriginalSafe(errorCallback: Consumer<Throwable>) {
        invokeOriginalSafe(args(), errorCallback)
    }

    fun invokeOriginalSafe(
        args: Array<Any?>,
        errorCallback: Consumer<Throwable>,
    ) {
        runCatching {
            setResult(XposedBridge.invokeOriginalMethod(method(), thisObject(), args))
        }.onFailure {
            errorCallback.accept(it)
        }
    }

    private fun invokeMethodSafe(
        obj: Any,
        methodName: String,
    ): Any? =
        try {
            obj::class.java.getMethod(methodName).invoke(obj)
        } catch (e: NoSuchMethodException) {
            null
        }

    private fun <T : Any> handlePrimitiveDefaults(clazz: Class<T>): T? =
        when (clazz) {
            Int::class.java -> 0 as T
            Double::class.java -> 0.0 as T
            Float::class.java -> 0f as T
            Long::class.java -> 0L as T
            Boolean::class.java -> false as T
            else -> null
        }

    fun <T : Any> convertToType(
        arg: Any,
        clazz: Class<T>,
    ): T? =
        try {
            when (clazz) {
                String::class.java -> invokeMethodSafe(arg, "toString") as T
                Int::class.java -> invokeMethodSafe(arg, "toInt") as T
                Double::class.java -> invokeMethodSafe(arg, "toDouble") as T
                Float::class.java -> invokeMethodSafe(arg, "toFloat") as T
                Long::class.java -> invokeMethodSafe(arg, "toLong") as T
                Boolean::class.java -> invokeMethodSafe(arg, "toBoolean") as T
                else -> null
            }
        } catch (_: Exception) {
            null
        }
}
