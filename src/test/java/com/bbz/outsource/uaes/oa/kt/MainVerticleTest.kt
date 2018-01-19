package com.bbz.outsource.uaes.oa.kt

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import kotlin.system.measureTimeMillis

suspend fun doSomethingUsefulOne(): Int {
    delay(1000L) // pretend we are doing something useful here
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    println("<top>.doSomethingUsefulTwo")
    delay(1000L) // pretend we are doing something useful here, too
    return 29
}
fun asyncSomethingUsefulOne() = async {
    doSomethingUsefulOne()
}

// The result type of asyncSomethingUsefulTwo is Deferred<Int>
fun asyncSomethingUsefulTwo() = async {
    println("asyncSomethingUsefulTwo")
    doSomethingUsefulTwo()
}
fun main(args: Array<String>)  {
    val time = measureTimeMillis {
        // we can initiate async actions outside of a coroutine
        val one = asyncSomethingUsefulOne()
        val two = asyncSomethingUsefulTwo()
        // but waiting for a result must involve either suspending or blocking.
        // here we use `runBlocking { ... }` to block the main thread while waiting for the result
        Thread.sleep(10000)
        println("xxxxxxxxxxxxxx")
        runBlocking {
            println("The answer is ${one.await() + two.await()}")
        }
    }
    println("Completed in $time ms")

    val student = Student("abcd",34)
    student.age = 45
    println(student)
}
data class Student(var name:String,var age:Int)