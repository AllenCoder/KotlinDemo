package coroutines

/**
 * @author Allen
 * @date : 2018/7/18
 * 修改时间：
 * 修改备注：
 */

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

@Volatile
var counter = AtomicInteger()

fun main(args: Array<String>) = runBlocking<Unit> {
    val a = produce<String>(coroutineContext) {
        repeat(4) {
            send("Hello $it")
        }
    }
    val b = produce<String>(coroutineContext) {
        repeat(4) {
            send("World $it")
        }
    }
    repeat(8) {
        println(selectAorB(a, b))
    }
    coroutineContext.cancelChildren()

}
//fun fizz(context: CoroutineContext) = produce<String>(context) {
//    GlobalScope.launch {
//        while (true) {
//            delay(300)
////            send("Fizz")
//        }
//    }
//}

//fun buzz(context: CoroutineContext) = produce<String>(context) {
//    GlobalScope.launch {
//        while (true) {
//            delay(500)
////            send("Buzz!")
//        }
//    }
//}


//fun counterActor() = actor<CounterMsg> {
//    var counter = 0
////    for (msg in channel) {
////        when (msg) {
////            is IncCounter -> counter++
////            is GetCounter -> msg.response.complete(counter)
////        }
////    }
//}

suspend fun massiveRun(context: CoroutineContext, action: suspend () -> Unit) {
    val n = 1000 // number of coroutines to launch
    val k = 1000 // times an action is repeated by each coroutine
    val time = measureTimeMillis {
//        val jobs = List(n) {
//            launch(context) {
//                repeat(k) { action() }
//            }
//        }
//        jobs.forEach { it.join() }
    }
    println("Completed ${n * k} actions in $time ms")
}