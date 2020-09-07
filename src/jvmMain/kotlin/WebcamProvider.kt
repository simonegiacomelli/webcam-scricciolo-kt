import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.timer
import kotlin.concurrent.withLock

object WebcamProvider {
    private var w: Webcam? = null
    val lock: Lock = ReentrantLock()
    val value: Webcam
        get() {
            return lock.withLock {
                if (w == null)
                    w = newWebcam()
                w!!
            }
        }

    private fun newWebcam(): Webcam {
        val start = System.currentTimeMillis()
        log("Loading $imageDirectory")
        val webcam1 = Webcam(imageDirectory)
        val ela = System.currentTimeMillis() - start
        log("newWebcam took $ela millis")
        return webcam1
    }

    fun refreshOnceInAWhile() {
        timer("WebcamProvider", daemon = true, period = 1000 * 60) {
            val v = newWebcam()
            lock.withLock { w = v }
        }
    }
}