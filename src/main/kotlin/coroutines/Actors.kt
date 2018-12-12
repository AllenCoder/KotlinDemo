package coroutines

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.runBlocking

/**
 * @author Allen
 * @date : 2018/12/12
 * 修改时间：
 * 修改备注：
 */
sealed class CounterMsg
object IncCounter : CounterMsg() // one-way message to increment counter
class GetCounter(val response: CompletableDeferred<Int>) : CounterMsg()

fun CoroutineScope.counterActor() = actor<CounterMsg> {
    var counter = 0 // actor state
    for (msg in channel) { // iterate over incoming messages
        when (msg) {
            is IncCounter -> counter++
            is GetCounter -> msg.response.complete(counter)
        }
    }
}

fun main(args: Array<String>) {
    runBlocking<Unit> {
        //sampleStart
        val counter = counterActor() // create the actor
        GlobalScope.massiveRun {
            counter.send(IncCounter)
        }
        // send a message to get a counter value from an actor
        val response = CompletableDeferred<Int>()
        counter.send(GetCounter(response))
        println("Counter = ${response.await()}")
        counter.close() // shutdown the actor
//sampleEnd
    }
}