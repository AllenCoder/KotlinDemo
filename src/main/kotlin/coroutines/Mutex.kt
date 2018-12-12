package coroutines

/**
 * @author Allen
 * @date : 2018/12/12
 * 修改时间：
 * 修改备注：
 */
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


val mutex = Mutex()

fun main() = runBlocking<Unit> {
    //sampleStart
    val mtContext = newFixedThreadPoolContext(1, "mtPool")
//    互斥锁时，多线程不一定比单线程快
    var counter=0
    CoroutineScope(mtContext).massiveRun {
        mutex.withLock {
            counter++
        }
    }
    println("Counter = $counter")
//sampleEnd
}