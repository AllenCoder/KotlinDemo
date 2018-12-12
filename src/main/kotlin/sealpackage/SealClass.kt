package sealpackage

/**
 * Created by Allen on 2018/4/9.
 */

/*等同于枚举类*/
sealed class Expr {
    val a = 7
}

data class Const(val number: Double) : Expr()

data class Sum(val e1: Expr, val e2: Expr) : Expr()

object NotANumber : Expr()

fun eval(expr: Expr): Double = when (expr) {
    is Const -> expr.number
    is Sum -> eval(expr.e1) + eval(expr.e2)
    NotANumber -> Double.NaN
}

val e = eval(Sum(Const(1.0), Const(2.0)))

data class Person(val name: String, val age: Int) {
    val isAdult get() = age >= 20
}

fun main(args: Array<String>) {
    print("e is $e")

    val oneMillion = 1_000_000
    val hexBytes = 0xFF_EC_DE_5E

    val result = "aaaa"
    result.takeUnless(String::isBlank)

}