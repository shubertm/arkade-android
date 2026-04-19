package com.arkade.utils

actual fun Log.debug(
    tag: String,
    message: String,
) {
    android.util.Log.d(tag, message)
}

actual fun Log.info(
    tag: String,
    message: String,
) {
    android.util.Log.i(tag, message)
}

actual fun Log.warning(
    tag: String,
    message: String,
) {
    android.util.Log.w(tag, message)
}

actual fun Log.verbose(
    tag: String,
    message: String,
) {
    android.util.Log.v(tag, message)
}

actual fun Log.error(
    tag: String,
    message: String,
) {
    android.util.Log.e(tag, message)
}
