package com.bbz.outsource.uaes.oa.kt.http.handlers.user

import com.bbz.outsource.uaes.oa.kt.consts.JsonConsts
import com.bbz.outsource.uaes.oa.kt.coroutineHandler
import com.bbz.outsource.uaes.oa.kt.db.UserDataProvider
import com.bbz.outsource.uaes.oa.kt.http.handlers.AbstractHandler
import com.bbz.outsource.uaes.oa.kt.http.handlers.auth.anno.RequirePermissions
import com.bbz.outsource.uaes.oa.kt.util.CustomHashStrategy
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
    @RequirePermissions("sys:user:create")
    private suspend fun save(ctx: RoutingContext) {
        val userJson = ctx.bodyAsJson
        checkArguments(userJson,"username","password")
        val postId = userJson.getString(JsonConsts.DB_ID)
        val isCreate = (postId == null)
        val result = if (isCreate) {
            create( userJson)
        } else {
            update( userJson)
        }
        ctx.response().end(result.keys.encode())
    }
    private suspend fun del(ctx: RoutingContext) {
        val bodyAsJson = ctx.bodyAsJson
        val params = JsonArray()
        params.add(bodyAsJson.getValue("name"))
        val updateResult = dataProvider.create(params)
        ctx.response().end(updateResult.keys.encode())
    }
    @RequirePermissions("sys:user:create")
    private suspend fun create(userJson:JsonObject): UpdateResult {
        val salt = CustomHashStrategy.generateSalt()

        val cryptPassword = CustomHashStrategy.INSTANCE
                .cryptPassword(userJson.getString(JsonConsts.USER_PASSWORD), salt)

//        userJson.put(JsonConsts.USER_PASSWORD, cryptPassword)
        userJson.remove(JsonConsts.DB_ID)//去掉_id，以便让db自动生成

        val params = JsonArray()
        params.add(userJson.getValue("username")).add(salt).add(cryptPassword)
        return dataProvider.create(params)
//        ctx.response().end(updateResult.keys.encode())
    }
    private suspend fun update(userJson:JsonObject) : UpdateResult{

        val params = JsonArray()
        params.add(userJson.getValue("name"))
        return dataProvider.create(params)
//        ctx.response().end(updateResult.keys.encode())
    }

    @RequirePermissions("sys:user:query")
    private suspend fun query(ctx: RoutingContext) {
    }

}