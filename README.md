![](https://img.hacpai.com/bing/20180921.jpg?imageView2/1/w/960/h/520/interlace/1/q/100) 
---
 原文翻译  [https://github.com/Kotlin/kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) 
 
---
## Kotlin协程1.0.1版本 更新
## 更新时间 2018/12/12 22:50



### 你的第一个协程

```
fun main(args: Array<String>) {
    GlobalScope.launch {
        //在后台启动新的协程并继续
        delay(1000L) //非阻塞延迟1秒（默认时间单位为ms）
        println("World!") //延迟后打印
    }
    println("Hello,") //主线程继续，而协程延迟
    Thread.sleep(2000L)//阻塞主线程2秒以保持JVM活动
}
```
输出结果

```
Hello,
World!

```

从本质上讲，协程是轻量级的线程。它们是与发布 协程构建器一起启动的。您可以实现相同的结果替换  **launch { ... }** 用 **thread { ... }** ，并 **delay(...)** 用  **Thread.sleep(...)**  。尝试一下。

如果以替换launch为开头thread，则编译器会产生以下错误：

```
Error: Kotlin: Suspend functions are only allowed to be called from a coroutine or another suspend function
```

这是因为delay是一个特殊的挂起函数，它不会阻塞一个线程，但会挂起 协同程序，它只能从协程中使用。

### 桥接阻塞和非阻塞世界

第一个示例在同一代码中混合非阻塞 delay(...)和阻塞 Thread.sleep(...)。很容易迷失哪一个阻塞而另一个阻塞。让我们明确说明使用runBlocking coroutine builder进行阻塞：

```
fun main(args: Array<String>) { 
   GlobalScope.launch {
           // 运行一个新的协程在后台任务
           delay(1000L)
           println("World!")
       }
       println("Hello,") // 主线程立即执行
       runBlocking {
           // 主线程延时2000ms
           delay(2000L)  //  
       }
}
```

结果是相同的，但此代码仅使用非阻塞延迟。主线程，调用runBlocking，块，直到协程内runBlocking完成。

这个例子也可以用更常用的方式重写，runBlocking用来包装main函数的执行：
```
import kotlinx.coroutines.*

fun main() = runBlocking<Unit> { // start main coroutine
    GlobalScope.launch { // launch new coroutine in background and continue
        delay(1000L)
        println("World!")
    }
    println("Hello,") // main coroutine continues here immediately
    delay(2000L)      // delaying for 2 seconds to keep JVM alive
}
```

### 等候工作
当另一个协程正在工作时延迟等待一段时间并不是一个好的方法。我们更希望明确等待（以非阻塞方式），直到我们启动的后台作业完成：

```
fun main(args: Array<String>) = runBlocking<Unit> {
    val job = GlobalScope.launch { // launch new coroutine and keep a reference to its Job
        delay(1000L)
        println("World!")
    }
    println("Hello,")
    job.join() // wait until child coroutine completes
}
```

### 结构化并发

1303/5000
对于协同程序的实际使用仍有一些需要。当我们使用GlobalScope.launch时，我们创建了一个顶级协程。
尽管它很轻，但它在运行时仍会消耗一些内存资源。如果我们忘记保留对新启动的协程的引用，它仍会运行。
如果协同程序中的代码挂起（例如，我们错误地延迟了太长时间），如果我们启动了太多的协程并且内存不足会怎么样？
必须手动保持对所有已启动的协同程序的引用并加入它们是容易出错的。有一个更好的解决方案。
我们可以在代码中使用结构化并发。就像我们通常使用线程（线程总是全局的）一样，
我们可以在我们正在执行的操作的特定范围内启动协同程序，而不是在GlobalScope中启动协同程序。
在我们的示例中，我们使用runBlocking coroutine builder将main函数转换为协程。每个协程构建器（包括runBlocking）
都将CoroutineScope的实例添加到其代码块的范围内。我们可以在此范围内启动协程，
而无需显式连接它们，因为在其范围内启动的所有协程完成之前，外部协程（在我们的示例中为runBlocking）不会完成。因此，我们可以使我们的示例更简单：

```
import kotlinx.coroutines.*

fun main() = runBlocking { // this: CoroutineScope
    launch { // 运行一个协程在runBlocking作用域
        delay(1000L)
        println("World!")
    }
    println("Hello,")
}
```

### 作用域构建器

除了由不同构建器提供的协同作用域之外，还可以使用coroutineScope构建器声明自己的作用域。
它会创建新的协程范围，并且在所有已启动的子项完成之前不会完成。
runBlocking和coroutineScope之间的主要区别在于后者在等待所有子进程完成时不会阻塞当前线程。

```
fun main() = runBlocking { // this: CoroutineScope
    launch {
        delay(200L)
        println("Task from runBlocking")
    }

    coroutineScope { // Creates a new coroutine scope
        launch {
            delay(500L)
            println("Task from nested launch")
        }

        delay(100L)
        println("Task from coroutine scope") // This line will be printed before nested launch
    }

    println("Coroutine scope is over") // 直到所有任务执行完成打印
}
```


### 提取函数重构
让我们将代码块提取launch { ... }到一个单独的函数中。
当您对此代码执行“提取功能”重构时，您将获得带有suspend修饰符的新功能。
这是你的第一个暂停功能。挂起函数可以在协程内部使用，就像常规函数一样，
但它们的附加功能是它们可以反过来使用其他挂起函数（如delay本示例中所示）来暂停协程的执行。



```
fun main(args: Array<String>) = runBlocking<Unit> {
    val job = launch { doWorld() }
    println("Hello,")
    job.join()
}

// this is your first suspending function
suspend fun doWorld() {
    delay(1000L)
    println("World!")
}
```

但是，如果提取的函数包含在当前作用域上调用的协程构建器，该怎么办？
在这种情况下，提取函数上的suspend修饰符是不够的。 在CoroutineScope上
制作doWorld扩展方法是其中一种解决方案，但它可能并不总是适用，
因为它不会使API更清晰。 惯用解决方案是将显式CoroutineScope作为包含目标函数的类中的字段，
或者在外部类实现CoroutineScope时隐式。 作为最后的手段，
可以使用CoroutineScope（coroutineContext），但是这种方法在结构上是不安全的，
因为您不再能够控制此方法的执行范围。 只有私有API才能使用此构建器。

### 协同程序足够轻量级

```
fun main(args: Array<String>) = runBlocking<Unit> {
    val jobs = List(100_000) {
        // launch a lot of coroutines and list their jobs
        launch {
            delay(1000L)
            print(".")
        }
    }
    jobs.forEach { it.join() } // wait for all jobs to complete
}
```
它启动了100K协同程序，一秒钟之后，每个协同程序都打印出一个点。
现在，尝试使用线程。会发生什么？（很可能你的代码会产生某种内存不足的错误）

### 协同程序就像守护程序线程
下面的代码启动一个长时间运行的协同程序，每秒打印“我正在睡觉”两次，然后在一段延迟后从main函数返回：


```
fun main(args: Array<String>) = runBlocking{
    GlobalScope.launch {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
    }
    delay(1300L) // just quit after delay
}
```
您可以运行并看到它打印三行并终止：

```
I'm sleeping 0 ...
I'm sleeping 1 ...
I'm sleeping 2 ...
```

活动协同程序不会使进程保持活动状态。它们就像守护程序线程。


### 取消和超时

在小应用程序中，从“main”方法返回可能听起来像是一个好主意，以便隐式终止所有协同程序。
在较大的长期运行的应用程序中，您需要更精细的控制。在推出函数返回一个作业，可用于取消运行协程：


```
fun main(args: Array<String>) = runBlocking<Unit> {
    val job = launch {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
    }
    delay(1300L) // delay a bit
    println("main: I'm tired of waiting!")
    job.cancel() // cancels the job
    job.join() // waits for job's completion 
    println("main: Now I can quit.")
}
```
输出如下

```
I'm sleeping 0 ...
I'm sleeping 1 ...
I'm sleeping 2 ...
main: I'm tired of waiting!
main: Now I can quit.
```

主调用后job.cancel，我们看不到其他协同程序的任何输出，因为它已被取消。
还有一个Job扩展函数cancelAndJoin ，它结合了取消和连接调用。

### 取消是协同的

协程取消是协同的。协程代码必须合作才能取消。所有挂起函数kotlinx.coroutines都是可取消的。
他们检查coroutine的取消并在取消时抛出CancellationException。但是，如果协程正在计算中并且未检查取消，则无法取消它，如下例所示：

```
fun main(args: Array<String>) = runBlocking<Unit> {
    val startTime = System.currentTimeMillis()
    val job = launch {
        var nextPrintTime = startTime
        var i = 0
        while (i < 5) { // computation loop, just wastes CPU
            // print a message twice a second
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("I'm sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L) // delay a bit
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // cancels the job and waits for its completion
    println("main: Now I can quit.")
}
```
运行它以查看它继续打印“我正在睡觉”，即使在取消之后，直到作业在五次迭代后自行完成。
输出结果

```
I'm sleep 0
I'm sleep 1
I'm sleep 2
main I;m tried of waiting
I'm sleep 3
I'm sleep 4
main Now I can quit

```

### 使计算代码可取消
有两种方法可以使计算代码可以取消。第一个是定期调用检查取消的挂起功能。
有一个收益率的功能是实现这一目的的好选择。另一个是明确检查取消状态。让我们尝试后一种方法。

```
fun main(args: Array<String>) = runBlocking<Unit> {
    val startTime = System.currentTimeMillis()
    val job = launch {
        var nextPrintTime = startTime
        var i = 0
        while (isActive) { // cancellable computation loop
            // print a message twice a second
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("I'm sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L) // delay a bit
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // cancels the job and waits for its completion
    println("main: Now I can quit.")
}
```


如您所见，现在此循环已取消。isActive是通过CoroutineScope对象在协同程序代码中可用的属性。

### 最后关闭资源

可取消的挂起函数会在取消时抛出CancellationException，这可以通过所有常规方式处理。例如，当取消协程时，try {...} finally {...}表达式和Kotlin use函数通常会执行其终结操作：

```
fun main(args: Array<String>) = runBlocking<Unit> {
   val job = launch {
        try {
            repeat(1000) { i ->
                println("I'm sleeping $i ...")
                delay(500L)
            }
        } finally {
            println("I'm running finally")
        }
    }
    delay(1300L) // delay a bit
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // cancels the job and waits for its completion
    println("main: Now I can quit.")
	
```

无论加入和cancelAndJoin等待所有完成动作来完成的，所以上面的例子产生下面的输出：

```
I'm sleeping 0 ...
I'm sleeping 1 ...
I'm sleeping 2 ...
main: I'm tired of waiting!
I'm running finally
main: Now I can quit.
```

### 运行不可取消的块
finally在前一个示例的块中尝试使用挂起函数将导致CancellationException，因为运行此代码的协程将 被取消。通常，这不是问题，因为所有表现良好的关闭操作（关闭文件，取消作业或关闭任何类型的通信通道）通常都是非阻塞的，并且不涉及任何挂起功能。但是，在极少数情况下，当您需要挂起已取消的协同程序时，可以withContext(NonCancellable) {...}使用withContext函数和NonCancellable上下文包装相应的代码， 如下例所示：

```
fun main(args: Array<String>) = runBlocking<Unit> {
    val job = launch {
        try {
            repeat(1000) { i ->
                println("I'm sleeping $i ...")
                delay(500L)
            }
        } finally {
            withContext(NonCancellable) {
                println("I'm running finally")
                delay(1000L)
                println("And I've just delayed for 1 sec because I'm non-cancellable")
            }
        }
    }
    delay(1300L) // delay a bit
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // cancels the job and waits for its completion
    println("main: Now I can quit.")
}
```

超时退出
在实践中取消协程执行的最明显的原因是因为它的执行时间超过了一些超时。虽然您可以手动跟踪对相应作业的引用并启动单独的协同程序以在延迟后取消跟踪的协程，
但是有一个准备好使用withTimeout函数执行此操作。请看以下示例：

```
fun main(args: Array<String>) = runBlocking<Unit> {
    withTimeout(1300L) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
    }
}
```

```
I'm sleeping 0 ...
I'm sleeping 1 ...
I'm sleeping 2 ...
Exception in thread "main" kotlinx.coroutines.experimental.TimeoutCancellationException: Timed out waiting for 1300 MILLISECONDS
```



该TimeoutCancellationException由抛出withTimeout是的子类CancellationException。
我们之前没有看到它的堆栈跟踪打印在控制台上。这是因为在取消的协程中CancellationException被认为是协程完成的正常原因。
但是，在这个例子中我们withTimeout在main函数内部使用了。

因为取消只是一个例外，所有资源都将以通常的方式关闭。
您可以在超时包裹代码try {...} catch (e: TimeoutCancellationException) {...}块，
如果你需要专门做一些额外的行动在任何类型的超时或使用withTimeoutOrNull功能类似于withTimeout，
但返回null的超时，而不是抛出一个异常：


```

fun main(args: Array<String>) = runBlocking<Unit> {
    val result = withTimeoutOrNull(1300L) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
        "Done" // will get cancelled before it produces this result
    }
    println("Result is $result")
}
```
运行此代码时不再有异常：

```
I'm sleeping 0 ...
I'm sleeping 1 ...
I'm sleeping 2 ...
Result is null

```

### 暂停功能指南

#### 默认顺序
假设我们在其他地方定义了两个挂起函数，它们可以像某种远程服务调用或计算一样有用。
我们只是假装它们很有用，但实际上每个只是为了这个例子的目的而延迟一秒：

```
suspend fun doSomethingUsefulOne(): Int {
    delay(1000L) // pretend we are doing something useful here
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1000L) // pretend we are doing something useful here, too
    return 29
}
```

如果需要按顺序调用它们，我们该怎么做- 首先doSomethingUsefulOne 然后 doSomethingUsefulTwo计算结果的总和？
实际上，如果我们使用第一个函数的结果来决定是否需要调用第二个函数或决定如何调用它，我们就会这样做。

我们只使用正常的顺序调用，因为协程中的代码与常规代码中的代码一样，默认是顺序的。
以下示例通过测量执行两个挂起函数所需的总时间来演示它：



```
fun main(args: Array<String>) = runBlocking<Unit> {
    val time = measureTimeMillis {
        val one = doSomethingUsefulOne()
        val two = doSomethingUsefulTwo()
        println("The answer is ${one + two}")
    }
    println("Completed in $time ms")
}
```

它产生这样的东西：

```
The answer is 42
Completed in 2017 ms
```

### 并发使用异步

如果在调用doSomethingUsefulOne和之间没有依赖关系，doSomethingUsefulTwo并且我们希望通过同时执行两者来更快地得到答案，该怎么办？这是异步来帮助的地方。

从概念上讲，异步就像启动一样。它启动一个单独的协程，这是一个轻量级的线程，与所有其他协同程序同时工作。不同之处在于launch返回一个Job并且不携带任何结果值，同时async返回Deferred - 一个轻量级的非阻塞未来，表示稍后提供结果的承诺。您可以使用.await()延迟值来获取其最终结果，但Deferred也是a Job，因此您可以根据需要取消它。

```
fun main(args: Array<String>) = runBlocking<Unit> {
    val time = measureTimeMillis {
        val one = async { doSomethingUsefulOne() }
        val two = async { doSomethingUsefulTwo() }
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}
```

它产生这样的东西：

```
The answer is 42
Completed in 1017 ms
```

这是两倍的速度，因为我们同时执行了两个协同程序。注意，与协同程序的并发性始终是显式的。


### 懒加载实现异步

使用值为CoroutineStart.LAZY的可选参数进行异步时有一个惰性选项。
它仅在某些等待需要其结果或调用启动函数时才启动协同程序 。运行以下示例，该示例仅与此前一个示例不同：start

```
import kotlinx.coroutines.*
import kotlin.system.*

fun main() = runBlocking<Unit> {
//sampleStart
    val time = measureTimeMillis {
        val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }
        val two = async(start = CoroutineStart.LAZY) { doSomethingUsefulTwo() }
        // some computation
        one.start() // start the first one
        two.start() // start the second one
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
//sampleEnd    
}

suspend fun doSomethingUsefulOne(): Int {
    delay(1000L) // pretend we are doing something useful here
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1000L) // pretend we are doing something useful here, too
    return 29
}
```

它产生这样的东西：

```
The answer is 42
Completed in 2017 ms
```
所以，这里定义了两个协同程序，但是没有像前面的例子那样执行，但是程序员在完全通过调用start开始执行时会给出控制权。 
我们首先启动一个，然后启动两个，然后等待各个协同程序完成。

注意，如果我们在println中调用了await并且在各个协程上省略了start，
那么我们就会得到顺序行为，因为await启动协程执行并等待执行完成，这不是懒惰的预期用例。
在计算值涉及挂起函数的情况下，async（start = CoroutineStart.LAZY）的用例是标准惰性函数的替代。


### 异步风格的功能

我们可以定义使用异步协同生成器调用doSomethingUsefulOne和doSomethingUsefulTwo 异步调用的异步样式函数。
使用“Async”后缀命名此类函数是一种很好的方式，以突出显示它们只启动异步计算并且需要使用结果延迟值来获取结果的事实。

```
// somethingUsefulOneAsync的结果类型是Deferred <Int> 
fun  somethingUsefulOneAsync() =GlobalScope.async {
    doSomethingUsefulOne()
}

// somethingUsefulTwoAsync的结果类型是Deferred <Int> 
fun  somethingUsefulTwoAsync() = GlobalScope.async {
    doSomethingUsefulTwo()
}
```

注意，这些xxxAsync功能不是 暂停功能。它们可以在任何地方使用。
但是，它们的使用总是意味着它们的动作与调用代码的异步（这里意味着并发）。

以下示例显示了它们在协同程序之外的用法：


```
// note, that we don't have `runBlocking` to the right of `main` in this example
fun main(args: Array<String>) {
    val time = measureTimeMillis {
        // we can initiate async actions outside of a coroutine
        val one = somethingUsefulOneAsync()
        val two = somethingUsefulTwoAsync()
        // but waiting for a result must involve either suspending or blocking.
        // here we use `runBlocking { ... }` to block the main thread while waiting for the result
        runBlocking {
            println("The answer is ${one.await() + two.await()}")
        }
    }
    println("Completed in $time ms")
}

```

### 协同上下文和调度器

协同程序总是在某些上下文中执行，该上下文由 在Kotlin标准库中定义的CoroutineContext类型的值表示 。

协程上下文是一组各种元素。主要元素是我们之前见过的协同工作及其调度程序，本节将对其进行介绍。


### 调度器和线程

协程上下文包括一个协程调度程序（请参阅CoroutineDispatcher），它确定相应的协程用于执行的线程。
协程调度程序可以将协程执行限制在特定线程，将其分派给线程池，或让它无限制地运行。

所有协同构建器（如launch和async）都接受一个可选的 CoroutineContext 参数，
该参数可用于显式指定新协程和其他上下文元素的调度程序。

请尝试以下示例：

```
fun main(args: Array<String>) = runBlocking<Unit> {
    val jobs = arrayListOf<Job>()
    jobs += launch(Unconfined) { // not confined -- will work with main thread
        println("      'Unconfined': I'm working in thread ${Thread.currentThread().name}")
    }
    jobs += launch(coroutineContext) { // context of the parent, runBlocking coroutine
        println("'coroutineContext': I'm working in thread ${Thread.currentThread().name}")
    }
    jobs += launch(CommonPool) { // will get dispatched to ForkJoinPool.commonPool (or equivalent)
        println("      'CommonPool': I'm working in thread ${Thread.currentThread().name}")
    }
    jobs += launch(newSingleThreadContext("MyOwnThread")) { // will get its own new thread
        println("          'newSTC': I'm working in thread ${Thread.currentThread().name}")
    }
    jobs.forEach { it.join() }
}
```
它产生以下输出（可能以不同的顺序）：


```
      'Unconfined': I'm working in thread main
      'CommonPool': I'm working in thread ForkJoinPool.commonPool-worker-1
          'newSTC': I'm working in thread MyOwnThread
'coroutineContext': I'm working in thread main
```

我们在前面部分中使用的默认调度程序由DefaultDispatcher表示，
它等于当前实现中的CommonPool。所以，launch { ... }是一样的launch(DefaultDispatcher) { ... }，
它是一样的launch(CommonPool) { ... }。

父coroutineContext和 Unconfined上下文之间的区别 将在稍后显示。

注意，newSingleThreadContext创建一个新线程，这是一个非常昂贵的资源。
在实际应用程序中，它必须在不再需要时释放，使用close 函数，或者存储在顶级变量中并在整个应用程序中重用。


#### 无限制与受限制的调度器

Dispatchers.Unconfined协程调度程序在调用程序线程中启动协同程序，但只在第一个挂起点之前。
暂停后，它将在线程中恢复，该线程完全由调用的挂起函数确定。
当协同程序不消耗CPU时间也不更新任何局限于特定线程的共享数据（如UI）时，无限制调度程序是合适的。

另一方面，默认情况下，继承外部CoroutineScope的调度程序。 
特别是runBlocking协同程序的默认调度程序仅限于调用程序线程，因此继承它具有通过可预测的FIFO调度将执行限制在此线程的效果。

```
fun main(args: Array<String>) = runBlocking<Unit> {
    val jobs = arrayListOf<Job>()
    jobs += launch(Unconfined) { // not confined -- will work with main thread
        println("      'Unconfined': I'm working in thread ${Thread.currentThread().name}")
        delay(500)
        println("      'Unconfined': After delay in thread ${Thread.currentThread().name}")
    }
    jobs += launch(coroutineContext) { // context of the parent, runBlocking coroutine
        println("'coroutineContext': I'm working in thread ${Thread.currentThread().name}")
        delay(1000)
        println("'coroutineContext': After delay in thread ${Thread.currentThread().name}")
    }
    jobs.forEach { it.join() }
}
```

所以，这继承了协程coroutineContext的runBlocking {...}继续在执行main线程，
而不受限制一个曾在默认执行线程重新恢复延迟 功能使用。


### 调试协程和线程

协同程序可以暂停在一个线程，并恢复与另一个线程开敞调度员或默认多线程调度。即使使用单线程调度程序，
也可能很难弄清楚协程正在做什么，何时何地。使用线程调试应用程序的常用方法是在每个日志语句的日志文件中打印线程名称。日志框架普遍支持此功能。使用协同程序时，单独的线程名称不会给出很多上下文，因此 kotlinx.coroutines包括调试工具以使其更容易。

使用-Dkotlinx.coroutines.debugJVM选项运行以下代码：


```
fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

fun main(args: Array<String>) = runBlocking<Unit> {
    val a = async(coroutineContext) {
        log("I'm computing a piece of the answer")
        6
    }
    val b = async(coroutineContext) {
        log("I'm computing another piece of the answer")
        7
    }
    log("The answer is ${a.await() * b.await()}")
}
```

有三个协同程序。主协程（＃1） - runBlocking一个和两个协程计算延迟值a（＃2）和b（＃3）。
它们都在上下文中执行，runBlocking并且仅限于主线程。此代码的输出是：


```
[main @coroutine#2] I'm computing a piece of the answer
[main @coroutine#3] I'm computing another piece of the answer
[main @coroutine#1] The answer is 42
```
该log函数在方括号中打印线程的名称，您可以看到它是main 线程，但是当前正在执行的协程的标识符被附加到它。
打开调试模式时，会将此标识符连续分配给所有已创建的协同程序。
您可以在newCoroutineContext函数的文档中阅读有关调试工具的更多信息。


### 在线程之间跳转

使用 **-Dkotlinx.coroutines.debug**  JVM选项运行以下代码：

```
fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

fun main(args: Array<String>) {
    newSingleThreadContext("Ctx1").use { ctx1 ->
        newSingleThreadContext("Ctx2").use { ctx2 ->
            runBlocking(ctx1) {
                log("Started in ctx1")
                withContext(ctx2) {
                    log("Working in ctx2")
                }
                log("Back to ctx1")
            }
        }
    }
}
```

它演示了几种新技术。一个是使用带有明确指定上下文的runBlocking，另一个是使用withContext函数来更改协程的上下文，同时仍然保持在下面的输出中可以看到的相同协程：

```
[Ctx1 @coroutine#1] Started in ctx1
[Ctx2 @coroutine#1] Working in ctx2
[Ctx1 @coroutine#1] Back to ctx1
```
请注意，此示例还使用useKotlin标准库中的函数来释放在不再需要时使用newSingleThreadContext创建的线程。


#### 工作在上下文中

协程的工作是其背景的一部分。协程可以使用coroutineContext[Job]表达式从其自己的上下文中检索它：

```
fun main(args: Array<String>) = runBlocking<Unit> {
    println("My job is ${coroutineContext[Job]}")
}

```
在调试模式下运行时会产生类似的东西：

```
My job is 
