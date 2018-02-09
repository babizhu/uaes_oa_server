package com.bbz.outsource.uaes.oa.db

import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.UpdateResult
import io.vertx.kotlin.core.json.json

class LoginDataProvider(dbClient: SQLClient) : AbstractDataProvider(dbClient) {

    suspend fun login(data: JsonArray): ResultSet {
        return queryWithParams(dbClient,
                "select password,password_salt from user where username= (?)",
                json { data }
        )
    }

    suspend fun update(data: JsonArray): UpdateResult {
        return updateWithParams(dbClient,
                "INSERT INTO user (username) VALUES (?)",
                json { data }
        )
    }

    suspend fun queryRoles(data: JsonArray): ResultSet {
        return queryWithParams(dbClient,
                "select role from user_roles where username= (?)",
                json { data }
        )
    }

    suspend fun queryRolesPermission(): ResultSet {
        return query(dbClient, "select * from roles_perms")
    }

}