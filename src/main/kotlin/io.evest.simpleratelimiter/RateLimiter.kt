package io.evest.simpleratelimit

import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

private const val defaultMinWait = 10L
private const val defaultMaxCache = 60L

object RateLimiter {
    private val cacheMap = ConcurrentHashMap<String, WaitBucket>()

    private var minimumWaitMillis: Long = Duration.ofSeconds(defaultMinWait).toMillis()
    private var maxCacheTime: Long = Duration.ofSeconds(defaultMaxCache).toMillis()

    /** Set the minimum wait time between calls and the maximum time a value is cached. */
    fun settings(
        minimumWaitMillis: Duration = Duration.ofSeconds(defaultMinWait),
        maxCacheTime: Duration = Duration.ofSeconds(defaultMaxCache)
    ) {
        this.minimumWaitMillis = minimumWaitMillis.toMillis()
        this.maxCacheTime = maxCacheTime.toMillis()
    }

    private fun synchronizedWaitValue(key: String?, millis: Long): Long = synchronized(cacheMap) {
        key ?: return@synchronized millis

        val currentTime = System.currentTimeMillis()
        // Clear old cache entries
        cacheMap.entries.removeIf { currentTime - it.value.lastCall > maxCacheTime }

        val bucket = cacheMap[key]
        val lastCall = bucket?.lastCall ?: currentTime
        val diffMillis = currentTime - lastCall

        when {
            diffMillis < minimumWaitMillis -> {
                val newWait = (bucket?.waitUntil?.times(1.5) ?: millis.toDouble()).toLong()
                cacheMap[key] = WaitBucket(currentTime, newWait)
                newWait
            }

            else -> millis
        }
    }

    fun <T> T.throttle(key: String? = null, millis: Long = 1_500): T = also {
        val waitUntil = synchronizedWaitValue(key, millis)
        Thread.sleep(waitUntil)
    }
}
