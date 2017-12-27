@file:Suppress("unused")

package com.bbz.outsource.uaes.oa.kt

import com.bbz.outsource.uaes.oa.consts.ErrorCode
import com.bbz.outsource.uaes.oa.consts.ErrorCodeException
import com.bbz.outsource.uaes.oa.kt.http.createHttpServer
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch


/**
 * 重点 演示对 MySQL 的操作， 利用 协程 写出类似同步的代码
 * Created by sweet on 2017/12/21.
 * ---------------------------
 */
fun main(args: Array<String>) {
//    System.setProperty("vertx.logger-delegate-factory-class-name",
//            "io.vertx.core.logging.Log4j2LogDelegateFactory")
    Vertx.vertx().deployVerticle(LaunchVerticle())
}

@Suppress("unused")
class LaunchVerticle : CoroutineVerticle() {
    companion object {
        val logger = LoggerFactory.getLogger(this.javaClass)!!
    }

    lateinit var dbClient: SQLClient

    suspend override fun start() {
        try {
            throw ErrorCodeException(ErrorCode.PARAMETER_ERROR,"abcd")
        }catch (e:ErrorCodeException){
            println(""+e.errorCode +":"+e.message)
        }
        dbClient = MySQLClient.createShared(vertx, json {
            obj(
                    "host" to "127.0.0.1",
                    "port" to 3306,
                    "username" to "root",
                    "password" to "root",
                    "database" to "uaes_oa"
            )
        })
        createHttpServer()
    }
}
fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
    handler { ctx ->
        launch(ctx.vertx().dispatcher()) {
            try {
                fn(ctx)
            } catch (e: Exception) {
                ctx.fail(e)
            }
        }
    }
}