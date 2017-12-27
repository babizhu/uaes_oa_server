package com.bbz.outsource.uaes.oa.kt.db

import io.vertx.ext.sql.SQLClient

abstract class AbstractDataProvider(dbClient: SQLClient)   {
    val dbClient: SQLClient = dbClient
}