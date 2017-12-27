package com.bbz.outsource.uaes.oa.kt.http.handlers

import com.bbz.outsource.uaes.oa.kt.coroutineHandler
import com.bbz.outsource.uaes.oa.kt.db.UserDataProvider
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.JsonArray

class UserHandler(dbClient: SQLClient) : AbstractHandler(dbClient) {
    private val dataProvider:UserDataProvider = UserDataProvider(dbClient)
    fun addRouter(mainRouter: Router): Router? {

        mainRouter.route("/create").coroutineHandler{ create(it) }
        return mainRouter
    }
    private suspend fun create(ctx: RoutingContext) {
        val bodyAsJson = ctx.bodyAsJson
        
//        updateWithParams(dbClient,
//                "INSERT INTO t_student (name, age, school_id, teacher_id) VALUES (?,?,?,?)",
//                json { bodyAsJsonArray }
//        )
//
        val params = JsonArray()
        params.add(bodyAsJson.getValue("name"))
        val updateResult = dataProvider.create(params)
        ctx.response().end(updateResult.keys.encode())
    }

}