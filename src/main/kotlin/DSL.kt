import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.util.*
import jdk.nashorn.internal.objects.NativeDate.getTime
import java.util.Calendar


/**
 * Created by Allen on 2018/3/5.
 */
val Int.days: Period
    get() = Period.ofDays(this)

val Period.ago: LocalDate
    get() = LocalDate.now() - this


val Date.later: Date
    get() = Date()

//获取某个日期的后一天
fun String.getSpecifiedDayAfter(specifiedDay: String): Date {
    var calendar = Calendar.getInstance()
    //输出的日期格式
    var sdf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    //自定义过来的String格式的日期
    var date: Date = SimpleDateFormat("yyyy-MM-dd").parse(specifiedDay)
    calendar.time = date
    var day = calendar.get(Calendar.DATE)
    calendar.set(Calendar.DATE, day + 6)
    return calendar.time
}

fun getDateNight(time: String): Date {
    val parse = SimpleDateFormat("yyyy-MM-dd").parse(time)
    val calendar = Calendar.getInstance()
    calendar.time = parse
//    calendar.set(Calendar.DAY_OF_MONTH, 6)
    calendar.add(Calendar.DATE,6)
    calendar.set(Calendar.HOUR_OF_DAY, 23)

    calendar.set(Calendar.MINUTE, 59)

    calendar.set(Calendar.SECOND, 59)

    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.time
}

fun isSameWeek(date: Date,  dateZero: Date): Boolean {

    val week1 = Calendar.getInstance()
    week1.time = date
    val week2 = Calendar.getInstance()
    week2.time = dateZero

    return  week1.get(Calendar.WEEK_OF_YEAR) == week2.get(Calendar.WEEK_OF_YEAR)
}
fun main(args: Array<String>) {

    val ago = 1.days
    println(ago)
    val yesterday = 1.days.ago
    println("2018-03-20".getSpecifiedDayAfter("2016-03-19"))
    println(getDateNight("2018-03-19"))
    println(isSameWeek(getDateNight("2016-12-26"),getDateNight("2017-01-01")))
//    for (i in 0..999) {
//        println(Random().nextInt(5))
//    }

}