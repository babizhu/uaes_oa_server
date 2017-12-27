package com.bbz.outsource.uaes.oa.kt.http.handlers

import com.bbz.outsource.uaes.oa.consts.JsonConsts
import com.bbz.outsource.uaes.oa.kt.coroutineHandler
import com.bbz.outsource.uaes.oa.kt.db.UserDataProvider
import com.bbz.outsource.uaes.oa.kt.http.handlers.auth.anno.RequirePermissions
import io.vertx.core.json.JsonObject
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.UpdateResult
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.JsonArray

class UserHandler(dbClient: SQLClient) : AbstractHandler() {
    private val dataProvider:UserDataProvider = UserDataProvider(dbClient)
    fun addRouter(mainRouter: Router): Router? {

        mainRouter.route("/save").coroutineHandler{ save(it) }
        mainRouter.route("/del").coroutineHandler{ del(it) }
        mainRouter.route("/query").coroutineHandler{ query(it) }
        return mainRouter
    }
    private suspend fun save(ctx: RoutingContext) {
        val userJson = ctx.bodyAsJson
        val postId = userJson.getString(JsonConsts.DB_ID)
        create(ctx, userJson)
        val isCreate = postId == "-1"
        val result: UpdateResult
        result = if (isCreate) {
            create(ctx, userJson)
        } else {
            update(ctx, userJson)
        }


//        val bodyAsJson = ctx.bodyAsJson
//        val params = JsonArray()
//        params.add(bodyAsJson.getValue("name"))
//        val updateResult = dataProvider.create(params)
        ctx.response().end(result.keys.encode())
    }
    private suspend fun del(ctx: RoutingContext) {
        val bodyAsJson = ctx.bodyAsJson
        val params = JsonArray()
        params.add(bodyAsJson.getValue("name"))
        val updateResult = dataProvider.create(params)
        ctx.response().end(updateResult.keys.encode())
    }
    private suspend fun create(ctx: RoutingContext,userJson:JsonObject): UpdateResult {
        val bodyAsJson = ctx.bodyAsJson
        val params = JsonArray()
        params.add(bodyAsJson.getValue("name"))
        return dataProvider.create(params)
//        ctx.response().end(updateResult.keys.encode())
    }
    private suspend fun update(ctx: RoutingContext,userJson:JsonObject) : UpdateResult{

        val params = JsonArray()
        params.add(userJson.getValue("name"))
        return dataProvider.create(params)
//        ctx.response().end(updateResult.keys.encode())
    }

    @RequirePermissions("sys:user:query")
    private suspend fun query(ctx: RoutingContext) {
    }

}