import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

val x = Dispatchers.IO

// how great that i can pass a factory

val poool = ThreadPoolExecutor(
    0,
    64,
    60L,
    TimeUnit.SECONDS,
    SynchronousQueue(),
    object : ThreadFactory {
        var x = 0
        override fun newThread(r: Runnable): Thread {
            val fact = Executors.defaultThreadFactory()
            val newThread = fact.newThread {
                FFMQosSetter.setQosClass(QosClass.QOS_CLASS_BACKGROUND)
                r.run()
            }
            newThread.name = "eThread-${x++}"
            return newThread
        }
    }
)

val BackgroundQosCoroutineDispatcher = poool.asCoroutineDispatcher()

fun main() {
    runBlocking {
        repeat(20) {
            launch {
                withContext(BackgroundQosCoroutineDispatcher) {
                    doHeavyWork()
                }
            }
        }
    }
}

fun log(message: Any?) {
    println("[${Thread.currentThread().name}] $message")
}