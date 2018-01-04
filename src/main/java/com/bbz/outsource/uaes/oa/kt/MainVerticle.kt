@file:Suppress("unused")

package com.bbz.outsource.uaes.oa.kt

import com.bbz.outsource.uaes.oa.kt.consts.ErrorCode
import com.bbz.outsource.uaes.oa.kt.consts.ErrorCodeException
import com.bbz.outsource.uaes.oa.kt.http.createHttpServer
import com.bbz.outsource.uaes.oa.kt.http.handlers.auth.CustomJwt
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
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

        val jwtAuthOptions = JWTAuthOptions()
                .setKeyStore(KeyStoreOptions()
                        .setPath("./resources/keystore.jceks")
                        .setType("jceks")
                        .setPassword("secret"))
        jwtAuthProvider = JWTAuth.create(vertx, jwtAuthOptions)
        createHttpServer()
        CustomJwt.initRole2PermissionsMap(dbClient)
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