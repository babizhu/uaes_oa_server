@file:Suppress("unused")

package com.bbz.outsource.uaes.oa

import com.bbz.outsource.uaes.oa.http.createHttpServer
import com.bbz.outsource.uaes.oa.http.handlers.auth.CustomJwtImpl
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.auth.KeyStoreOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch
import java.nio.charset.Charset
import java.time.LocalTime
import kotlin.coroutines.experimental.suspendCoroutine


/**
 * MainVerticle
 * ---------------------------
 */
fun main(args: Array<String>) {
//    System.setProperty("vertx.logger-delegate-factory-class-name",
//            "io.vertx.core.logging.Log4j2LogDelegateFactory")
    val vertxOptions = VertxOptions()
    vertxOptions.blockedThreadCheckInterval = 1000000
    val vertx = Vertx.vertx(vertxOptions)
    vertx.deployVerticle(MainVerticle())
}

@Suppress("unused")
class MainVerticle : CoroutineVerticle() {
    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }

    lateinit var jwtAuthProvider: JWTAuth
    lateinit var dbClient: SQLClient
    lateinit var httpClient: HttpClient

    override suspend fun start() {
        dbClient = MySQLClient.createShared(vertx, json {
            obj(
                    "host" to "127.0.0.1",
                    "port" to 3306,
                    "username" to "root",
                    "password" to "root",
                    "database" to "uaes_oa"
            )
        })

        createJwtProvider()
        createHttpServer()
        CustomJwtImpl.initRole2PermissionsMap(dbClient)
        createHttpClient()
//        findTusi()
    }

    private fun createHttpClient() {
        httpClient = vertx.createHttpClient(HttpClientOptions().setVerifyHost(false).setSsl(true).setTrustAll(true))
    }

    private fun createJwtProvider() {
        val jwtAuthOptions = JWTAuthOptions()
                .setKeyStore(KeyStoreOptions()
                        .setPath("./resources/keystore.jceks")
                        .setType("jceks")
                        .setPassword("secret"))
        jwtAuthProvider = JWTAuth.create(vertx, jwtAuthOptions)
    }
    private suspend fun findTusi() {
        val list = ArrayList<Int>()
        for (i in 201..401) {
            println("${LocalTime.now()} 开始处理https://www.smzdm.com/p$i/")

            val buffer = getNow(443, "www.smzdm.com", "/p$i/").toString(Charset.defaultCharset())

            if (buffer.indexOf("吐司") != -1) {
                list.add(i)
            }
            println("${LocalTime.now()} https://www.smzdm.com/p$i/处理完毕")

        }
        list.map {
            println("包含吐司的页面地址为https://www.smzdm.com/p$it/")
        }
    }

    //
//    private suspend fun getNow1(port: Int, host: String, uri: String):Buffer{
//        async {
//            httpClient
//                    .get(port, host, uri) { response: HttpClientResponse ->
//                        //                            println(response.headers().map { println(it) })
//                        response.bodyHandler { buffer ->
//                            println(Thread.currentThread())
//                            return buffer
//                        }
//                    }
//                    .exceptionHandler { throwable ->
//
//                    }
//                    .end()
//        }
//    }
    private suspend fun getNow(port: Int, host: String, uri: String): Buffer =
            suspendCoroutine { cont ->

                httpClient
                        .get(port, host, uri) { response: HttpClientResponse ->
                            //                            println(response.headers().map { println(it) })
                            response.bodyHandler { buffer ->
                                println(Thread.currentThread())
                                cont.resume(buffer)
                            }
                        }
                        .exceptionHandler { throwable ->
                            cont.resumeWithException(throwable)
                        }
                        .end()

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