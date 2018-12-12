package database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.exposedLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.util.*

/**
 * @author Allen
 * @date : 2018/8/14
 * 修改时间：
 * 修改备注：
 */
fun main(args: Array<String>) {

//    Database.connect("jdbc:oracle:thin:@192.168.1.215:1521:lottery", driver = "oracle.jdbc.OracleDriver")
//    TestDB.ORACLE.connect()
}
enum class TestDB(val connection: String, val driver: String, val user: String = "walk", val pass: String = "walk",
                  val beforeConnection: () -> Unit = {}, val afterTestFinished: () -> Unit = {}, var db: Database? = null) {
    SQLITE("jdbc:sqlite:file:test?mode=memory&cache=shared", "org.sqlite.JDBC"),
    ORACLE(driver = "oracle.jdbc.OracleDriver", user = "walk", pass = "walk",
            connection = ("jdbc:oracle:thin:@192.168.1.215:1521:lottery"),
            beforeConnection = {
                Locale.setDefault(Locale.ENGLISH)
                val tmp = Database.connect(ORACLE.connection, user = "walk", password = "walk", driver = ORACLE.driver)
                transaction(java.sql.Connection.TRANSACTION_READ_COMMITTED, 1, tmp) {
//                    try {
//                        exec("DROP USER ExposedTest CASCADE")
//                    } catch (e: Exception) { // ignore
//                        exposedLogger.warn("Exception on deleting ExposedTest user", e)
//                    }
//
//                    exec("CREATE USER ExposedTest IDENTIFIED BY 12345 DEFAULT TABLESPACE system QUOTA UNLIMITED ON system")
//                    exec("grant all privileges to ExposedTest IDENTIFIED BY 12345")
//                    exec("grant dba to ExposedTest IDENTIFIED BY 12345")
                }
                Unit
            }),
    SQLSERVER("jdbc:sqlserver://${System.getProperty("exposed.test.sqlserver.host", "192.168.99.100")}" +
            ":${System.getProperty("exposed.test.sqlserver.port", "32781")}",
            "com.microsoft.sqlserver.jdbc.SQLServerDriver", "SA", "yourStrong(!)Password");

    fun connect() = Database.connect(connection, user = user, password = pass, driver = driver)

    companion object {
        fun enabledInTests(): List<TestDB> {
            val embeddedTests = (TestDB.values().toList() - ORACLE - SQLSERVER).joinToString()
            val concreteDialects = System.getProperty("exposed.test.dialects", embeddedTests).let {
                if (it == "") emptyList()
                else it.split(',').map { it.trim().toUpperCase() }
            }
            return values().filter { concreteDialects.isEmpty() || it.name in concreteDialects }
        }
    }
}
