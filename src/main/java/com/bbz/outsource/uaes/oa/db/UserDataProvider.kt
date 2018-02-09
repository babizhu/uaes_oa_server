package com.bbz.outsource.uaes.oa.db

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.UpdateResult
import io.vertx.kotlin.core.json.json

class UserDataProvider(dbClient: SQLClient) : AbstractDataProvider(dbClient) {

    suspend fun create(data: JsonArray): UpdateResult {
        return updateWithParams(dbClient,
                "INSERT INTO user (username,password_salt,password) VALUES (?,?,?)",
                json { data }
        )
    }

    suspend fun update(data: JsonArray): UpdateResult {
        return updateWithParams(dbClient,
                "INSERT INTO user (name) VALUES (?)",
                json { data }
        )
    }

    suspend fun query(condition: JsonObject): ResultSet {
        var sql = "select * from user "
        val conditionArray = JsonArray()

        if (!condition.isEmpty) {
            sql += " where"
            for (entry in condition) {
                sql += " ${entry.key}=? and "
                conditionArray.add(entry.value)
            }
            sql = sql.substring(0, sql.length - 4)
        }

        println(sql)
        return queryWithParams(dbClient,
                sql,
                conditionArray)

    }
}