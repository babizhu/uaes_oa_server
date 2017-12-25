package com.bbz.outsource.uaes.oa.kt.http

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerRequest
import io.vertx.ext.web.Router

fun createHttpServer(vertx: Vertx){
    val router = Router.router(vertx)
    initHandler(router)
    val portNumber =8000

    vertx.createHttpServer()
            .requestHandler(Handler<HttpServerRequest> { router.accept(it) })
            .listen(portNumber!!) { ar ->
                if (ar.succeeded()) {
//                    log.info("HTTP server running on port " + portNumber!!)
//                    startFuture.complete()
                } else {
//                    log.error("Could not start a HTTP server", ar.cause())
//                    startFuture.fail(ar.cause())
                }
            }
}
fun initHandler(router: Router){

//        router.route().handler(BodyHandler.create())
//                .failureHandler(errorHandler)
}