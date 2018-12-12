import kotlinx.coroutines.*

/**
 * Created by Allen on 2018/1/17.
 */
fun main(args: Array<String>) {
    val deferred = (1..1_000_000).map { n ->
        GlobalScope.async {
            workload(n)
        }
    }
    runBlocking {

        async {
            var s ="ddd"

            s.toRegex()::matches
        }
        val sum = deferred.sumBy { it.await() }
        println("Sum: $sum")
    }


}
suspend fun workload(n: Int): Int {
    println("执行workload")
    delay(1000)
    return n
}