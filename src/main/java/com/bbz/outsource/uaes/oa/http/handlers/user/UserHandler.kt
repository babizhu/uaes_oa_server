package com.bbz.outsource.uaes.oa.http.handlers.user

import com.bbz.outsource.uaes.oa.consts.JsonConsts
import com.bbz.outsource.uaes.oa.coroutineHandler
import com.bbz.outsource.uaes.oa.db.UserDataProvider
import com.bbz.outsource.uaes.oa.http.handlers.AbstractHandler
import com.bbz.outsource.uaes.oa.http.handlers.auth.CustomJwtImpl
import com.bbz.outsource.uaes.oa.http.handlers.auth.anno.RequirePermissions
import com.bbz.outsource.uaes.oa.http.handlers.endSuccess
import com.bbz.outsource.uaes.oa.util.CustomHashStrategy
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.JsonArray
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json

class UserHandler(dbClient: SQLClient) : AbstractHandler() {
    private val dataProvider: UserDataProvider = UserDataProvider(dbClient)
    fun addRouter(mainRouter: Router): Router? {

//        mainRouter.route("/save").coroutineHandler { save(it) }
        mainRouter.route("/create").coroutineHandler { create(it) }
        mainRouter.route("/update").coroutineHandler { update(it) }
        mainRouter.route("/del").coroutineHandler { del(it) }
        mainRouter.route("/query").coroutineHandler { query(it) }
        mainRouter.route("/permisstionsQuery").coroutineHandler { permisstionsQuery(it) }
        mainRouter.route("/test/直接跟函数的方法").handler(this::aa)
        return mainRouter
    }

    private fun aa(ctx:RoutingContext){
        ctx.response().end("hello")
    }

//    @RequirePermissions("sys:user:create")
//    private suspend fun save(ctx: RoutingContext) {
//        val userJson = ctx.bodyAsJson
//        checkArguments(userJson, "username", "password")
//        val postId = userJson.getString(JsonConsts.DB_ID)
//        val isCreate = (postId == null)
//        val result = if (isCreate) {
////            create( userJson)
//        } else {
//            update(userJson)
//        }
//        ctx.response().endSuccess(result.toJson())
//    }

    @RequirePermissions("sys:user:del")
    private suspend fun del(ctx: RoutingContext) {
        val bodyAsJson = ctx.bodyAsJson

        val updateResult = dataProvider.create(json{array(bodyAsJson.getValue("name"))})
        ctx.response().endSuccess(updateResult.keys.encode())
    }

    @RequirePermissions("sys:user:create")
    private suspend fun create(ctx: RoutingContext) {
        val userJson = ctx.bodyAsJson
        checkArguments(userJson, "username", "password")
        val salt = CustomHashStrategy.generateSalt()

        val cryptPassword = CustomHashStrategy.INSTANCE
                .cryptPassword(userJson.getString(JsonConsts.USER_PASSWORD), salt)
        json { array(userJson.getValue("username"), salt, cryptPassword) }

        var result = dataProvider.create(json {
            array(
                    userJson.getValue("username"),
                    salt,
                    cryptPassword
            )
        })
        ctx.response().endSuccess(result.toJson())
    }

    @RequirePermissions("sys:user:update")
    private suspend fun update(ctx: RoutingContext) {
        val userJson = ctx.bodyAsJson
        checkArguments(userJson, "username", "password")
        val params = JsonArray()
        params.add(userJson.getValue("name"))
        val updateResult= dataProvider.create(params)
        ctx.response().endSuccess(updateResult.keys.encode())
    }

    @RequirePermissions("sys:user:query")
    private suspend fun query(ctx: RoutingContext) {
        val condition = ctx.bodyAsJson
        val result = dataProvider.query(condition)

        ctx.response().endSuccess(result.toJson())
    }

    @RequirePermissions("sys:permissions:query")
    private suspend fun permisstionsQuery(ctx: RoutingContext) {

        ctx.response().endSuccess(CustomJwtImpl.URI_PERMISSIONS_MAP.values.flatMap { it }.toSet().joinToString(","))
    }


}