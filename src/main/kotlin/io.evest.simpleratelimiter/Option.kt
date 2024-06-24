package io.evest.simpleratelimiter

data class Option<T>(val some: T, val exception: Exception? = null)