package com.id.administrasikaligentong.util

sealed class State<out T> {
    data class Success<T>(val data: T): State<T>()
    data class Failure(val throwable: Throwable): State<Nothing>()
    object Loading: State<Nothing>()
}