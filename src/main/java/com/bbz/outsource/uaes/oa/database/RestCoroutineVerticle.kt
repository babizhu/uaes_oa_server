@file:Suppress("unused")

package com.bbz.outsource.uaes.oa.database

import com.bbz.outsource.uaes.oa.kt.http.handlers.endFail
import com.bbz.outsource.uaes.oa.kt.http.handlers.endSuccess
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch


/**
 * 重点 演示对 MySQL 的操作， 利用 协程 写出类似同步的代码
 * Created by sweet on 2017/12/21.
 * ---------------------------
 */
fun main(args: Array<String>) {
//    System.setProperty("vertx.logger-delegate-factory-class-name",
//            "io.vertx.core.logging.Log4j2LogDelegateFactory")
    Vertx.vertx().deployVerticle(RestCoroutineVerticle())
}

@Suppress("unused")
class RestCoroutineVerticle : CoroutineVerticle() {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private lateinit var client: SQLClient

    suspend override fun start() {
        client = MySQLClient.createShared(vertx, json {
            obj(
                    "host" to "127.0.0.1",
                    "port" to 3306,
                    "username" to "root",
                    "password" to "root",
                    "database" to "uaes_oa"
            )
        })

        val data = JsonArray()
        val insertSql = "INSERT INTO ask_for_leave (user, begin, approval_result, approvaler) VALUES (?,?,?,?)"

        data.add("user")
                .add(1)
                .add(1)
                .add("babizhu")
        val result = updateWithParams(client,
                insertSql,
                json { data }
        )
        println(result.toJson())

        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())
                .failureHandler(errorHandler)

        router.get("/student/:id").handler { getStudent(it) }
        router.post("/student").coroutineHandler { createStudent(it) }
        router.delete("/student/:id").coroutineHandler { deleteStudent(it) }
        router.put("/student/:id").coroutineHandler { updateStudent(it) }

        router.get("/teacher/:id").coroutineHandler { getTeacher(it) }
        router.get("/teachers").coroutineHandler { getTeachers(it) }

        // 演示事务
        router.post("/tx").coroutineHandler { tx(it) }
        // 演示复杂事务
        router.post("/tx2").coroutineHandler { tx3(it) }

        router.get("/test/error").handler({
            1 / 0
            it.response().end("OK")
        })

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8080, {
                    if (it.succeeded()) {
                        logger.info("http server start !!! ${it.result().actualPort()}")
                    } else {
                        logger.error("http server error :: " + it.cause().message)
                        it.cause().printStackTrace()
                    }
                })
    }

    private suspend fun tx3(context: RoutingContext) {
        val bodyAsJson = context.bodyAsJson
        val map = mutableMapOf<String, JsonArray>()
        bodyAsJson.forEach { k ->
            println("sql: ${k.key}, value: ${k.value}")
            map.put(k.key, JsonArray(k.value.toString()))
        }
        val flag = fluentTx(client, map)
        context.response().endSuccess(json {
            obj("flag" to flag)
        })
    }

    private suspend fun tx(context: RoutingContext) {

        val bodyAsJson = context.bodyAsJson
        val school = bodyAsJson.getJsonArray("school")
        val teacher = bodyAsJson.getJsonArray("teacher")

        val connection = getConnection(client)
        connection.use {
            beginTx(connection)
            try {

                val schoolResult = updateWithParams(connection,
                        "INSERT INTO t_school (name) VALUES (?)",
                        json { array(school.list[0]) }
                )

                val teacherResult = updateWithParams(connection,
                        "INSERT INTO t_teacher (name, school_id) VALUES (?,?)",
                        json { teacher }
                )

                logger.debug("school result: ${schoolResult.toJson().encodePrettily()}")
                logger.debug("teacher result: ${teacherResult.toJson().encodePrettily()}")
                commitTx(connection)

                context.response().endSuccess(json {
                    obj("msg" to "OK")
                })
            } catch (e: Exception) {
                e.printStackTrace()
                rollbackTx(connection)
                context.response().endFail("事务失败")
            }
        }
    }

    private suspend fun getTeachers(context: RoutingContext) {
        val teacherResultSet = query(client,
                "SELECT id, name, school_id FROM t_teacher"
        )

        val result = teacherResultSet.rows
        result.forEach {
            val tId = it.getInteger("id")
            val studentResultSet = queryWithsParams(client,
                    "SELECT name, age FROM t_student WHERE teacher_id = ?",
                    json { array(tId) }
            )

            val studentResult = studentResultSet.rows
            it.put("student", studentResult)
        }
        logger.debug(result.toString())
        context.response().end(result.toString())
    }

    private suspend fun getTeacher(context: RoutingContext) {
        val id = context.pathParam("id")
        val teacherResultSet = queryWithsParams(client,
                "SELECT id, name, school_id FROM t_teacher WHERE id = ?",
                json { array(id) }
        )

        val result = teacherResultSet.rows
        result.forEach {
            val tId = it.getInteger("id")
            val studentResultSet = queryWithsParams(client,
                    "SELECT name, age FROM t_student WHERE teacher_id = ?",
                    json { array(tId) }
            )

            val studentResult = studentResultSet.rows
            it.put("student", studentResult)
        }

        logger.debug(result.toString())
        context.response().end(result.toString())
    }

    private suspend fun updateStudent(context: RoutingContext) {
        val id = context.pathParam("id")
        val bodyAsJsonArray = context.bodyAsJsonArray
        bodyAsJsonArray.add(id)
        val result = updateWithParams(client,
                "UPDATE t_student SET name = ?, age = ?, school_id = ?, teacher_id = ? WHERE id = ?",
                json { bodyAsJsonArray }
        )

        context.response().end(result.toJson().encodePrettily())
    }

    private suspend fun deleteStudent(context: RoutingContext) {
        val id = context.pathParam("id")
        val result = updateWithParams(client,
                "DELETE FROM t_student WHERE id = ?",
                json { array(id) }
        )

        context.response().end(result.toJson().encodePrettily())
    }

    private suspend fun createStudent(context: RoutingContext) {
        val bodyAsJsonArray = context.bodyAsJsonArray

        val result = updateWithParams(client,
                "INSERT INTO t_student (name, age, school_id, teacher_id) VALUES (?,?,?,?)",
                json { bodyAsJsonArray }
        )
        context.response().end(result.toJson().encodePrettily())
    }

    private fun getStudent(context: RoutingContext) {
        val id = context.pathParam("id")
        launch(context.vertx().dispatcher()) {
            val result = awaitResult<ResultSet> {
                client.queryWithParams("SELECT id, name, age FROM t_student WHERE id = ?",
                        json { array(id) }, it)
            }
            if (result.rows.size == 1) {
                context.response().end(result.rows[0].encodePrettily())
            } else {
                context.response().setStatusCode(404).end()
            }
        }
    }

    val errorHandler = Handler<RoutingContext> {
        it.failure().printStackTrace()
        println("uri ${it.request().uri()}")
        it.response().endFail("ERROR ${it.failure().message}")
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