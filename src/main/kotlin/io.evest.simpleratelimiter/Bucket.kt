import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

data class Bucket(
    var calls: Int,
    val firstCall: Long,
)

object BucketHandler {
    private val bucketMap = ConcurrentHashMap<String, Bucket>()

    fun count(key: String, maxCalls: Int, per: Duration): Boolean = synchronized(bucketMap) {
        val currentTime = System.currentTimeMillis()
        val timeAlive = per.toMillis()
        val currentBucket = bucketMap[key] ?: Bucket(0, currentTime)

        // within the time frame
        if (currentBucket.firstCall + timeAlive < currentTime) {
            // increment the number of calls
            currentBucket.calls += 1
        } else {
            // reset the bucket
            bucketMap.remove(key)
        }

        val shouldTimeout = when {
            currentBucket.calls > maxCalls -> {
                bucketMap[key] = Bucket(0, currentTime)
                true
            }

            else -> {
                bucketMap[key] = currentBucket
                false
            }
        }


        return@synchronized shouldTimeout
    }
}