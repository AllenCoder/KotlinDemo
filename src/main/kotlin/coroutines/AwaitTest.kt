package coroutines

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * @author Allen
 * @date : 2018/7/31
 * 修改时间：
 * 修改备注：
 */
fun main(args: Array<String>) {
    val list = ArrayList<Deferred<Any>>()
    runBlocking {
        val a = async {
            repeat(1000) {
                val b = async {
                    println("-------")
                    val a = async {
                        delay(1000)
                    }
                    println(a.await())
                    println("!!!!!!!!!!!!")
                }
                list.add(b)

            }
            list.forEach {
            }
        }
        a.await()
    }
}