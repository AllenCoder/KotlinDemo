import javafx.application.Application.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * Created by Allen on 2018/1/17.
 */
fun main(args: Array<String>) {
    //    非常耗时，电脑被卡死 花费时间101006ms
//    835858431
//    testThreadTime()

// 非常快
//    开始时间 1516176664835
//    花费时间746
//    1784293664
    testCoroutinesTime()
}

/**
 * 开始时间 1516176664835
花费时间746
1784293664
 */
private fun testCoroutinesTime() {
    val time = System.currentTimeMillis()
    println("开始时间 " + time)
    val c = AtomicInteger()

    for (i in 1..1_000_000)
        GlobalScope.launch {
            c.addAndGet(i)
        }
    println("花费时间" + (System.currentTimeMillis() - time))
    println(c.get())
}

private fun testThreadTime() {
    val time = System.currentTimeMillis()
    println("开始时间 " + time)
    val c = AtomicInteger()

    for (i in 1..1_000_000)
        thread(start = true) {
            c.addAndGet(i)
        }
    println("花费时间" + (System.currentTimeMillis() - time))
    println(c.get())
}