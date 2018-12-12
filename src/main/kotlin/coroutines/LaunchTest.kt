package coroutines

import kotlinx.coroutines.*

/**
 * @author Allen
 * @date : 2018/11/12
 * 修改时间：
 * 修改备注：
 */
fun main(args: Array<String>) {

    runBlocking {
        CoroutineScope(Dispatchers.IO).launch {
            println("Start coroutine")
            asyTest().await()
            Thread {
                println("Start new thread")
                 launch {
                    //                    Why is this code not working?
                    println("coroutine can't running in new thread")
                }
            }.start()
            launch {
                //                    this   working
                println("coroutine running in current thread")
            }
        }
        delay(3000)
    }

}

fun asyTest(): Deferred<Unit> {
    return GlobalScope.async {
        delay(1000)
        println("coroutine runing async ")
    }
}