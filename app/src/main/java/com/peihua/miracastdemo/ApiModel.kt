package com.peihua.miracastdemo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import java.util.concurrent.CancellationException

/**
 * api 方法封装
 * @author dingpeihua
 * @date 2021/2/19 17:45
 * @version 1.0
 */
class ApiModel {
    companion object {
        private const val TAG = "ApiModel"
    }
    private var coroutineScope: CoroutineScope? = null

    private var onStart: (() -> Unit?)? = null


    private var onError: ((Throwable) -> Unit?)? = null

    private var onComplete: (() -> Unit?)? = null
    private var onCancel: (() -> Unit?)? = null
    private var isCanceledJob = false

    internal fun isOnStart(): Boolean {
        return onStart != null
    }

    internal fun isOnCancel(): Boolean {
        return onCancel != null
    }

    internal fun isOnError(): Boolean {
        return onError != null
    }

    internal fun isOnComplete(): Boolean {
        return onComplete != null
    }

    infix fun onStart(onStart: (() -> Unit?)?): ApiModel {
        this.onStart = onStart
        return this
    }


    infix fun onError(onError: ((Throwable) -> Unit)?): ApiModel {
        this.onError = onError
        return this
    }

    infix fun onComplete(onComplete: (() -> Unit)?): ApiModel {
        this.onComplete = onComplete
        return this
    }

    infix fun onCancel(onCancel: (() -> Unit)?): ApiModel {
        this.onCancel = onCancel
        return this
    }

    val isCanceled: Boolean
        get() = isCanceledJob

    fun cancelJob() {
        isCanceledJob = true
        coroutineScope?.cancel(CancellationException("Close dialog"))
        invokeOnCancel()
    }


    fun invokeOnError(e: Throwable) {
        if (isCanceled) return
        this.onError?.invoke(e)
    }

    fun invokeOnStart() {
        if (isCanceled) return
        this.onStart?.invoke()
    }

    fun invokeOnComplete() {
        if (isCanceled) return
        this.onComplete?.invoke()
    }

    fun invokeOnCancel() {
        this.onCancel?.invoke()
    }
}