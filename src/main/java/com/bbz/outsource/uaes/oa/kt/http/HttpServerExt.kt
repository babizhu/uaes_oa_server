package com.bbz.outsource.uaes.oa.kt.http

import com.bbz.outsource.uaes.oa.database.endFail
import com.bbz.outsource.uaes.oa.kt.MainVerticle
import com.bbz.outsource.uaes.oa.kt.http.handlers.UserHandler
import io.vertx.core.Handler
import io.vertx.ext.auth.KeyStoreOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler

fun MainVerticle.createHttpServer() {
    val logger = MainVerticle.logger
    val router = Router.router(vertx)
    initHandler(router)

    vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(8000, {
                if (it.succeeded()) {
                    logger.info("http server start !!! ${it.result().actualPort()}")
                } else {
//                        logger.error("http server error :: " + it.cause().message)
                    it.cause().printStackTrace()
                }
            })
}

private fun MainVerticle.initHandler(mainRouter: Router) {
    val jwtAuthOptions = JWTAuthOptions()
            .setKeyStore(KeyStoreOptions()
                    .setPath("./resources/keystore.jceks")
                    .setType("jceks")
                    .setPassword("secret"))
    jwtAuthProvider = JWTAuth.create(vertx, jwtAuthOptions)

    mainRouter.route().failureHandler(errorHandler)
    mainRouter.route().handler(BodyHandler.create())
    dispatcherHandler(mainRouter)
    adapterReactHandler(mainRouter)//这个只能放在倒数第二的位置

    mainRouter.route().handler(StaticHandler.create())//静态文件处理，必须放在最后

}
val errorHandler = Handler<RoutingContext> {
    it.failure().printStackTrace()
    println("uri ${it.request().uri()}")
    it.response().endFail("ERROR ${it.failure().message}")
}
private fun MainVerticle.dispatcherHandler(mainRouter: Router) {
    mainRouter.mountSubRouter("/user", UserHandler(dbClient).addRouter(Router.router(vertx)))
}

private fun adapterAuthHandler(mainRouter: Router) {


//    mainRouter.route( API_PREFIX+"*" ).handler( CustomJWTAuthHandlerImpl(eventBus, jwtAuthProvider ) );//暂时只能屏蔽
}
/**
 * 适配react客户端的路由模式，访问任何页面都重定向到index.html中
 * @param mainRouter mainRouter
 */
private fun adapterReactHandler(mainRouter: Router) {
    mainRouter.route("/*").handler { ctx ->
        if (!ctx.request().uri().contains(".")) {
            ctx.reroute("/index.html")
        } else {
            ctx.next()
        }
    }
}