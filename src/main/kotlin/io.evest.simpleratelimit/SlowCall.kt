import java.time.Duration
import java.util.concurrent.ConcurrentLinkedQueue

data class WaitBucket(
    val key: String,
    val lastCall: Long,
    val firstCall: Long,
    val waitUntil: Long,
)

object SlowCall {
    private val cacheMap = ConcurrentLinkedQueue<WaitBucket>()
    private var minimumWaitMillis: Long = Duration.ofSeconds(10).toMillis()
    private var maxCacheTime: Long = Duration.ofMinutes(1).toMillis()

    /**
     * Set the minimum wait time between calls
     * and the maximum time a value is cached
     */
    fun settings(
        minimumWaitMillis: Duration = Duration.ofSeconds(10),
        maxCacheTime: Duration = Duration.ofMinutes(1),
    ) {
        this.minimumWaitMillis = minimumWaitMillis.toMillis()
        this.maxCacheTime = maxCacheTime.toMillis()
    }

    private fun waitUntilValue(key: String?, millis: Long): Long {
        key ?: return millis

        synchronized(cacheMap) {
            // Clear old cache entries
            val currentTime = System.currentTimeMillis()
            cacheMap.removeIf { it.firstCall < currentTime - maxCacheTime }

            val bucket = cacheMap.find { it.key == key }
            val lastCall = bucket?.lastCall ?: currentTime
            val diffMillis = currentTime - lastCall

            return if (diffMillis < minimumWaitMillis) {
                val newWait = bucket?.waitUntil?.times(1.5) ?: millis.toDouble()
                cacheMap.add(WaitBucket(key, currentTime, bucket?.firstCall ?: currentTime, newWait.toLong()))
                newWait.toLong()
            } else {
                millis
            }
        }
    }

    fun <T> T.slowCall(
        key: String? = null,
        millis: Long = 1_500,
    ): T {
        val waitUntil = waitUntilValue(key, millis)
        Thread.sleep(waitUntil)
        return this
    }
}