package com.aoya.telegami.util

fun String.toPascalCase(): String =
    replace(Regex("([a-z])([A-Z])")) { "${it.groupValues[1]}${it.groupValues[2]}" }
        .replaceFirstChar { it.uppercase() }
