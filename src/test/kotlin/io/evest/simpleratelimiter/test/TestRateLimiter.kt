package io.evest.simpleratelimiter.test

import io.evest.simpleratelimiter.RateLimiter.bucket
import kotlin.test.Test


class TestRateLimiter {

    @Test
    fun `test bucket rate limiter`() {
        val any = Any()
        val startTime = System.currentTimeMillis()
        for (i in 0..100) {
            any.bucket("1")
            val currentTime = System.currentTimeMillis()

            println("Time taken: ${currentTime - startTime}ms for iteration $i")
        }

    }

}