package coroutines

import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select

/**
 * @author Allen
 * @date : 2018/12/12
 * 修改时间：
 * 修改备注：
 */
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

fun main(args: Array<String>) {
    runBlocking<Unit> {
        //sampleStart
        val a = produce<String> {
            repeat(4) { send("Hello $it") }
        }
        val b = produce<String> {
            repeat(4) { send("World $it") }
        }
        repeat(8) {
            // print first eight results
            println(selectAorB(a, b))
        }
        coroutineContext.cancelChildren()
//sampleEnd
    }
}