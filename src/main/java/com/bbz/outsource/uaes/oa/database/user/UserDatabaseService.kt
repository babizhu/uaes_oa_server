package com.bbz.outsource.uaes.oa.database.user

import io.vertx.codegen.annotations.GenIgnore
import io.vertx.codegen.annotations.ProxyGen
import io.vertx.codegen.annotations.VertxGen
import io.vertx.ext.jdbc.JDBCClient

@ProxyGen
@VertxGen
interface UserDatabaseService {
    /**
     * create
     *
     * @param dbClient  dbClient
     * @return this
     */
    @GenIgnore
    fun create(dbClient: JDBCClient): UserDatabaseService {
        return UserDatabaseServiceImpl(dbClient)
    }

    /**
     * createProxy
     * @param vertx     vertx
     * @param address   address
     * @return  AttendanceDatabaseServiceVertxEBProxy
     */
//    fun createProxy(vertx: Vertx, address: String): UserDatabaseService {
//        return UserDatabaseServiceVertxEBProxy(vertx, address)//kt不支持codegen
//    }
}
