package io.evest.simpleratelimiter

import BucketHandler
import Throttler
import java.time.Duration

private const val defaultMinWait = 10L
private const val defaultMaxCache = 60L

object RateLimiter {
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


    fun <T> T.throttle(key: String? = null, millis: Long = 1_500): T = also {
        val waitUntil = Throttler.synchronizedWaitValue(key, millis, minimumWaitMillis, maxCacheTime)
        Thread.sleep(waitUntil)
    }


    /**
     * Bucket the call, if the rate limit is exceeded, throw a [RateLimitException].
     */
    fun <T> T.bucket(
        key: String,
        maxCalls: Int = 1,
        per: Duration = Duration.ofSeconds(defaultMinWait),
    ): T = also {
        val exhausted = BucketHandler.count(key, maxCalls, per)
        if (exhausted)
            throw RateLimitException("Rate limit exceeded for key: $key")
    }

    /**
     * Bucket the call, if the rate limit is exceeded, call the [error] function.
     */
    fun <T> T.tryBucket(
        key: String,
        maxCalls: Int = 1,
        per: Duration = Duration.ofSeconds(defaultMinWait),
        error: (exception: RateLimitException?) -> Unit = {},
    ): T = also {
        val exhausted = when (BucketHandler.count(key, maxCalls, per)) {
            true -> RateLimitException("Rate limit exceeded for key: $key")
            false -> null
        }
        error(exhausted)
    }
}
