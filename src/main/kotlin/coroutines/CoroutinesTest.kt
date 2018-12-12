package coroutines

import com.sun.management.jmx.Trace.send
import io.reactivex.internal.operators.completable.CompletableDefer
import javafx.application.Application.launch
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.channels.*
import kotlin.coroutines.CoroutineContext

/**
 * @author Allen
 * @date : 2018/7/18
 * 修改时间：
 * 修改备注：
 */

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.selects.select
import java.util.concurrent.atomic.AtomicInteger
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
suspend fun selectAorB(a: ReceiveChannel<String>, b: ReceiveChannel<String>): String =
        select<String> {
            a.onReceiveOrNull { value ->
                if (value == null)
                    "Channel 'a' is closed"
                else
                    "a -> '$value'"
            }
            b.onReceiveOrNull { value ->
                if (value == null)
                    "Channel 'b' is closed"
                else
                    "b -> '$value'"
            }
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

suspend fun selectFizzBuzz(fizz: ReceiveChannel<String>, buzz: ReceiveChannel<String>) {
    select<Unit> {
        fizz.onReceive { value ->
            println("fizz ->'$value")
        }
        buzz.onReceive { value ->
            println("buzz->$value")
        }
    }
}

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

sealed class CounterMsg

object IncCounter : CounterMsg()

class GetCounter(val response: CompletableDeferred<Int>) : CounterMsg()