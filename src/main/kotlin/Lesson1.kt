/**
 * Created by Allen on 2018/4/4.
 */


fun main(args: Array<String>) {
    val a = 12
    println("a ${if (a > 6) "sss" else "bbb"} ")
    val set = setOf(1, 2, 3)
    val list = listOf(1, 2, 3)
    val map = mapOf(1 to "one", 2 to "two")
    print(map.get(1))

    var button = Button()
    button.showOff()


    var tupple = Triple<String, String, Int>("A", "b", 1000)
    tupple?.let {
        if (it.first.equals("B")) {
            return
        }
        print("测试")
    }
}

class DelegatingCollection<T>(val innerList: Collection<T> = ArrayList())
    : Collection<T> by innerList

class CountingSet<T>(val innerList: MutableCollection<T> = HashSet<T>())
    : MutableCollection<T> by innerList {
    override fun add(element: T): Boolean {
        return true
    }
}

interface Clickable {
    fun click()
    fun showOff() = println("i'm Clickable!") //默认实现的方法
}

class Button : Clickable {
    override fun click() {
        print("click")
    }
};