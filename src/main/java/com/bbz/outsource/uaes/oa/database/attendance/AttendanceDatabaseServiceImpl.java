package com.bbz.outsource.uaes.oa.database.attendance;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import java.util.List;

/**
 * @author liulaoye
 */
public class AttendanceDatabaseServiceImpl implements AttendanceDatabaseService {

  private final JDBCClient dbClient;

  AttendanceDatabaseServiceImpl(JDBCClient dbClient) {
    this.dbClient = dbClient;
  }

  @Override
  public AttendanceDatabaseService search(JsonObject condition, Handler<AsyncResult<List<JsonArray>>> resultHandler) {
    dbClient.query("select * from ask_for_leave", res -> {
      if (res.succeeded()) {
        ResultSet result = res.result();
        List<JsonArray> pages = result.getResults();
        resultHandler.handle(Future.succeededFuture(pages));
      } else {
        res.cause().printStackTrace();
      }
    });
    return this;
  }

  @Override
  public AttendanceDatabaseService query(JsonObject condition,
      Handler<AsyncResult<String>> resultHandler) {
    System.out.println(condition);

    resultHandler.handle(Future.succeededFuture("abcdffghkdk"));

    return this;
  }
//
//  @Override
//  public AttendanceDatabaseService delete(JsonObject condition,
//      Handler<AsyncResult<List<JsonArray>>> resultHandler) {
//    return this;
//  }
//
//  @Override
//  public AttendanceDatabaseService create(JsonObject condition,
//      Handler<AsyncResult<List<JsonArray>>> resultHandler) {
//    return this;
//  }
//
//  @Override
//  public AttendanceDatabaseService update(JsonObject condition,
//      Handler<AsyncResult<List<JsonArray>>> resultHandler) {
//    return this;
//  }
//
  @Override
  public void doSomething(String str,Handler<AsyncResult<String>> resultHandler) {
    System.out.println(str);
    resultHandler.handle(Future.succeededFuture("刘老爷"));
  }


}
