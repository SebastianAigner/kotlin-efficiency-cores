import kotlin.math.sqrt
import kotlin.random.Random

fun doHeavyWork() {
    log("I'm doing heavy work!")
    var x = 0f
    while (true) {
        x += sqrt(Random.nextInt().toDouble()).toFloat()
        if (x < 0) return
    }
}