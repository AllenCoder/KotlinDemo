package emptytest

/**
 * Created by Allen on 2018/2/26.
 */
fun main(args: Array<String>) {
    var sum: Double = 0.0
    val t = System.currentTimeMillis()
    var i = 0.0
    while (i < 20000) {
        i += 0.1
        for (n in 0..19999) {
            sum = sum + i * n
        }
    }


    println(sum)
    println((System.currentTimeMillis() - t).toFloat())
    println("kotlin ")
}

