package io.evest.simpleratelimiter

data class ThrottleBucket(val lastCall: Long, val waitUntil: Long)