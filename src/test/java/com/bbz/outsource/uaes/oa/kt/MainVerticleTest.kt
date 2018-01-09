package com.bbz.outsource.uaes.oa.kt

import kotlinx.coroutines.experimental.launch
import org.junit.Test
import kotlin.concurrent.thread
import kotlin.coroutines.experimental.suspendCoroutine

class MainVerticleTest {

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