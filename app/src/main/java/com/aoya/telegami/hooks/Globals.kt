package com.aoya.telegami.hooks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.atomic.AtomicBoolean

object Globals {
    val allowMsgDelete = AtomicBoolean(false)

    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
