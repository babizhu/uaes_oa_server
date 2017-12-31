package com.bbz.outsource.uaes.oa.kt.db

import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.UpdateResult
import io.vertx.kotlin.core.json.json

class LoginDataProvider(dbClient: SQLClient) : AbstractDataProvider(dbClient) {

    suspend fun login(data: JsonArray) : ResultSet {
      return  queryWithParams(dbClient,
                "select * from user where name= (?)",
                json { data }
        )
    }
    suspend fun update(data: JsonArray) : UpdateResult {
        return  updateWithParams(dbClient,
                "INSERT INTO user (name) VALUES (?)",
                json { data }
        )
    }
}