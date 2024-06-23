import io.evest.simpleratelimiter.ThrottleBucket
import java.util.concurrent.ConcurrentHashMap

object Throttler {
    private val cacheMap = ConcurrentHashMap<String, ThrottleBucket>()

    fun synchronizedWaitValue(
        key: String?,
        millis: Long,
        minimumWaitMillis: Long,
        maxCacheTime: Long,
    ): Long = synchronized(cacheMap) {
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
                cacheMap[key] = ThrottleBucket(currentTime, newWait)
                newWait
            }

            else -> millis
        }
    }
}