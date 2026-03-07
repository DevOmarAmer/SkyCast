package com.example.skycast.utils


sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    // always include data
    class Success<T>(data: T) : Resource<T>(data)

    // always include message and may include data
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)

    // in general it doesn't include data
    class Loading<T>(data: T? = null) : Resource<T>(data)
}