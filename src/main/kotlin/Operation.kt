/**
 * Created by Allen on 2018/2/28.
 */
class Main {
    fun test() {
        val run1 = kotlin.run {
            println("我是内部run1")
            "run1"
        }
        println("我是内部run1运行返回结果 $run1")

        val run2 = kotlin.run {
            println("我是内部run2")
            "run2"
        }
        println("我是内部run2运行结果 $run2")

    }


}

fun main(args: Array<String>) {
    val main =Main()
    main.test()
}