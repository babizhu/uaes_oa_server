package com.bbz.outsource.uaes.oa.database


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
  this.putHeader("content-type", "application/json; charset=utf-8")
    .end(json {
      obj(
        "error" to msg,
        "time" to System.currentTimeMillis()
      )
    }.encode())
}

fun HttpServerResponse.endFail(exception: ErrorCodeException, msg: String) {
    this.setStatusCode(500).putHeader("content-type", "application/json; charset=utf-8")
            .end(json {
                obj(
                        "eid" to exception.errorCode.toNum(),
                        "msg" to msg
                )
            }.encode())
}

fun HttpServerResponse.endSuccess(body: JsonObject) {
  this.putHeader("content-type", "application/json; charset=utf-8")
    .end(body.put("time", System.currentTimeMillis()).encode())
}