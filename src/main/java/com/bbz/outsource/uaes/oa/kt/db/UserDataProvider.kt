package com.bbz.outsource.uaes.oa.kt.db

import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.UpdateResult
import io.vertx.kotlin.core.json.json

class UserDataProvider(dbClient: SQLClient) : AbstractDataProvider(dbClient) {

    suspend fun create(data: JsonArray) : UpdateResult {
      return com.bbz.outsource.uaes.oa.database.updateWithParams(dbClient,
              "INSERT INTO user (username,password_salt,password) VALUES (?,?,?)",
              json { data }
      )
    }
    suspend fun update(data: JsonArray) : UpdateResult {
        return com.bbz.outsource.uaes.oa.database.updateWithParams(dbClient,
                "INSERT INTO user (name) VALUES (?)",
                json { data }
        )
    }
}