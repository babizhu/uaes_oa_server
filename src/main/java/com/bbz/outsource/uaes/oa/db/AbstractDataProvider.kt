package com.bbz.outsource.uaes.oa.db

import io.vertx.ext.sql.SQLClient

abstract class AbstractDataProvider(dbClient: SQLClient)   {
    val dbClient: SQLClient = dbClient
}