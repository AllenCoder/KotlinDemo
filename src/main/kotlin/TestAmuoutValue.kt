import java.lang.Double
import java.math.BigDecimal

/**
 * @author Allen
 * @date : 2018/12/1
 * 修改时间：
 * 修改备注：
 */
fun main(args: Array<String>) {
    formatValue(getRoundUp(100))
    formatValue(getRoundUp(101))
    formatValue(getRoundUp(110))
    formatValue(getRoundUp(2500))
    formatValue(getRoundUp(2510))
    formatValue(getRoundUp(2510))
}

private fun formatValue(value: String): String {
    if (value.length < 3) {
        return value
    }
    return when {
        value.endsWith(".00") -> value.dropLast(3)
        value.endsWith("0") -> value.dropLast(1)
        else -> value
    }
}

fun getRoundUp(value: Int, digit: Int = 2): String {
    val result = BigDecimal(value.toDouble() / 100f)
    return result.setScale(digit, BigDecimal.ROUND_HALF_UP).toString()
}