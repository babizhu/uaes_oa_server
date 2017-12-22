package com.bbz.outsource.uaes.oa.database.attendance;


import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import java.util.List;

@ProxyGen
@VertxGen
public interface AttendanceDatabaseService {
  @GenIgnore
  static AttendanceDatabaseService create(JDBCClient dbClient) {
    return new AttendanceDatabaseServiceImpl(dbClient);
  }
  static AttendanceDatabaseService createProxy(Vertx vertx, String address) {
    return new AttendanceDatabaseServiceVertxEBProxy(vertx, address);
  }


  @Fluent
  AttendanceDatabaseService search(JsonObject condition, Handler<AsyncResult<List<JsonArray>>> resultHandler);
}
