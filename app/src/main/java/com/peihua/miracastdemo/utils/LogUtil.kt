@file:JvmName("LogUtil")
@file:JvmMultifileClass

package com.peihua.miracastdemo.utils


private const val STACK_TRACE_INDEX = 6
fun <T> T.aLog(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.A, null, message)
    return this
}

fun <T> T.vLog(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.V, null, message)
    return this
}

fun <T> T.jsonLog(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.JSON, null, message)
    return this
}

fun <T> T.xmlLog(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.XML, null, message)
    return this
}

fun <T> T.wLog(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.W, null, message)
    return this
}

fun <T> T.eLog(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.E, null, message)
    return this
}

fun <T> T.dLog(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.D, null, message)
    return this
}

fun <T> T.iLog(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.I, null, message)
    return this
}

fun Int.dLog(lazyMessage: () -> Any): Int {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.D, null, message)
    return this
}

fun Double.dLog(lazyMessage: () -> Any): Double {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.D, null, message)
    return this
}

fun Float.dLog(lazyMessage: () -> Any): Float {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.D, null, message)
    return this
}

fun Long.dLog(lazyMessage: () -> Any): Long {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.D, null, message)
    return this
}


fun Int.eLog(lazyMessage: () -> Any): Int {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.D, null, message)
    return this
}

fun Double.eLog(lazyMessage: () -> Any): Double {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.E, null, message)
    return this
}

fun Float.eLog(lazyMessage: () -> Any): Float {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.E, null, message)
    return this
}

fun Long.eLog(lazyMessage: () -> Any): Long {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.E, null, message)
    return this
}

fun Int.wLog(lazyMessage: () -> Any): Int {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.W, null, message)
    return this
}

fun Double.wLog(lazyMessage: () -> Any): Double {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.W, null, message)
    return this
}

fun Float.wLog(lazyMessage: () -> Any): Float {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.W, null, message)
    return this
}

fun Long.wLog(lazyMessage: () -> Any): Long {
    val message = lazyMessage()
    Logcat.printLog(STACK_TRACE_INDEX, Logcat.W, null, message)
    return this
}