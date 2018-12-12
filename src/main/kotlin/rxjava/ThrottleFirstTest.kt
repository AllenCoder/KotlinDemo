package rxjava

import io.reactivex.Observable
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
  val a = Observable.just(1)  .throttleFirst(2, TimeUnit.SECONDS)
    while (true){

        repeat(100){
            a .subscribe {
                println("${it}")
            }
        }

       Thread.sleep(10000*10)
    }


}