---
title: kotlin协程
tags: kotlin协程
grammar_cjkRuby: true
---
## Kotlin协程1.3.0 更新
## 更新时间 2018/12/12

### 你的第一个协程

``` kotlin
fun main(args: Array<String>) {
    launch { //在后台启动新的协程并继续 
        delay(1000L) //非阻塞延迟1秒（默认时间单位为ms） 
        println("World!") //延迟后打印
    }
    println("Hello,") //主线程继续，而协程延迟 
    Thread.sleep(2000L)//阻塞主线程2秒以保持JVM活动 
}
```
输出结果

```kotlin
Hello,
World!

```

从本质上讲，协同程序是轻量级的线程。它们是与发布 协同程序构建器一起启动的。您可以实现相同的结果替换  **launch { ... }** 用 **thread { ... }** ，并 **delay(...)** 用  **Thread.sleep(...)**  。尝试一下。

如果以替换launch为开头thread，则编译器会产生以下错误：

``` stylus
Error: Kotlin: Suspend functions are only allowed to be called from a coroutine or another suspend function
```

这是因为delay是一个特殊的挂起函数，它不会阻塞一个线程，但会挂起 协同程序，它只能从协程中使用。

### 桥接阻塞和非阻塞世界

第一个示例在同一代码中混合非阻塞 delay(...)和阻塞 Thread.sleep(...)。很容易迷失哪一个阻塞而另一个阻塞。让我们明确说明使用runBlocking coroutine builder进行阻塞：

``` stylus
fun main(args: Array<String>) { 
    launch { // launch new coroutine in background and continue
        delay(1000L)
        println("World!")
    }
    println("Hello,") // main thread continues here immediately
    runBlocking {     // but this expression blocks the main thread
        delay(2000L)  // ... while we delay for 2 seconds to keep JVM alive
    } 
}
```

结果是相同的，但此代码仅使用非阻塞延迟。主线程，调用runBlocking，块，直到协程内runBlocking完成。

这个例子也可以用更惯用的方式重写，runBlocking用来包装main函数的执行：

等待工作
在另一个协程正在工作时延迟一段时间并不是一个好方法。让我们明确等待（以非阻塞方式），直到我们启动的后台作业完成：

``` stylus
fun main(args: Array<String>) = runBlocking<Unit> {
    val job = launch { // launch new coroutine and keep a reference to its Job
        delay(1000L)
        println("World!")
    }
    println("Hello,")
    job.join() // wait until child coroutine completes
}
```

### 提取函数重构
让我们将代码块提取launch { ... }到一个单独的函数中。当您对此代码执行“提取功能”重构时，您将获得带有suspend修饰符的新功能。这是你的第一个暂停功能。挂起函数可以在协程内部使用，就像常规函数一样，但它们的附加功能是它们可以反过来使用其他挂起函数（如delay本示例中所示）来暂停协程的执行。



``` stylus
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
它启动了100K协同程序，一秒钟之后，每个协同程序都打印出一个点。现在，尝试使用线程。会发生什么？（很可能你的代码会产生某种内存不足的错误）

### 协同程序就像守护程序线程
下面的代码启动一个长时间运行的协同程序，每秒打印“我正在睡觉”两次，然后在一段延迟后从main函数返回：


```
fun main(args: Array<String>) = runBlocking<Unit> {
    launch {
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

在小应用程序中，从“main”方法返回可能听起来像是一个好主意，以便隐式终止所有协同程序。在较大的长期运行的应用程序中，您需要更精细的控制。在推出函数返回一个作业，可用于取消运行协程：


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

主调用后job.cancel，我们看不到其他协同程序的任何输出，因为它已被取消。还有一个Job扩展函数cancelAndJoin ，它结合了取消和连接调用。

### 取消是合作的

协同取消是合作的。协程代码必须合作才能取消。所有挂起函数kotlinx.coroutines都是可取消的。他们检查coroutine的取消并在取消时抛出CancellationException。但是，如果协程正在计算中并且未检查取消，则无法取消它，如下例所示：

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
有两种方法可以使计算代码可以取消。第一个是定期调用检查取消的挂起功能。有一个收益率的功能是实现这一目的的好选择。另一个是明确检查取消状态。让我们尝试后一种方法。

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
在实践中取消协程执行的最明显的原因是因为它的执行时间超过了一些超时。虽然您可以手动跟踪对相应作业的引用并启动单独的协同程序以在延迟后取消跟踪的协程，但是有一个准备好使用withTimeout函数执行此操作。请看以下示例：

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



该TimeoutCancellationException由抛出withTimeout是的子类CancellationException。我们之前没有看到它的堆栈跟踪打印在控制台上。这是因为在取消的协程中CancellationException被认为是协程完成的正常原因。但是，在这个例子中我们withTimeout在main函数内部使用了。

因为取消只是一个例外，所有资源都将以通常的方式关闭。您可以在超时包裹代码try {...} catch (e: TimeoutCancellationException) {...}块，如果你需要专门做一些额外的行动在任何类型的超时或使用withTimeoutOrNull功能类似于withTimeout，但返回null的超时，而不是抛出一个异常：


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

### 撰写暂停功能

#### 默认顺序
假设我们在其他地方定义了两个挂起函数，它们可以像某种远程服务调用或计算一样有用。我们只是假装它们很有用，但实际上每个只是为了这个例子的目的而延迟一秒：

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

如果需要按顺序调用它们，我们该怎么做- 首先doSomethingUsefulOne 然后 doSomethingUsefulTwo计算结果的总和？实际上，如果我们使用第一个函数的结果来决定是否需要调用第二个函数或决定如何调用它，我们就会这样做。

我们只使用正常的顺序调用，因为协程中的代码与常规代码中的代码一样，默认是顺序的。以下示例通过测量执行两个挂起函数所需的总时间来演示它：



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


### 懒惰地开始异步
使用值为CoroutineStart.LAZY的可选参数进行异步时有一个惰性选项。它仅在某些等待需要其结果或调用启动函数时才启动协同程序 。运行以下示例，该示例仅与此前一个示例不同：start

```
fun main(args: Array<String>) = runBlocking<Unit> {
    val time = measureTimeMillis {
        val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }
        val two = async(start = CoroutineStart.LAZY) { doSomethingUsefulTwo() }
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}
```

它产生这样的东西：

```
The answer is 42
Completed in 2017 ms
```
所以，我们回到顺序执行，因为我们首先启动并等待one，然后启动并等待two。它不是懒惰的预期用例。lazy在计算值涉及暂停函数的情况下，它被设计为标准函数的替代。


### 异步风格的功能

我们可以定义使用异步协同生成器调用doSomethingUsefulOne和doSomethingUsefulTwo 异步调用的异步样式函数。使用“Async”后缀命名此类函数是一种很好的方式，以突出显示它们只启动异步计算并且需要使用结果延迟值来获取结果的事实。

```
// somethingUsefulOneAsync的结果类型是Deferred <Int> 
fun  somethingUsefulOneAsync（） = async {
    doSomethingUsefulOne（）
}

// somethingUsefulTwoAsync的结果类型是Deferred <Int> 
fun  somethingUsefulTwoAsync（） = async {
    doSomethingUsefulTwo（）
}
```

注意，这些xxxAsync功能不是 暂停功能。它们可以在任何地方使用。但是，它们的使用总是意味着它们的动作与调用代码的异步（这里意味着并发）。

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

### 协同上下文和调度员

协同程序总是在某些上下文中执行，该上下文由 在Kotlin标准库中定义的CoroutineContext类型的值表示 。

协程上下文是一组各种元素。主要元素是我们之前见过的协同工作及其调度程序，本节将对其进行介绍。


### 调度员和线程

协程上下文包括一个协程调度程序（请参阅CoroutineDispatcher），它确定相应的协程用于执行的线程。协程调度程序可以将协程执行限制在特定线程，将其分派给线程池，或让它无限制地运行。

所有协同构建器（如launch和async）都接受一个可选的 CoroutineContext 参数，该参数可用于显式指定新协程和其他上下文元素的调度程序。

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

我们在前面部分中使用的默认调度程序由DefaultDispatcher表示，它等于当前实现中的CommonPool。所以，launch { ... }是一样的launch(DefaultDispatcher) { ... }，它是一样的launch(CommonPool) { ... }。

父coroutineContext和 Unconfined上下文之间的区别 将在稍后显示。

注意，newSingleThreadContext创建一个新线程，这是一个非常昂贵的资源。在实际应用程序中，它必须在不再需要时释放，使用close 函数，或者存储在顶级变量中并在整个应用程序中重用。


#### 无限制与受限制的调度员


该开敞协程调度员开始协程在调用线程，但直到第一个悬挂点。暂停后，它将在线程中恢复，该线程完全由调用的挂起函数确定。当协同程序不消耗CPU时间也不更新任何局限于特定线程的共享数据（如UI）时，无限制调度程序是合适的。

另一方面， coroutineContext 属性（在任何协同程序中可用）是对此特定协同程序的上下文的引用。这样，可以继承父上下文。特别是runBlocking协同程序的默认调度程序仅限于调用程序线程，因此继承它具有通过可预测的FIFO调度将执行限制在此线程的效果。

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

所以，这继承了协程coroutineContext的runBlocking {...}继续在执行main线程，而不受限制一个曾在默认执行线程重新恢复延迟 功能使用。


### 调试协程和线程

协同程序可以暂停在一个线程，并恢复与另一个线程开敞调度员或默认多线程调度。即使使用单线程调度程序，也可能很难弄清楚协程正在做什么，何时何地。使用线程调试应用程序的常用方法是在每个日志语句的日志文件中打印线程名称。日志框架普遍支持此功能。使用协同程序时，单独的线程名称不会给出很多上下文，因此 kotlinx.coroutines包括调试工具以使其更容易。

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

有三个协同程序。主协程（＃1） - runBlocking一个和两个协程计算延迟值a（＃2）和b（＃3）。它们都在上下文中执行，runBlocking并且仅限于主线程。此代码的输出是：


```
[main @coroutine#2] I'm computing a piece of the answer
[main @coroutine#3] I'm computing another piece of the answer
[main @coroutine#1] The answer is 42
```
该log函数在方括号中打印线程的名称，您可以看到它是main 线程，但是当前正在执行的协程的标识符被附加到它。打开调试模式时，会将此标识符连续分配给所有已创建的协同程序。

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
My job is "coroutine#1":BlockingCoroutine{Active}@6d311334
```

因此，isActive在CoroutineScope仅仅是一个方便快捷 coroutineContext[Job]?.isActive == true。


### 子协程

当 coroutineContext 协程的用于启动另一个协程，该工作新协程成为孩子的家长协程的工作。当父协程被取消时，它的所有子节点也会被递归取消。

```
fun main(args: Array<String>) = runBlocking<Unit> {
    // launch a coroutine to process some kind of incoming request
    val request = launch {
        // it spawns two other jobs, one with its separate context
        val job1 = launch {
            println("job1: I have my own context and execute independently!")
            delay(1000)
            println("job1: I am not affected by cancellation of the request")
        }
        // and the other inherits the parent context
        val job2 = launch(coroutineContext) {
            delay(100)
            println("job2: I am a child of the request coroutine")
            delay(1000)
            println("job2: I will not execute this line if my parent request is cancelled")
        }
        // request completes when both its sub-jobs complete:
        job1.join()
        job2.join()
    }
    delay(500)
    request.cancel() // cancel processing of the request
    delay(1000) // delay a second to see what happens
    println("main: Who has survived request cancellation?")
}
```

此代码的输出是：
```
job1: I have my own context and execute independently!
job2: I am a child of the request coroutine
job1: I am not affected by cancellation of the request
main: Who has survived request cancellation?

```

### 结合上下文
可以使用+运算符组合协程上下文。右侧的上下文替换了左侧上下文的相关条目。例如，可以继承父协程的Job，同时替换其调度程序：

```
fun main(args: Array<String>) = runBlocking<Unit> {
    // start a coroutine to process some kind of incoming request
    val request = launch(coroutineContext) { // use the context of `runBlocking`
        // spawns CPU-intensive child job in CommonPool !!! 
        val job = launch(coroutineContext + CommonPool) {
            println("job: I am a child of the request coroutine, but with a different dispatcher")
            delay(1000)
            println("job: I will not execute this line if my parent request is cancelled")
        }
        job.join() // request completes when its sub-job completes
    }
    delay(500)
    request.cancel() // cancel processing of the request
    delay(1000) // delay a second to see what happens
    println("main: Who has survived request cancellation?")
}

```

此代码的预期结果是：


```
job: I am a child of the request coroutine, but with a different dispatcher
main: Who has survived request cancellation?

```

### 父母的责任
父协同程序总是等待所有孩子的完成。Parent不必显式跟踪它启动的所有子节点，也不必使用Job.join在结束时等待它们：


```
fun main(args: Array<String>) = runBlocking<Unit> {
    // launch a coroutine to process some kind of incoming request
    val request = launch {
        repeat(3) { i -> // launch a few children jobs
            launch(coroutineContext)  {
                delay((i + 1) * 200L) // variable delay 200ms, 400ms, 600ms
                println("Coroutine $i is done")
            }
        }
        println("request: I'm done and I don't explicitly join my children that are still active")
    }
    request.join() // wait for completion of the request, including all its children
    println("Now processing of the request is complete")
}

```


结果将是：

```
request: I'm done and I don't explicitly join my children that are still active
Coroutine 0 is done
Coroutine 1 is done
Coroutine 2 is done
Now processing of the request is complete

```

#### 命名协同程序以进行调试
当协同程序经常记录时，自动分配的ID很好，您只需要关联来自同一协程的日志记录。但是，当协程与特定请求的处理或执行某些特定后台任务相关联时，最好将其明确命名以用于调试目的。 CoroutineName上下文元素与线程名称具有相同的功能。当调试模式打开时，它将显示在执行此协程的线程名称中。

以下示例演示了此概念：

```
fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

fun main(args: Array<String>) = runBlocking(CoroutineName("main")) {
    log("Started main coroutine")
    // run two background value computations
    val v1 = async(CoroutineName("v1coroutine")) {
        delay(500)
        log("Computing v1")
        252
    }
    val v2 = async(CoroutineName("v2coroutine")) {
        delay(1000)
        log("Computing v2")
        6
    }
    log("The answer for v1 / v2 = ${v1.await() / v2.await()}")
}
```

它使用-Dkotlinx.coroutines.debugJVM选项生成的输出类似于：



```
[main @main#1] Started main coroutine
[ForkJoinPool.commonPool-worker-1 @v1coroutine#2] Computing v1
[ForkJoinPool.commonPool-worker-2 @v2coroutine#3] Computing v2
[main @main#1] The answer for v1 / v2 = 42
```

### 通过明确的工作取消

让我们将关于上下文，child对象和工作的知识放在一起。假设我们的应用程序有一个具有生命周期的对象，但该对象不是协程。例如，我们正在编写一个Android应用程序，并在Android活动的上下文中启动各种协同程序，以执行异步操作以获取和更新数据，执行动画等。所有这些协同程序必须在活动被销毁时取消，以避免内存泄漏。

我们可以通过创建与我们活动的生命周期相关联的Job实例来管理协同程序的生命周期。使用Job（）工厂函数创建作业实例，如以下示例所示。为方便起见，launch(coroutineContext + job)我们可以编写launch(coroutineContext, parent = job)以明确表示正在使用父作业的事实，而不是使用表达式。

现在，Job.cancel的单个调用取消了我们启动的所有孩子。此外，Job.join等待所有这些完成，所以我们也可以在这个示例中使用cancelAndJoin：


```
fun main(args: Array<String>) = runBlocking<Unit> {
    val job = Job() // create a job object to manage our lifecycle
    // now launch ten coroutines for a demo, each working for a different time
    val coroutines = List(10) { i ->
        // they are all children of our job object
        launch(coroutineContext, parent = job) { // we use the context of main runBlocking thread, but with our parent job
            delay((i + 1) * 200L) // variable delay 200ms, 400ms, ... etc
            println("Coroutine $i is done")
        }
    }
    println("Launched ${coroutines.size} coroutines")
    delay(500L) // delay for half a second
    println("Cancelling the job!")
    job.cancelAndJoin() // cancel all our coroutines and wait for all of them to complete
}



```

```
Launched 10 coroutines
Coroutine 0 is done
Coroutine 1 is done
Cancelling the job!
```

正如你所看到的，只有前三个协同程序打印了一条消息，而其他协同程序被一次调用取消了job.cancelAndJoin()。因此，我们在假设的Android应用程序中需要做的就是在创建活动时创建父作业对象，将其用于子协同程序，并在销毁活动时取消它。我们不能join在Android生命周期的情况下使用它们，因为它是同步的，但是这种连接能力在构建后端服务以确保有限的资源使用时非常有用。


### 通道

延迟值提供了在协同程序之间传输单个值的便捷方法。管道提供了一种传输值流的方法。


### 通道的基础知识

一个通道是在概念上非常相似BlockingQueue。一个关键的区别是，它不是阻塞put操作，而是暂停发送，而不是阻塞take操作，它有一个暂停接收。

```

fun main(args: Array<String>) = runBlocking<Unit> {
    val channel = Channel<Int>()
    launch {
        // this might be heavy CPU-consuming computation or async logic, we'll just send five squares
        for (x in 1..5) channel.send(x * x)
    }
    // here we print five received integers:
    repeat(5) { println(channel.receive()) }
    println("Done!")
}

```

此代码的输出是：


```
1
4
9
16
25
Done!
```


### 关闭和迭代通道

与队列不同，可以关闭通道以指示不再有元素到来。在接收器端，使用常规for循环来接收来自信道的元素是方便的。

从概念上讲，关闭就像向通道发送特殊的关闭令牌。一旦收到此关闭令牌，迭代就会停止，因此可以保证收到关闭前所有先前发送的元素：


```

fun main(args: Array<String>) = runBlocking<Unit> {
    val channel = Channel<Int>()
    launch {
        for (x in 1..5) channel.send(x * x)
        channel.close() // we're done sending
    }
    // here we print received values using `for` loop (until the channel is closed)
    for (y in channel) println(y)
    println("Done!")
}
```
### 建立渠道生产者

协程生成一系列元素的模式很常见。这是生产者 - 消费者模式的一部分，通常在并发代码中找到。您可以将这样的生成器抽象为一个以通道作为参数的函数，但这与必须从函数返回结果的常识相反。

有一个名为produce的便利协程构建器，它可以很容易地在生产者端执行，并且扩展函数consumeEach，它取代了for消费者端的循环：

```
fun produceSquares() = produce<Int> {
    for (x in 1..5) send(x * x)
}

fun main(args: Array<String>) = runBlocking<Unit> {
    val squares = produceSquares()
    squares.consumeEach { println(it) }
    println("Done!")
}

```
### **管道**

管道是一个协程正在生成的模式，可能是无限的值流：


```
fun produceNumbers() = produce<Int> {
    var x = 1
    while (true) send(x++) // infinite stream of integers starting from 1
}
```



而另一个协程或协同程序正在消耗该流，进行一些处理，并产生一些其他结果。在下面的例子中，数字只是平方：


```
fun square(numbers: ReceiveChannel<Int>) = produce<Int> {
    for (x in numbers) send(x * x)
}
```
主代码启动并连接整个管道:

```
fun main(args: Array<String>) = runBlocking<Unit> {
    val numbers = produceNumbers() // produces integers from 1 and on
    val squares = square(numbers) // squares integers
    for (i in 1..5) println(squares.receive()) // print first five
    println("Done!") // we are done
    squares.cancel() // need to cancel these coroutines in a larger app
    numbers.cancel()
}
```

我们不必在这个示例应用程序中取消这些协同程序，因为 协同程序就像守护程序线程，但是在更大的应用程序中，如果我们不再需要它，我们将需要停止我们的管道。或者，我们可以运行管道协同程序作为 主协程的子代，如以下示例所示。

### 带管道的素数
让我们通过一个使用协程管道生成素数的例子将管道带到极端。我们从无限的数字序列开始。这次我们引入一个显式context参数并将其传递给generate构建器，以便调用者可以控制我们的协程运行的位置：

``` stylus
fun numbersFrom(context: CoroutineContext, start: Int) = produce<Int>(context) {
    var x = start
    while (true) send(x++) // infinite stream of integers from start
}
```
以下管道阶段过滤传入的数字流，删除可由给定素数整除的所有数字：

``` stylus
fun filter(context: CoroutineContext, numbers: ReceiveChannel<Int>, prime: Int) = produce<Int>(context) {
    for (x in numbers) if (x % prime != 0) send(x)
}
```
现在我们通过从2开始一个数字流来构建我们的管道，从当前通道获取素数，并为找到的每个素数启动新的管道阶段：

``` stylus
numbersFrom(2) -> filter(2) -> filter(3) -> filter(5) -> filter(7) ... 
```
以下示例打印前十个素数，在主线程的上下文中运行整个管道。由于所有协同程序都是在其coroutineContext中作为主runBlocking协程的 子进程启动的，因此我们不必保留我们已经启动的所有协同程序的明确列表。我们使用cancelChildren 扩展函数取消所有子协同程序。

``` stylus
fun main(args: Array<String>) = runBlocking<Unit> {
    var cur = numbersFrom(coroutineContext, 2)
    for (i in 1..10) {
        val prime = cur.receive()
        println(prime)
        cur = filter(coroutineContext, cur, prime)
    }
    coroutineContext.cancelChildren() // cancel all children to let main finish
}
```



此代码的输出是：

``` stylus
2
3
5
7
11
13
17
19
23
29
```

请注意，您可以使用buildIterator 标准库中的coroutine builder 来构建相同的管道 。更换produce用buildIterator，send用yield，receive用next， ReceiveChannel用Iterator，并摆脱上下文。你也不需要runBlocking。但是，如上所示使用通道的管道的好处是，如果在CommonPool上下文中运行它，它实际上可以使用多个CPU内核。

无论如何，这是找到素数的极不切实际的方法。在实践中，管道确实涉及一些其他挂起调用（如对远程服务的异步调用），并且这些管道不能使用buildSeqeunce/ 构建buildIterator，因为它们不允许任意挂起，这与produce完全异步完全不同 。

### 扇出


多个协同程序可以从同一个通道接收，在它们之间分配工作。让我们从生成器协程开始，它定期生成整数（每秒十个数字）：

``` stylus
fun produceNumbers() = produce<Int> {
    var x = 1 // start from 1
    while (true) {
        send(x++) // produce next
        delay(100) // wait 0.1s
    }
}
```

然后我们可以有几个处理器协同程序。在这个例子中，他们只打印他们的id和收到的号码：

``` stylus
fun launchProcessor(id: Int, channel: ReceiveChannel<Int>) = launch {
    for (msg in channel) {
        println("Processor #$id received $msg")
    }    
}
```

现在让我们启动五个处理器，让它们工作几乎一秒钟。走着瞧吧：


``` 

fun main(args: Array<String>) = runBlocking<Unit> {
    val producer = produceNumbers()
    repeat(5) { launchProcessor(it, producer) }
    delay(950)
    producer.cancel() // cancel producer coroutine and thus kill them all
}

```
输出将类似于以下输出，尽管接收每个特定整数的处理器ID可能不同：

``` stylus
Processor #2 received 1
Processor #4 received 2
Processor #0 received 3
Processor #1 received 4
Processor #3 received 5
Processor #2 received 6
Processor #4 received 7
Processor #0 received 8
Processor #1 received 9
Processor #3 received 10
```

注意，取消生成器协同程序会关闭其通道，从而最终终止处理器协同程序正在执行的通道上的迭代。

另外，请注意我们如何使用for循环显式迭代通道以在launchProcessor代码中执行扇出。与consumeEach此不同，这种for循环模式可以非常安全地从多个协同程序中使用。如果其中一个处理器协同程序失败，则其他处理程序协同程序仍将处理该通道，而通过其写入的处理器consumeEach 总是在正常或异常终止时消耗（取消）底层通道。

### 扇入

多个协同程序可以发送到同一个通道。例如，让我们有一个字符串通道和一个挂起函数，它以指定的延迟重复发送指定的字符串到此通道：


``` 
suspend fun sendString(channel: SendChannel<String>, s: String, time: Long) {
    while (true) {
        delay(time)
        channel.send(s)
    }
}

```
现在，让我们看看如果我们启动几个协同程序发送字符串会发生什么（在这个例子中，我们在主线程的上下文中将它们作为主协程的子节点启动）：


``` 
fun main(args: Array<String>) = runBlocking<Unit> {
    val channel = Channel<String>()
    launch(coroutineContext) { sendString(channel, "foo", 200L) }
    launch(coroutineContext) { sendString(channel, "BAR!", 500L) }
    repeat(6) { // receive first six
        println(channel.receive())
    }
    coroutineContext.cancelChildren() // cancel all children to let main finish
}

```
输出是：

```
foo
foo
BAR!
foo
foo
BAR!


```

### 缓冲频道

到目前为止显示的通道没有缓冲区。当发送方和接收方彼此相遇（也称为集合点）时，无缓冲的信道传输元素。如果首先调用send，那么它将被挂起，直到调用receive，如果先调用receive，它将被挂起，直到调用send。

两个信道（）工厂函数和产生助洗剂采取可选的capacity参数来指定缓冲区大小。缓冲区允许发送方在挂起之前发送多个元素，类似于BlockingQueue具有指定容量的缓冲区已满时阻塞。

看一下以下代码的行为：


``` stylus
fun main(args: Array<String>) = runBlocking<Unit> {
    val channel = Channel<Int>(4) // create buffered channel
    val sender = launch(coroutineContext) { // launch sender coroutine
        repeat(10) {
            println("Sending $it") // print before sending each element
            channel.send(it) // will suspend when buffer is full
        }
    }
    // don't receive anything... just wait....
    delay(1000)
    sender.cancel() // cancel sender coroutine
}
```
它使用容量为4的缓冲通道打印“发送” 五次：

``` stylus
Sending 0
Sending 1
Sending 2
Sending 3
Sending 4
```

前四个元素被添加到缓冲区，发送方在尝试发送第五个元素时暂停。

### Ticker通道

Ticker通道是一个特殊的会合通道，Unit每次从此通道上次消耗后产生给定的延迟通道。虽然它可能看起来没有用，但它是一个有用的构建块，可以创建复杂的基于时间的生产 管道和操作员，这些管道和操作员可以进行窗口化和其他时间依赖的处理。可以在select中使用Ticker通道执行“on tick”操作。

要创建此类渠道，请使用工厂方法代码。要指示不需要其他元素，请使用ReceiveChannel.cancel方法。

``` 
fun main(args: Array<String>) = runBlocking<Unit> {
    val tickerChannel = ticker(delay = 100, initialDelay = 0) // create ticker channel
    var nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
    println("Initial element is available immediately: $nextElement") // initial delay hasn't passed yet

    nextElement = withTimeoutOrNull(50) { tickerChannel.receive() } // all subsequent elements has 100ms delay
    println("Next element is not ready in 50 ms: $nextElement")

    nextElement = withTimeoutOrNull(60) { tickerChannel.receive() }
    println("Next element is ready in 100 ms: $nextElement")

    // Emulate large consumption delays
    println("Consumer pauses for 150ms")
    delay(150)
    // Next element is available immediately
    nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
    println("Next element is available immediately after large consumer delay: $nextElement")
    // Note that the pause between `receive` calls is taken into account and next element arrives faster
    nextElement = withTimeoutOrNull(60) { tickerChannel.receive() } 
    println("Next element is ready in 50ms after consumer pause in 150ms: $nextElement")

    tickerChannel.cancel() // indicate that no more elements are needed
}


```
它打印以下行：

``` stylus

Initial element is available immediately: kotlin.Unit
Next element is not ready in 50 ms: null
Next element is ready in 100 ms: kotlin.Unit
Consumer pauses for 150ms
Next element is available immediately after large consumer delay: kotlin.Unit
Next element is ready in 50ms after consumer pause in 150ms: kotlin.Unit

```
请注意，自动收报机知道可能的消费者暂停，并且默认情况下，如果发生暂停，则调整下一个生成的元素延迟，尝试维持生成元素的固定速率。

可选地，mode可以指定等于[TickerMode.FIXED_DELAY]的参数以维持元素之间的固定延迟。


### 渠道公平

对于从多个协同程序调用它们的顺序，向通道发送和接收操作是公平的。它们以先进先出顺序提供，例如，要调用的第一个协程receive 获取元素。在以下示例中，两个协程“ping”和“pong”正在从共享的“table”通道接收“ball”对象。

``` stylus
data class Ball(var hits: Int)

fun main(args: Array<String>) = runBlocking<Unit> {
    val table = Channel<Ball>() // a shared table
    launch(coroutineContext) { player("ping", table) }
    launch(coroutineContext) { player("pong", table) }
    table.send(Ball(0)) // serve the ball
    delay(1000) // delay 1 second
    coroutineContext.cancelChildren() // game over, cancel them
}

suspend fun player(name: String, table: Channel<Ball>) {
    for (ball in table) { // receive the ball in a loop
        ball.hits++
        println("$name $ball")
        delay(300) // wait a bit
        table.send(ball) // send the ball back
    }
}
```

“ping”协程首先启动，因此它是第一个接收球的人。即使“ping”coroutine在将球送回桌面后立即再次接球，球也会被“pong”协程接收，因为它已经在等待它了：

``` stylus
ping Ball(hits=1)
pong Ball(hits=2)
ping Ball(hits=3)
pong Ball(hits=4)
```
请注意，由于正在使用的执行程序的性质，有时通道可能会产生看起来不公平的执行。有关详细信息，请参阅此问


#### 共享可变状态和并发
可以使用多线程调度程序（如默认的CommonPool）同时执行协同程序。它提出了所有常见的并发问题。主要问题是同步访问共享可变状态。在协同程序领域，这个问题的一些解决方案类似于多线程世界中的解决方案，但其他解决方案是独一无二的。
 
#### 问题

让我们推出一千个协同程序，它们都做了一千次相同的动作（总计一百万次执行）。我们还将测量完成时间以进行进一步比较：


``` stylus
suspend fun massiveRun(context: CoroutineContext, action: suspend () -> Unit) {
    val n = 1000 // number of coroutines to launch
    val k = 1000 // times an action is repeated by each coroutine
    val time = measureTimeMillis {
        val jobs = List(n) {
            launch(context) {
                repeat(k) { action() }
            }
        }
        jobs.forEach { it.join() }
    }
    println("Completed ${n * k} actions in $time ms")    
}
```
我们从一个非常简单的操作开始，该操作使用多线程CommonPool上下文来增加共享的可变变量。

``` stylus
var counter = 0

fun main(args: Array<String>) = runBlocking<Unit> {
    massiveRun(CommonPool) {
        counter++
    }
    println("Counter = $counter")
}
```
最后打印什么？它不太可能打印“Counter = 1000000”，因为一千个协程counter从多个线程同时增加而没有任何同步。



> 注意：如果您的旧系统具有2个或更少的CPU，那么您将始终看到1000000，因为 CommonPool在这种情况下仅在一个线程中运行。要重现此问题，您需要进行以下更改：

``` 

val mtContext = newFixedThreadPoolContext(2, "mtPool") // explicitly define context with two threads
var counter = 0

fun main(args: Array<String>) = runBlocking<Unit> {
    massiveRun(mtContext) { // use it instead of CommonPool in this sample and below 
        counter++
    }
    println("Counter = $counter")
}
```


最后打印什么？它不太可能打印“Counter = 1000000”，因为一千个协程counter从多个线程同时增加而没有任何同步。

> 注意：如果您的旧系统具有2个或更少的CPU，那么您将始终看到1000000，因为 CommonPool在这种情况下仅在一个线程中运行。要重现此问题，您需要进行以下更改：

``` 
val mtContext = newFixedThreadPoolContext(2, "mtPool") // explicitly define context with two threads
var counter = 0

fun main(args: Array<String>) = runBlocking<Unit> {
    massiveRun(mtContext) { // use it instead of CommonPool in this sample and below 
        counter++
    }
    println("Counter = $counter")
}

```
**Volatiles** 没有任何帮助
有一个常见的误解是，使变量volatile解决了并发问题。让我们试一试：

``` 
@Volatile // in Kotlin `volatile` is an annotation 
var counter = 0

fun main(args: Array<String>) = runBlocking<Unit> {
    massiveRun(CommonPool) {
        counter++
    }
    println("Counter = $counter")
}
```
这段代码运行速度较慢，但​​我们仍然没有得到“Counter = 1000000”，因为volatile变量保证可线性化（这是“原子”的技术术语）读取和写入相应的变量，但不提供原子性较大的行动（在我们的案例中增加）


### 线程安全的数据结构


适用于线程和协同程序的通用解决方案是使用线程安全（也称为同步，可线性化或原子）数据结构，该数据结构为需要在共享状态上执行的相应操作提供所有必需的同步。在简单计数器的情况下，我们可以使用AtomicInteger具有原子incrementAndGet操作的类：

``` stylus
var counter = AtomicInteger()

fun main(args: Array<String>) = runBlocking<Unit> {
    massiveRun(CommonPool) {
        counter.incrementAndGet()
    }
    println("Counter = ${counter.get()}")
}
```

这是针对此特定问题的最快解决方案。它适用于普通计数器，集合，队列和其他标准数据结构以及它们的基本操作。但是，它不容易扩展到复杂状态或没有现成的线程安全实现的复杂操作。

### 线程限制细粒度

线程限制是解决共享可变状态问题的一种方法，其中对特定共享状态的所有访问都限于单个线程。它通常用于UI应用程序，其中所有UI状态都局限于单个事件派发/应用程序线程。使用
单线程上下文很容易应用协同程序：

``` stylus
val counterContext = newSingleThreadContext("CounterContext")
var counter = 0

fun main(args: Array<String>) = runBlocking<Unit> {
    massiveRun(CommonPool) { // run each coroutine in CommonPool
        withContext(counterContext) { // but confine each increment to the single-threaded context
            counter++
        }
    }
    println("Counter = $counter")
}
```

此代码的工作速度非常慢，因为它可以进行细粒度的线程限制。每个增量CommonPool使用withContext块从多线程上下文切换到单线程上下文。

####  线程限制粗粒度

实际上，线程限制是在大块中执行的，例如，大块状态更新业务逻辑仅限于单个线程。下面的示例就是这样，在单线程上下文中运行每个协程开始。

``` 
val counterContext = newSingleThreadContext("CounterContext")
var counter = 0

fun main(args: Array<String>) = runBlocking<Unit> {
    massiveRun(counterContext) { // run each coroutine in the single-threaded context
        counter++
    }
    println("Counter = $counter")
}
```
这现在可以更快地运行并产生正确的结果。

### 相互排斥

该问题的相互排除解决方案 是使用永远不会同时执行的关键部分来保护共享状态的所有修改。在一个阻塞的世界中，你通常会使用synchronized或ReentrantLock为此而使用。Coroutine的替代品叫做Mutex。它具有锁定和解锁功能，可以分隔关键部分。关键的区别在于它Mutex.lock()是一个暂停功能。它不会阻塞线程。
还有withLock扩展功能，方便代表 mutex.lock(); try { ... } finally { mutex.unlock() }模式：

``` 
val mutex = Mutex()
var counter = 0

fun main(args: Array<String>) = runBlocking<Unit> {
    massiveRun(CommonPool) {
        mutex.withLock {
            counter++        
        }
    }
    println("Counter = $counter")
}
```

此示例中的锁定是细粒度的，因此它付出了代价。但是，对于某些必须定期修改某些共享状态的情况，它是一个不错的选择，但是没有自然线程可以限制此状态。

### Actors

的演员是由一个协程，即被限制和封装到该协程的状态下，并与其他协同程序进行通信的信道的组合的实体。一个简单的actor可以写成一个函数，但是一个具有复杂状态的actor更适合一个类。

有一个actor协程构建器，它可以方便地将actor的邮箱通道组合到其作用域中，以便从发送通道接收消息并将其组合到生成的作业对象中，这样对actor的单个引用就可以作为其句柄携带。

使用actor的第一步是定义一个actor要处理的消息类。Kotlin的密封课程非常适合这个目的。我们CounterMsg使用IncCounter消息定义密封类以增加计数器和GetCounter消息以获取其值。后者需要发送回复。甲CompletableDeferred通信原码，即表示将在将来已知的（传送）一个单一的值，在这里用于该目的。

``` 
sealed class CounterMsg
object IncCounter : CounterMsg() // one-way message to increment counter
class GetCounter(val response: CompletableDeferred<Int>) : CounterMsg() // a request with reply

```
然后我们定义一个使用actor coroutine builder 启动actor的函数：


``` stylus
// This function launches a new counter actor
fun counterActor() = actor<CounterMsg> {
    var counter = 0 // actor state
    for (msg in channel) { // iterate over incoming messages
        when (msg) {
            is IncCounter -> counter++
            is GetCounter -> msg.response.complete(counter)
        }
    }
}
```

主要代码很简单：

``` 
fun main(args: Array<String>) = runBlocking<Unit> {
    val counter = counterActor() // create the actor
    massiveRun(CommonPool) {
        counter.send(IncCounter)
    }
    // send a message to get a counter value from an actor
    val response = CompletableDeferred<Int>()
    counter.send(GetCounter(response))
    println("Counter = ${response.await()}")
    counter.close() // shutdown the actor
}
```
执行者本身执行的上下文无关紧要（正确性）。一个actor是一个协程，一个协同程序按顺序执行，因此将状态限制到特定协程可以解决共享可变状态的问题。实际上，演员可以修改自己的私有状态，但只能通过消息相互影响（避免任何锁定）。


Actor比在负载下锁定更有效，因为在这种情况下它总是有工作要做，而且根本不需要切换到不同的上下文。

> 注意，actor协程构建器是产品协同程序构建器的双重构件。一个actor与它接收消息的频道相关联，而一个制作者与它发送元素的频道相关联。

### 选择表达式

选择表达式可以同时等待多个挂起函数，并选择 第一个可用的挂起函数。

#### 从频道中选择

让我们有两个字符串生成器：fizz和buzz。该fizz生产“菲斯”串每300毫秒：

``` 
fun fizz(context: CoroutineContext) = produce<String>(context) {
    while (true) { // sends "Fizz" every 300 ms
        delay(300)
        send("Fizz")
    }
}
```

而buzz产品“Buzz！” 字符串每500毫秒：

``` 
fun buzz(context: CoroutineContext) = produce<String>(context) {
    while (true) { // sends "Buzz!" every 500 ms
        delay(500)
        send("Buzz!")
    }
}

```

使用接收暂停功能，我们可以接收任一从一个通道或其他。但select表达式允许我们同时使用其 onReceive子句从两者接收：

``` 
suspend fun selectFizzBuzz(fizz: ReceiveChannel<String>, buzz: ReceiveChannel<String>) {
    select<Unit> { // <Unit> means that this select expression does not produce any result 
        fizz.onReceive { value ->  // this is the first select clause
            println("fizz -> '$value'")
        }
        buzz.onReceive { value ->  // this is the second select clause
            println("buzz -> '$value'")
        }
    }
}
```

让我们一起运行七次：

``` 
fun main(args: Array<String>) = runBlocking<Unit> {
    val fizz = fizz(coroutineContext)
    val buzz = buzz(coroutineContext)
    repeat(7) {
        selectFizzBuzz(fizz, buzz)
    }
    coroutineContext.cancelChildren() // cancel fizz & buzz coroutines    
}
```
这段代码的结果是：

``` 
fizz -> 'Fizz'
buzz -> 'Buzz!'
fizz -> 'Fizz'
fizz -> 'Fizz'
buzz -> 'Buzz!'
fizz -> 'Fizz'
buzz -> 'Buzz!'

```
### 选择关闭

所述的onReceive条款select当信道被关闭引起相应失败 select抛出异常。我们可以使用onReceiveOrNull子句在关闭通道时执行特定操作。以下示例还显示该select表达式返回其所选子句的结果：


``` 
suspend fun selectAorB(a: ReceiveChannel<String>, b: ReceiveChannel<String>): String =
    select<String> {
        a.onReceiveOrNull { value -> 
            if (value == null) 
                "Channel 'a' is closed" 
            else 
                "a -> '$value'"
        }
        b.onReceiveOrNull { value -> 
            if (value == null) 
                "Channel 'b' is closed"
            else    
                "b -> '$value'"
        }
    }
```

让我们使用它a产生“Hello”字符串四次的频道b和产生“世界”四次的频道：

``` 
fun main(args: Array<String>) = runBlocking<Unit> {
    // we are using the context of the main thread in this example for predictability ... 
    val a = produce<String>(coroutineContext) {
        repeat(4) { send("Hello $it") }
    }
    val b = produce<String>(coroutineContext) {
        repeat(4) { send("World $it") }
    }
    repeat(8) { // print first eight results
        println(selectAorB(a, b))
    }
    coroutineContext.cancelChildren()    
}

```
这段代码的结果非常有趣，所以我们将在模式细节中分析它：

``` 
a -> 'Hello 0'
a -> 'Hello 1'
b -> 'World 0'
a -> 'Hello 2'
a -> 'Hello 3'
b -> 'World 1'
Channel 'a' is closed
Channel 'a' is closed
```

有几个观察要做出来。

首先，select是偏向于第一条。当可以同时选择多个子句时，其中的第一个子句将被选中。在这里，两个通道都在不断地产生字符串，因此a作为select中的第一个子句的channel获胜。但是，因为我们使用的是无缓冲通道，所以a它的发送调用会不时地暂停，并且也有机会b发送。

第二个观察结果是，当通道已经关闭时，会立即选择onReceiveOrNull。

### 选择发送

选择表达式具有onSend子句，可以与选择的偏见性结合使用。

让我们编写一个整数生成器的示例，side当主要通道上的消费者无法跟上它时，它会将其值发送到通道：

``` 
fun produceNumbers(context: CoroutineContext, side: SendChannel<Int>) = produce<Int>(context) {
    for (num in 1..10) { // produce 10 numbers from 1 to 10
        delay(100) // every 100 ms
        select<Unit> {
            onSend(num) {} // Send to the primary channel
            side.onSend(num) {} // or to the side channel     
        }
    }
}
```

消费者将会非常缓慢，需要250毫秒才能处理每个号码：

``` 

fun main(args: Array<String>) = runBlocking<Unit> {
    val side = Channel<Int>() // allocate side channel
    launch(coroutineContext) { // this is a very fast consumer for the side channel
        side.consumeEach { println("Side channel has $it") }
    }
    produceNumbers(coroutineContext, side).consumeEach { 
        println("Consuming $it")
        delay(250) // let us digest the consumed number properly, do not hurry
    }
    println("Done consuming")
    coroutineContext.cancelChildren()    
}

```
那么让我们看看会发生什么：

``` 
Consuming 1
Side channel has 2
Side channel has 3
Consuming 4
Side channel has 5
Side channel has 6
Consuming 7
Side channel has 8
Side channel has 9
Consuming 10
Done consuming
```

#### 选择延期值
可以使用onAwait子句选择延迟值。让我们从一个异步函数开始，该函数在随机延迟后返回一个延迟字符串值：

``` 
fun asyncString(time: Int) = async {
    delay(time.toLong())
    "Waited for $time ms"
}

```
让我们随机延迟开始十几个。

``` 
fun asyncStringsList(): List<Deferred<String>> {
    val random = Random(3)
    return List(12) { asyncString(random.nextInt(1000)) }
}
```

现在，主函数等待第一个函数完成并计算仍处于活动状态的延迟值的数量。注意，我们在这里使用的select表达式是Kotlin DSL，因此我们可以使用任意代码为它提供子句。在这种情况下，我们遍历一个延迟值列表，onAwait为每个延迟值提供子句。

``` 
fun main(args: Array<String>) = runBlocking<Unit> {
    val list = asyncStringsList()
    val result = select<String> {
        list.withIndex().forEach { (index, deferred) ->
            deferred.onAwait { answer ->
                "Deferred $index produced answer '$answer'"
            }
        }
    }
    println(result)
    val countActive = list.count { it.isActive }
    println("$countActive coroutines are still active")
}

```

输出是：

``` 
Deferred 4 produced answer 'Waited for 128 ms'
11 coroutines are still active
```

### 切换延迟值的通道

让我们编写一个使用延迟字符串值通道的通道生成器函数，等待每个接收的延迟值，但只有在下一个延迟值结束或通道关闭之前。这个例子将onReceiveOrNull和onAwait子句放在一起 select：

``` 

fun switchMapDeferreds(input: ReceiveChannel<Deferred<String>>) = produce<String> {
    var current = input.receive() // start with first received deferred value
    while (isActive) { // loop while not cancelled/closed
        val next = select<Deferred<String>?> { // return next deferred value from this select or null
            input.onReceiveOrNull { update ->
                update // replaces next value to wait
            }
            current.onAwait { value ->  
                send(value) // send value that current deferred has produced
                input.receiveOrNull() // and use the next deferred from the input channel
            }
        }
        if (next == null) {
            println("Channel was closed")
            break // out of loop
        } else {
            current = next
        }
    }
}

```
为了测试它，我们将使用一个简单的异步函数，它在指定的时间后解析为指定的字符串：


``` 
fun asyncString(str: String, time: Long) = async {
    delay(time)
    str
}
```

main函数只是启动一个协程来打印结果switchMapDeferreds并向它发送一些测试数据：

``` 

fun main(args: Array<String>) = runBlocking<Unit> {
    val chan = Channel<Deferred<String>>() // the channel for test
    launch(coroutineContext) { // launch printing coroutine
        for (s in switchMapDeferreds(chan)) 
            println(s) // print each received string
    }
    chan.send(asyncString("BEGIN", 100))
    delay(200) // enough time for "BEGIN" to be produced
    chan.send(asyncString("Slow", 500))
    delay(100) // not enough time to produce slow
    chan.send(asyncString("Replace", 100))
    delay(500) // give it time before the last one
    chan.send(asyncString("END", 500))
    delay(1000) // give it time to process
    chan.close() // close the channel ... 
    delay(500) // and wait some time to let it finish
}

```

这段代码的结果：

``` 
BEGIN
Replace
END
Channel was closed
```





