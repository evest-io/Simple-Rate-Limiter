package io.evest.simpleratelimiter.test

import io.evest.simpleratelimiter.RateLimitException
import io.evest.simpleratelimiter.RateLimiter.tryBucket
import java.time.Duration
import kotlin.test.Test


class TestRateLimiter {

    @Test
    fun `test bucket rate limiter`() {
        val any = Any()
        for (i in 0..11) {
            val startTime = System.currentTimeMillis()
            try {
                any.tryBucket("1", 2, Duration.ofSeconds(1)) {
                    println("Rate limit exceeded for iteration $i")
                }
            } catch (e: RateLimitException) {
                println("Rate limit exceeded for iteration $i")
            }
            val currentTime = System.currentTimeMillis()

            val taken = currentTime - startTime
            println("Time taken: $taken ms for iteration $i")
        }

    }

}