package coroutines

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

/**
 * @author Allen
 * @date : 2018/12/12
 * 修改时间：
 * 修改备注：
 */

fun main(args: Array<String>) {
    val mtContext = newFixedThreadPoolContext(1, "mtPool") // explicitly define context with two threads
    var counter = AtomicInteger()
    runBlocking {
        CoroutineScope(mtContext).massiveRun {
            counter.incrementAndGet()
        }
        println("Counter = $counter")
    }
}
suspend fun CoroutineScope.massiveRun(action: suspend () -> Unit) {
    val n = 100  // number of coroutines to launch
    val k = 1000 // times an action is repeated by each coroutine
    val time = measureTimeMillis {
        val jobs = List(n) {
            launch {
                repeat(k) { action() }
            }
        }
        jobs.forEach { it.join() }
    }
    println("Completed ${n * k} actions in $time ms")
}