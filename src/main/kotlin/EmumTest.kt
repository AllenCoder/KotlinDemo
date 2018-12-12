/**
 * Created by Allen on 2018/7/4.
 */

enum class StateLayout(var value: Int) {
    /*初始状态，不显示任何View*/
    INIT(0),
    /*请求数据*/
    LOADING(1),
    /*空数据*/
    EMPTY(2),
    /*网络异常*/
    NET_ERROR(3),
    /*正常显示*/
    SUCCESS(4)
}
fun main(args: Array<String>) {
    println(StateLayout.values( ))
}