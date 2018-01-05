package com.bbz.outsource.uaes.oa.kt.http.handlers


import com.bbz.outsource.uaes.oa.kt.consts.ErrorCode
import com.bbz.outsource.uaes.oa.kt.consts.ErrorCodeException
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

/**
 * Created by sweet on 2017/12/25.
 * ---------------------------
 */
fun HttpServerResponse.endFail(msg: String) {
    this.setStatusCode(500).putHeader("content-type", "application/json; charset=utf-8")
            .end(json {
                obj(
                        "eid" to ErrorCode.SYSTEM_ERROR,
                        "error" to msg
                )
            }.encode())
}

fun HttpServerResponse.endFail(exception: ErrorCodeException) {
    this.setStatusCode(500).putHeader("content-type", "application/json; charset=utf-8")
            .end(json {
                obj(
                        "eid" to exception.errorCode,
                        "msg" to exception.message
                )
            }.encode())
}

fun HttpServerResponse.endFail(errorCode: ErrorCode) {
    this.setStatusCode(500).putHeader("content-type", "application/json; charset=utf-8")
            .end(json {
                obj(
                        "eid" to errorCode
                )
            }.encode())
}

fun HttpServerResponse.endFail(errorCode: ErrorCode, msg: String) {
    this.setStatusCode(500).putHeader("content-type", "application/json; charset=utf-8")
            .end(json {
                obj(
                        "eid" to errorCode,
                        "msg" to msg
                )
            }.encode())
}

fun HttpServerResponse.endSuccess(body: JsonObject) {
    this.putHeader("content-type", "application/json; charset=utf-8")
            .end(body.put("time", System.currentTimeMillis()).encode())
}

fun HttpServerResponse.endSuccess(body: String) {
    this.putHeader("content-type", "application/json; charset=utf-8")
            .end(JsonObject().put("result",body).encode())
}