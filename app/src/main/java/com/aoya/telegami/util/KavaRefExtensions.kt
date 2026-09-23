package com.aoya.telegami.util

import com.aoya.telegami.core.obfuscate.ResolverManager
import com.highcapable.kavaref.KavaRef.Companion.resolve
import java.lang.reflect.Method

fun ClassLoader.findMethod(
    className: String,
    methodName: String,
    parameterCount: Int? = null,
): Method {
    val resolvedClassName = ResolverManager.get(className)
    val resolvedMethodName = ResolverManager.getMethod(className, methodName)
    var targetClass: Class<*>? = loadClass(resolvedClassName)

    while (targetClass != null) {
        val currentClass = targetClass
        val method =
            currentClass
                .resolve()
                .firstMethodOrNull {
                    name = resolvedMethodName
                    if (parameterCount != null) parameterCount(parameterCount)
                }?.self
        if (method != null) return method
        targetClass = currentClass.superclass
    }

    error("Unable to find $resolvedMethodName on $resolvedClassName")
}
