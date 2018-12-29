package leetcode

import java.util.*

/**
 * @author Allen
 * @date : 2018/12/29
 * 修改时间：
 * 修改备注：
 */
class Solution {
    fun isValid(s: String): Boolean {
        val stackResult = Stack<Char>()

        val resultMap = mapOf(")" to "("
                , "]" to "["
                , "}" to "{")
        s.forEach {
            if (stackResult.isNotEmpty()) {
                val topElement = stackResult.peek()
                if (resultMap[it.toString()] ==topElement.toString()){
                    stackResult.pop()
                }else{
                    stackResult.push(it)
                }
            }else{
                stackResult.push(it)
            }
        }

        return stackResult.isEmpty()
    }
}

fun main(args: Array<String>) {
    println(Solution().isValid("()[]"))
    println(Solution().isValid("()[]{}"))
    println(Solution().isValid("(]"))
    println(Solution().isValid("([)]"))
    println(Solution().isValid("{[]}"))
}