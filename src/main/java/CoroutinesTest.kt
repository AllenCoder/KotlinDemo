/**
 * Created by Allen on 2018/1/17.
 */
import kotlinx.coroutines.delay
import kotlinx.coroutines.*

fun main(args: Array<String>) {
    val time =System.currentTimeMillis()
    println("Start")

    // Start a coroutine
//    GlobalScope.launch {
//        println("开始 launch "+(System.currentTimeMillis()-time))
//        delay(1000)
//        println("Hello  "+(System.currentTimeMillis()-time))
//    }
//    println("launch 下一步 "+(System.currentTimeMillis()-time))
//    runBlocking {
//        println("开始 runBlock "+(System.currentTimeMillis()-time))
//        delay(2000)
//        println("延时 runBlock "+(System.currentTimeMillis()-time))
//    }
//    println("runBlocking 下一步 "+(System.currentTimeMillis()-time))
//    Thread.sleep(3000) // wait for 2 seconds
//    println("Stop  "+(System.currentTimeMillis()-time))
}