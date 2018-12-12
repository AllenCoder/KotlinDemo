package html

import kotlinx.html.*
import kotlinx.html.consumers.PredicateResults
import kotlinx.html.consumers.filter
import kotlinx.html.dom.*

/**
 * @author Allen
 * @date : 2018/9/18
 * 修改时间：
 * 修改备注：
 */

val myDiv = document {
    create.div {
        p { +"text inside" }
    }
}

fun main(args: Array<String>) {
    myDiv
}
