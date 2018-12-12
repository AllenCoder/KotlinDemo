/**
 * Created by Allen on 2018/4/9.
 */
fun main(args: Array<String>) {
    val map = mapOf(1 to "one", 2 to "two")

    println(map.mapValues { a ->
        val (key, value) = a
    })
}