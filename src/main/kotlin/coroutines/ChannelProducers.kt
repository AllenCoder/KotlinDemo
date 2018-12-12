package coroutines

/**
 * @author Allen
 * @date : 2018/12/12
 * 修改时间：
 * 修改备注：
 */
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

fun CoroutineScope.produceSquares(): ReceiveChannel<Int> {
    return produce {
        for (x in 1..5) {
            send(x * x)
        }
    }
}

fun main() = runBlocking {
    //sampleStart
    val squares = produceSquares()
    squares.consumeEach { println(it) }
    println("Done!")
//sampleEnd
}