package com.bbz.outsource.uaes.oa.kt

import kotlinx.coroutines.experimental.launch
import org.junit.Test
import kotlin.concurrent.thread
import kotlin.coroutines.experimental.*

class MainVerticleTest {
    fun asyncCalcMd51(path:String,block: () -> Int){
        var invoke = block.invoke()
        println(invoke)
    }
    @Test
    fun t(){
        asyncCalcMd51("a",{
            return@asyncCalcMd51 102
        })
    }
    @Test
    fun findTusi() {
    }

    @Test
    fun foo() {
        println(Thread.currentThread())

        launch {
            println(suspendFunc1())
            println(suspendFunc2())
            println(suspendFunc3())
            println("coroutine is about to exit")
        }
        println("launch has returned")
        Thread.sleep(200) // make sure that the main thread won't exit before coroutine exit
    }

    private fun suspendFunc3(): Int {
        return 3
    }

    private suspend fun suspendFunc2(): Int {
        return suspendCoroutine { cont ->
            cont.resume(2)
        }
    }
    class FilePath(val path: String): AbstractCoroutineContextElement(FilePath){
        companion object Key : CoroutineContext.Key<FilePath>
    }
    private suspend fun suspendFunc1(): Int {
        return suspendCoroutine { cont ->
            println(Thread.currentThread())

            println("before thread")
            thread {
                Thread.sleep(100)
                cont.resume(1)
            }
        }
    }
}

fun main(args: Array<String>) {
    println("before coroutine")
    //启动我们的协程 
    asyncCalcMd5("test.zip") {
        println("in coroutine. Before suspend.")
        //暂停我们的线程，并开始执行一段耗时操作 
        val result: String = suspendCoroutine {
            continuation ->
            println("in suspend block.")
            continuation.resume(calcMd5(continuation.context[FilePath]!!.path))
            println("after resume.")
        }
        println("in coroutine. After suspend. result = $result")
    }
    println("after coroutine")
}
/**
 * 上下文，用来存放我们需要的信息，可以灵活的自定义
 */
class FilePath(val path: String): AbstractCoroutineContextElement(FilePath){
    companion object Key : CoroutineContext.Key<FilePath>
}


fun asyncCalcMd5(path: String, block: suspend () -> Unit) {
    val continuation = object : Continuation<Unit> {
        override val context: CoroutineContext
            get() = FilePath(path)

        override fun resume(value: Unit) {
            println("resume: $value")
        }

        override fun resumeWithException(exception: Throwable) {
            println(exception.toString())
        }
    }
    block.startCoroutine(continuation)
}

fun calcMd5(path: String): String{
    println("calc md5 for $path.")
    //暂时用这个模拟耗时 
    Thread.sleep(1000)
    //假设这就是我们计算得到的 MD5 值 
    return System.currentTimeMillis().toString()
} 