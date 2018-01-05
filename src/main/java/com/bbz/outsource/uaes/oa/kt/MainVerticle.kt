@file:Suppress("unused")

package com.bbz.outsource.uaes.oa.kt

import com.bbz.outsource.uaes.oa.kt.http.createHttpServer
import com.bbz.outsource.uaes.oa.kt.http.handlers.auth.CustomJwtImpl
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

    suspend override fun start() {
        dbClient = MySQLClient.createShared(vertx, json {
            obj(
                    "host" to "127.0.0.1",
                    "port" to 3306,
                    "username" to "root",
                    "password" to "root",
                    "database" to "uaes_oa"
            )
        })

        val jwtAuthOptions = JWTAuthOptions()
                .setKeyStore(KeyStoreOptions()
                        .setPath("./resources/keystore.jceks")
                        .setType("jceks")
                        .setPassword("secret"))
        jwtAuthProvider = JWTAuth.create(vertx, jwtAuthOptions)
        createHttpServer()
        CustomJwtImpl.initRole2PermissionsMap(dbClient)

        var httpClient = vertx.createHttpClient()

        var buffer = httpClient.getNow(80, "www.sina.com.cn", "/")
        println(buffer.toString(Charset.defaultCharset()))
        println("12345678")
    }

    private suspend fun HttpClient.getNow(port: Int, host: String, uri: String): Buffer =
            suspendCoroutine { cont ->
                vertx.createHttpClient(HttpClientOptions().setSsl(false).setTrustAll(true))
                        .get(port, host, uri) { response: HttpClientResponse ->
                            println(response.statusCode())
                            response.bodyHandler { buffer ->
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