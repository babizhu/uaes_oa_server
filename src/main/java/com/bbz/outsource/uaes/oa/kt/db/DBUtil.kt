@file:Suppress("unused")

package com.bbz.outsource.uaes.oa.kt.db


import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.sql.UpdateResult
import io.vertx.kotlin.coroutines.awaitResult


/**
 * Created by sweet on 2017/12/22.
 * ---------------------------
 */

suspend fun getConnection(client: SQLClient): SQLConnection {
  return awaitResult {
    client.getConnection(it)
  }
}

suspend fun queryWithParams(connection: SQLConnection, sql: String, args: JsonArray): ResultSet {
  return awaitResult {
    connection.queryWithParams(sql, args, it)
  }
}

suspend fun queryWithsParams(client: SQLClient, sql: String, args: JsonArray): ResultSet {
  return awaitResult {
    client.queryWithParams(sql, args, it)
  }
}

suspend fun query(connection: SQLConnection, sql: String): ResultSet {
  return awaitResult {
    connection.query(sql, it)
  }
}

suspend fun query(client: SQLClient, sql: String): ResultSet {
  return awaitResult {
    client.query(sql, it)
  }
}

suspend fun updateWithParams(connection: SQLConnection, sql: String, args: JsonArray): UpdateResult {
  return awaitResult {
    connection.updateWithParams(sql, args, it)
  }
}

suspend fun updateWithParams(client: SQLClient, sql: String, args: JsonArray): UpdateResult {
  return awaitResult {
    client.updateWithParams(sql, args, it)
  }
}

suspend fun update(connection: SQLConnection, sql: String): UpdateResult {
  return awaitResult {
    connection.update(sql, it)
  }
}

suspend fun update(client: SQLClient, sql: String): UpdateResult {
  return awaitResult {
    client.update(sql, it)
  }
}

suspend fun beginTx(connection: SQLConnection) {
  awaitResult<Void> {
    connection.setAutoCommit(false, it)
  }
}

suspend fun commitTx(connection: SQLConnection) {
  awaitResult<Void> {
    connection.commit(it)
  }
}

suspend fun rollbackTx(connection: SQLConnection) {
  awaitResult<Void> {
    connection.rollback(it)
  }
}

suspend fun executeSQL(connection: SQLConnection, sql: String) {
  awaitResult<Void> {
    connection.execute(sql, it)
  }
}

suspend fun fluentTx(client: SQLClient, sqlAndParamsMap: Map<String, JsonArray>): Boolean {
  val connection1 = getConnection(client)
  connection1.use {
      return try {
          beginTx(connection1)
          for ((sql, params) in sqlAndParamsMap) {
              updateWithParams(connection1, sql, params)
          }

          commitTx(connection1)
          true
      } catch (e: Exception) {
          rollbackTx(connection1)
          false
      }
  }

}