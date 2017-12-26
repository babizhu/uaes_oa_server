package com.bbz.outsource.uaes.oa.kt.http.handlers

import com.bbz.outsource.uaes.oa.kt.coroutineHandler
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

class UserHandler {
    fun addRouter(mainRouter: Router): Router? {
        mainRouter.route("/create").coroutineHandler{ create(it) }
        return mainRouter
    }
    private suspend fun create(context: RoutingContext) {
//        val bodyAsJsonArray = context.bodyAsJsonArray
//
//        val result = updateWithParams(client,
//                "INSERT INTO t_student (name, age, school_id, teacher_id) VALUES (?,?,?,?)",
//                json { bodyAsJsonArray }
//        )
        context.response().end("create success!!!")
    }

}