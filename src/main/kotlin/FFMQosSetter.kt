import java.lang.foreign.*
import kotlin.concurrent.thread

fun main() {
    repeat(20) {
        thread {
            FFMQosSetter.setQosClass(QosClass.QOS_CLASS_BACKGROUND)
            doHeavyWork()
            return@thread
        }
    }
}

object FFMQosSetter : QosSetter {
    override fun setQosClass(qosClass: QosClass, relativePriority: Int): Int {
        Arena.ofConfined().use { arena ->
            val linker = Linker.nativeLinker()

            val pthreadLookup = linker.defaultLookup() //SymbolLookup.libraryLookup("pthread", arena)

            // pthread_set_qos_class_self_np(qosClass: Int, relativePriority: Int): Int
            val setQosClassSelfNpSegment = pthreadLookup.find("pthread_set_qos_class_self_np").orElseThrow()
            val pthread_set_qos_class_self_np = linker.downcallHandle(
                setQosClassSelfNpSegment,
                FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
                )
            )

            val result = pthread_set_qos_class_self_np.invokeExact(qosClass.raw, relativePriority) as Int

            if (result != 0) {
                System.err.println("Failed to set QoS class, error code: $result")
            }
            println("QoS class set successfully.")
            return result
        }
    }
}