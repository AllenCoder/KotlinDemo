package coroutines

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * @author Allen
 * @date : 2018/12/12
 * 修改时间：
 * 修改备注：
 */
fun main(args: Array<String>) {
//    GlobalScope.launch {
//        //在后台启动新的协程并继续
//        delay(1000L) //非阻塞延迟1秒（默认时间单位为ms）
//        println("World!") //延迟后打印
//    }
//    println("Hello,") //主线程继续，而协程延迟
//    Thread.sleep(2000L)//阻塞主线程2秒以保持JVM活动

    GlobalScope.launch {
        // 运行一个新的协程在后台任务
        delay(1000L)
        println("World!")
    }
    println("Hello,") // 主线程立即执行
    runBlocking {
        // 主线程延时2000ms
        delay(2000L)  //
    }
}