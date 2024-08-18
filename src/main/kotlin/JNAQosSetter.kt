import com.sun.jna.Library
import com.sun.jna.Native
import kotlin.concurrent.thread

object JNAQosSetter: QosSetter {
    override fun setQosClass(qosClass: QosClass, relativePriority: Int): Int {
        val result = PThreadLibrary.INSTANCE.pthread_set_qos_class_self_np(QosClass.QOS_CLASS_BACKGROUND.raw, relativePriority)
        return result
    }
}

private interface PThreadLibrary : Library {
    fun pthread_set_qos_class_self_np(qosClass: Int, relativePriority: Int): Int

    companion object {
        val INSTANCE: PThreadLibrary = Native.load("pthread", PThreadLibrary::class.java)
    }
}


fun main(args: Array<String>) {
    repeat(20) {
        thread {
            JNAQosSetter.setQosClass(QosClass.QOS_CLASS_BACKGROUND)
            doHeavyWork()
        }
    }
}

