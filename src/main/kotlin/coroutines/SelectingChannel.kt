package coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.selects.select

/**
 * @author Allen
 * @date : 2018/12/12
 * 修改时间：
 * 修改备注：
 */
@ExperimentalCoroutinesApi
fun CoroutineScope.fizz() = produce {
    while (true) { // sends "Fizz" every 300 ms
        delay(300)
        send("Fizz")
    }
}

fun CoroutineScope.buzz() = produce {
    while (true) { // sends "Buzz!" every 500 ms
        delay(500)
        send("Buzz!")
    }
}

suspend fun selectFizzBuzz(fizz: ReceiveChannel<String>, buzz: ReceiveChannel<String>) {
    select<Unit> {
        // <Unit> means that this select expression does not produce any result
        fizz.onReceive { value ->
            // this is the first select clause
            println("fizz -> '$value'")
        }
        buzz.onReceive { value ->
            // this is the second select clause
            println("buzz -> '$value'")
        }
    }
}

fun main(args: Array<String>) {
    runBlocking {
        val fizz = fizz()
        val buzz = buzz()
        val context = newFixedThreadPoolContext(2, "my")
        /*
        * Exception in thread "my-1" kotlinx.coroutines.channels.ClosedReceiveChannelException: Channel was closed
                at kotlinx.coroutines.channels.Closed.getReceiveException(AbstractChannel.kt:1070)
                at kotlinx.coroutines.channels.AbstractChannel$ReceiveSelect.resumeReceiveClosed(AbstractChannel.kt:980)
                at kotlinx.coroutines.channels.AbstractSendChannel.helpClose(AbstractChannel.kt:320)
                at kotlinx.coroutines.channels.AbstractSendChannel.close(AbstractChannel.kt:256)
                at kotlinx.coroutines.channels.AbstractChannel.cancel(AbstractChannel.kt:665)
                at kotlinx.coroutines.channels.ChannelCoroutine.cancel(ChannelCoroutine.kt:26)
                at kotlinx.coroutines.channels.ChannelCoroutine.cancel(ChannelCoroutine.kt:20)
                at kotlinx.coroutines.JobKt__JobKt.cancelChildren(Job.kt:539)
                at kotlinx.coroutines.JobKt.cancelChildren(Unknown Source)
        *
        * */
        CoroutineScope(context).massiveRun {
            repeat(100) {
                if (it == 1) {
                    println("取消成功")
                    coroutineContext.cancelChildren() // cancel fizz & buzz coroutines
                }
                println("massiveRun --$it")
                selectFizzBuzz(fizz, buzz)
            }
        }

    }

}