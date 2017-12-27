package com.bbz.outsource.uaes.oa.kt.http

import com.bbz.outsource.uaes.oa.kt.LaunchVerticle
import com.bbz.outsource.uaes.oa.kt.http.handlers.UserHandler
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler

fun LaunchVerticle.createHttpServer() {
    val logger = LaunchVerticle.logger
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

private fun LaunchVerticle.initHandler(mainRouter: Router) {
    mainRouter.route().handler(BodyHandler.create())
    dispatcherHandler(mainRouter)
    adapterReactHandler(mainRouter)//这个只能放在倒数第二的位置
    mainRouter.route().handler(StaticHandler.create())//静态文件处理，必须放在最后
//                mainRouter.failureHandler(errorHandler)
}

private fun LaunchVerticle.dispatcherHandler(mainRouter: Router) {
    mainRouter.mountSubRouter("/user", UserHandler(dbClient).addRouter(Router.router(vertx)))
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