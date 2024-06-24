package io.evest.simpleratelimiter.test

import io.evest.simpleratelimiter.RateLimiter.bucket
import java.time.Duration
import kotlin.test.Test


class TestRateLimiter {

    @Test
    fun `test bucket rate limiter`() {
        val any = Any()
        for (i in 0..11) {
            val startTime = System.currentTimeMillis()
            any.bucket("1", 2, Duration.ofSeconds(1))
            val currentTime = System.currentTimeMillis()

            val taken = currentTime - startTime
            println("Time taken: $taken ms for iteration $i")

            if ((i + 1) % 3 == 0) {
                assert(taken > 1000)
            } else {
                assert(taken < 1000)
            }
        }

    }

}