package com.arkade.utils

import java.util.logging.Level
import java.util.logging.Logger

actual fun Log.debug(
    tag: String,
    message: String,
) {
    Logger.getLogger(tag).log(Level.FINE, message)
}

actual fun Log.info(
    tag: String,
    message: String,
) {
    Logger.getLogger(tag).info(message)
}

actual fun Log.warning(
    tag: String,
    message: String,
) {
    Logger.getLogger(tag).warning(message)
}

actual fun Log.verbose(
    tag: String,
    message: String,
) {
    Logger.getLogger(tag).log(Level.ALL, message)
}

actual fun Log.error(
    tag: String,
    message: String,
) {
    Logger.getLogger(tag).log(Level.SEVERE, message)
}
