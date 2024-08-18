/***
 * Refer to the [Energy Efficiency Guide for Mac Apps](https://developer.apple.com/library/archive/documentation/Performance/Conceptual/power_efficiency_guidelines_osx/PrioritizeWorkAtTheTaskLevel.html#//apple_ref/doc/uid/TP40013929-CH35-SW5)
 */
enum class QosClass(val raw: Int) {
    QOS_CLASS_USER_INTERACTIVE(0x21),
    QOS_CLASS_USER_INITIATED(0x19),
    QOS_CLASS_DEFAULT(0x15),
    QOS_CLASS_UTILITY(0x11),
    QOS_CLASS_BACKGROUND(0x09),
    QOS_CLASS_UNSPECIFIED(0x00),
}

interface QosSetter {
    fun setQosClass(qosClass: QosClass, relativePriority: Int = 0): Int
}

// This function won't mix well with coroutines, because a system thread can execute more than one coroutine at a time.
inline fun QosSetter.withQos(qos: QosClass, block: () -> Unit) {
    try {
        setQosClass(qos, 0)
        block()
    } finally {
        this.setQosClass(QosClass.QOS_CLASS_DEFAULT, 0)
    }
}