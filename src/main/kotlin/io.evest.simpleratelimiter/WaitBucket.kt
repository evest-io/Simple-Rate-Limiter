package io.evest.simpleratelimit

data class WaitBucket(val lastCall: Long, val waitUntil: Long)