package androidinput

import kotlin.reflect.KProperty

/**
 * @author Allen
 * @date : 2018/8/15
 * 修改时间：
 * 修改备注：
 */
fun main(args: Array<String>) {

//    val softType: SoftType
//    for (i in SoftType.values()) {
//        for (j in SoftType.values()) {
//            if (i.name != j.name) {
//                val str = " android:windowSoftInputMode=${i.name}|${j.name}    "
//                val value = i.typeValue or j.typeValue
//
//                println("Str = $str == value ${Integer.toHexString(value)}")
//            }
//
//        }
//    }

    val pairs = "age" to 7
    var user = User(mapOf(pairs, pairs ))
    var  pair =Pair<String,Int>("A",7)

//    println("name ${user.name}")
//    println("age ${user.age}")

    val parirs =Deleget()
    val a by parirs

}
class Deleget{
    operator fun getValue(nothing: Any?, property: KProperty<*>): Int {
        return 7
    }

    val a:Int =0
}
class User(val map: Map<String, Any?>) {
    val name: String by map
    val age: Int     by map
}

enum class SoftType(val typeValue: Int) {
    adjustNothing(0x30),
    adjustPan(0x20),
    adjustResize(0x10),
    adjustUnspecified(0x0),
    stateAlwaysHidden(0x3),
    stateAlwaysVisible(0x5),
    stateHidden(0x2),
    stateUnchanged(0x1),
    stateUnspecified(0x0),
    stateVisible(0x4);
}